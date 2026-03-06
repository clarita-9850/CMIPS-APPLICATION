#!/usr/bin/env python3
"""
DMX to PostgreSQL INSERT Statement Generator
Converts IBM Curam DMX files to PostgreSQL INSERT statements for IHSS modernized schema
"""

import xml.etree.ElementTree as ET
import os
import sys
from datetime import datetime
from typing import Dict, List, Any, Optional

class DMXParser:
    """Parse Curam DMX XML files and extract data"""
    
    def __init__(self, dmx_file_path: str):
        self.dmx_file_path = dmx_file_path
        self.table_name = None
        self.columns = []
        self.rows = []
        
    def parse(self):
        """Parse the DMX file"""
        tree = ET.parse(self.dmx_file_path)
        root = tree.getroot()
        
        # Get table name
        self.table_name = root.get('name')
        
        # Get column definitions
        for col in root.findall('column'):
            self.columns.append({
                'name': col.get('name'),
                'type': col.get('type')
            })
        
        # Get rows
        for row in root.findall('row'):
            row_data = {}
            for attr in row.findall('attribute'):
                name = attr.get('name')
                value_elem = attr.find('value')
                if value_elem is not None and value_elem.text:
                    row_data[name] = value_elem.text.strip()
                else:
                    row_data[name] = None
            self.rows.append(row_data)
        
        return self
    
    def get_data(self) -> Dict[str, Any]:
        """Return parsed data"""
        return {
            'table_name': self.table_name,
            'columns': self.columns,
            'rows': self.rows
        }


class IHSSInsertGenerator:
    """Generate INSERT statements for IHSS modernized schema"""
    
    @staticmethod
    def format_timestamp(value: str) -> str:
        """Convert Curam timestamp to PostgreSQL timestamp"""
        if not value:
            return 'NULL'
        try:
            # Format: 2009-4-20-09.55.11 or 2009-07-13-15.56.17
            parts = value.replace('.', '-').split('-')
            if len(parts) >= 6:
                year, month, day, hour, minute, second = parts[:6]
                ts = f"{year}-{month.zfill(2)}-{day.zfill(2)} {hour.zfill(2)}:{minute.zfill(2)}:{second.zfill(2)}"
                return f"'{ts}'"
        except:
            pass
        return 'NULL'
    
    @staticmethod
    def format_date(value: str) -> str:
        """Convert Curam date to PostgreSQL date"""
        if not value:
            return 'NULL'
        try:
            # Format: 2000-01-01-00.00.00
            parts = value.split('-')
            if len(parts) >= 3:
                year, month, day = parts[:3]
                return f"'{year}-{month.zfill(2)}-{day.zfill(2)}'"
        except:
            pass
        return 'NULL'
    
    @staticmethod
    def format_bool(value: str) -> str:
        """Convert Curam bool to PostgreSQL bool"""
        if not value:
            return 'FALSE'
        return 'TRUE' if value == '1' else 'FALSE'
    
    @staticmethod
    def format_string(value: str) -> str:
        """Escape and format string value"""
        if not value:
            return 'NULL'
        # Escape single quotes
        escaped = value.replace("'", "''")
        return f"'{escaped}'"
    
    @staticmethod
    def format_number(value: str) -> str:
        """Format number value"""
        if not value:
            return 'NULL'
        return value
    
    @staticmethod
    def generate_program_unit_inserts(org_units: List[Dict], org_structure: List[Dict]) -> str:
        """Generate INSERT statements for ihss_org.program_unit from ORGANISATIONUNIT"""
        inserts = []
        inserts.append("-- ============================================================================")
        inserts.append("-- INSERT STATEMENTS FOR ihss_org.program_unit")
        inserts.append("-- Generated from ORGANISATIONUNIT.dmx")
        inserts.append("-- ============================================================================\n")
        
        # Create mapping table for legacy IDs
        inserts.append("-- Create temporary mapping table for org unit IDs")
        inserts.append("CREATE TEMP TABLE IF NOT EXISTS temp_org_unit_mapping (")
        inserts.append("    legacy_id BIGINT,")
        inserts.append("    new_uuid UUID")
        inserts.append(");\n")
        
        for row in org_units:
            org_unit_id = row.get('organisationUnitID', '')
            name = row.get('name', f'Unit {org_unit_id}')
            status = row.get('statusCode', 'OUSC1')
            business_type = row.get('businessTypeCode', '')
            comments = row.get('comments', '')
            web_address = row.get('webAddress', '')
            location_id = row.get('locationID', '')
            creation_date = row.get('creationDate', '')
            created_by = row.get('createdBy', 'MIGRATION')
            created_on = row.get('createdOn', '')
            
            # Infer unit type from business type code or name
            if 'COUNTY' in name.upper() or business_type == 'OUBTC100':
                unit_type = 'COUNTY'
            elif 'DISTRICT' in name.upper() or business_type == 'OUBTC102':
                unit_type = 'DISTRICT'
            elif 'FIELD' in name.upper():
                unit_type = 'FIELD_OFFICE'
            elif 'TEAM' in name.upper():
                unit_type = 'TEAM'
            else:
                unit_type = 'FIELD_OFFICE'
            
            # Map status code
            if status in ('OUSC1', 'RST1'):
                status_enum = 'ACTIVE'
            else:
                status_enum = 'INACTIVE'
            
            insert = f"""
INSERT INTO ihss_org.program_unit (
    program_unit_id,
    unit_code,
    unit_name,
    unit_type,
    status,
    business_type_code,
    comments,
    web_address,
    effective_start,
    created_by,
    created_at,
    version_number,
    legacy_org_unit_id
) VALUES (
    gen_random_uuid(),
    'ORG-{org_unit_id}',
    {IHSSInsertGenerator.format_string(name)},
    '{unit_type}'::ihss_org.unit_type_enum,
    '{status_enum}'::ihss_org.unit_status_enum,
    {IHSSInsertGenerator.format_string(business_type)},
    {IHSSInsertGenerator.format_string(comments)},
    {IHSSInsertGenerator.format_string(web_address)},
    {IHSSInsertGenerator.format_date(creation_date)},
    {IHSSInsertGenerator.format_string(created_by or 'MIGRATION')},
    {IHSSInsertGenerator.format_timestamp(created_on or '2024-01-01-00.00.00')},
    {row.get('versionNo', '1')},
    {org_unit_id}
);
"""
            inserts.append(insert)
            
            # Add to mapping table
            mapping = f"INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT {org_unit_id}, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = {org_unit_id};"
            inserts.append(mapping)
        
        return '\n'.join(inserts)
    
    @staticmethod
    def generate_job_classification_inserts(jobs: List[Dict]) -> str:
        """Generate INSERT statements for ihss_org.job_classification from JOB"""
        inserts = []
        inserts.append("\n-- ============================================================================")
        inserts.append("-- INSERT STATEMENTS FOR ihss_org.job_classification")
        inserts.append("-- Generated from JOB.dmx")
        inserts.append("-- ============================================================================\n")
        
        inserts.append("CREATE TEMP TABLE IF NOT EXISTS temp_job_mapping (")
        inserts.append("    legacy_id BIGINT,")
        inserts.append("    new_uuid UUID")
        inserts.append(");\n")
        
        for row in jobs:
            job_id = row.get('jobID', '')
            name = row.get('name', f'Job {job_id}')
            job_family = row.get('jobFamily', '')
            grade_level = row.get('gradeLevel', '')
            description = row.get('description', '')
            created_by = row.get('createdBy', 'MIGRATION')
            
            insert = f"""
INSERT INTO ihss_org.job_classification (
    job_classification_id,
    classification_code,
    title,
    classification_family,
    grade_level,
    description,
    created_by,
    created_at,
    version_number,
    legacy_job_id
) VALUES (
    gen_random_uuid(),
    'JOB-{job_id}',
    {IHSSInsertGenerator.format_string(name)},
    {IHSSInsertGenerator.format_string(job_family)},
    {IHSSInsertGenerator.format_string(grade_level)},
    {IHSSInsertGenerator.format_string(description)},
    {IHSSInsertGenerator.format_string(created_by or 'MIGRATION')},
    NOW(),
    1,
    {job_id}
);
"""
            inserts.append(insert)
            
            mapping = f"INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT {job_id}, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = {job_id};"
            inserts.append(mapping)
        
        return '\n'.join(inserts)
    
    @staticmethod
    def generate_staff_account_inserts(users: List[Dict]) -> str:
        """Generate INSERT statements for ihss_org.staff_account from USERS"""
        inserts = []
        inserts.append("\n-- ============================================================================")
        inserts.append("-- INSERT STATEMENTS FOR ihss_org.staff_account")
        inserts.append("-- Generated from USERS.dmx")
        inserts.append("-- ============================================================================\n")
        
        inserts.append("CREATE TEMP TABLE IF NOT EXISTS temp_staff_mapping (")
        inserts.append("    legacy_username VARCHAR(120),")
        inserts.append("    new_uuid UUID")
        inserts.append(");\n")
        
        for row in users:
            username = row.get('USERNAME', '')
            if not username:
                continue
                
            first_name = row.get('FIRSTNAME', '')
            surname = row.get('SURNAME', '')
            full_name = row.get('FULLNAME', '')
            account_enabled = row.get('ACCOUNTENABLED', '1')
            status_code = row.get('STATUSCODE', 'ACTIVE')
            
            # Map employment status
            if status_code in ('ACTIVE', 'USR1'):
                emp_status = 'ACTIVE'
            elif status_code == 'SUSPENDED':
                emp_status = 'ON_LEAVE'
            else:
                emp_status = 'SEPARATED'
            
            insert = f"""
INSERT INTO ihss_org.staff_account (
    staff_id,
    staff_identifier,
    username,
    first_name,
    surname,
    full_name,
    employment_status,
    account_enabled,
    location_id,
    created_by,
    created_at,
    version_number,
    legacy_username
) VALUES (
    gen_random_uuid(),
    {IHSSInsertGenerator.format_string(username)},
    {IHSSInsertGenerator.format_string(username)},
    {IHSSInsertGenerator.format_string(first_name)},
    {IHSSInsertGenerator.format_string(surname)},
    {IHSSInsertGenerator.format_string(full_name or f'{first_name} {surname}')},
    '{emp_status}'::ihss_org.employment_status_enum,
    {IHSSInsertGenerator.format_bool(account_enabled)},
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    {IHSSInsertGenerator.format_string(username)}
);
"""
            inserts.append(insert)
            
            mapping = f"INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT {IHSSInsertGenerator.format_string(username)}, staff_id FROM ihss_org.staff_account WHERE legacy_username = {IHSSInsertGenerator.format_string(username)};"
            inserts.append(mapping)
        
        return '\n'.join(inserts)
    
    @staticmethod
    def generate_case_queue_inserts(queues: List[Dict]) -> str:
        """Generate INSERT statements for ihss_org.case_queue from WORKQUEUE"""
        inserts = []
        inserts.append("\n-- ============================================================================")
        inserts.append("-- INSERT STATEMENTS FOR ihss_org.case_queue")
        inserts.append("-- Generated from WORKQUEUE.dmx")
        inserts.append("-- ============================================================================\n")
        
        for row in queues:
            queue_id = row.get('workQueueID', '')
            name = row.get('name', f'Queue {queue_id}')
            allow_subscription = row.get('allowUserSubscriptionInd', '1')
            sensitivity = row.get('sensitivity', '')
            comments = row.get('comments', '')
            
            insert = f"""
INSERT INTO ihss_org.case_queue (
    case_queue_id,
    queue_code,
    queue_name,
    allow_user_subscription,
    sensitivity,
    comments,
    is_active,
    created_by,
    created_at,
    version_number,
    legacy_workqueue_id
) VALUES (
    gen_random_uuid(),
    'QUEUE-{queue_id}',
    {IHSSInsertGenerator.format_string(name)},
    {IHSSInsertGenerator.format_bool(allow_subscription)},
    {IHSSInsertGenerator.format_string(sensitivity)},
    {IHSSInsertGenerator.format_string(comments)},
    TRUE,
    'MIGRATION',
    NOW(),
    {row.get('versionNo', '1')},
    {queue_id}
);
"""
            inserts.append(insert)
        
        return '\n'.join(inserts)


def main():
    """Main execution"""
    dmx_dir = '/tmp/initial'
    output_file = '/home/claude/initial_data_inserts.sql'
    
    print("Starting DMX to PostgreSQL INSERT generation...")
    
    with open(output_file, 'w') as f:
        f.write("-- ============================================================================\n")
        f.write("-- INITIAL DATA LOAD FOR IHSS MODERNIZED SCHEMA\n")
        f.write("-- Generated from Curam DMX files\n")
        f.write(f"-- Generated: {datetime.now().isoformat()}\n")
        f.write("-- ============================================================================\n\n")
        f.write("-- Prerequisites: Run ihss_postgresql_complete_ddl.sql first\n\n")
        f.write("BEGIN;\n\n")
        
        # Parse and generate org units
        print("Processing ORGANISATIONUNIT.dmx...")
        org_parser = DMXParser(os.path.join(dmx_dir, 'ORGANISATIONUNIT.dmx'))
        org_data = org_parser.parse().get_data()
        org_inserts = IHSSInsertGenerator.generate_program_unit_inserts(
            org_data['rows'], []
        )
        f.write(org_inserts)
        print(f"  Generated {len(org_data['rows'])} program_unit inserts")
        
        # Parse and generate jobs
        print("Processing JOB.dmx...")
        job_parser = DMXParser(os.path.join(dmx_dir, 'JOB.dmx'))
        job_data = job_parser.parse().get_data()
        job_inserts = IHSSInsertGenerator.generate_job_classification_inserts(
            job_data['rows']
        )
        f.write(job_inserts)
        print(f"  Generated {len(job_data['rows'])} job_classification inserts")
        
        # Parse and generate users
        print("Processing USERS.dmx...")
        user_parser = DMXParser(os.path.join(dmx_dir, 'USERS.dmx'))
        user_data = user_parser.parse().get_data()
        user_inserts = IHSSInsertGenerator.generate_staff_account_inserts(
            user_data['rows']
        )
        f.write(user_inserts)
        print(f"  Generated {len(user_data['rows'])} staff_account inserts")
        
        # Parse and generate queues
        print("Processing WORKQUEUE.dmx...")
        queue_parser = DMXParser(os.path.join(dmx_dir, 'WORKQUEUE.dmx'))
        queue_data = queue_parser.parse().get_data()
        queue_inserts = IHSSInsertGenerator.generate_case_queue_inserts(
            queue_data['rows']
        )
        f.write(queue_inserts)
        print(f"  Generated {len(queue_data['rows'])} case_queue inserts")
        
        # Commit
        f.write("\n\nCOMMIT;\n")
        f.write("\n-- ============================================================================\n")
        f.write("-- DATA LOAD COMPLETE\n")
        f.write("-- ============================================================================\n")
    
    print(f"\nINSERT statements written to: {output_file}")
    print("Success!")

if __name__ == '__main__':
    main()
