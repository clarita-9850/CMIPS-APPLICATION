-- ============================================================================
-- INITIAL DATA LOAD FOR IHSS MODERNIZED SCHEMA
-- Generated from Curam DMX files
-- Generated: 2026-02-11T15:59:18.263686
-- ============================================================================

-- Prerequisites: Run ihss_postgresql_complete_ddl.sql first

BEGIN;

-- ============================================================================
-- INSERT STATEMENTS FOR ihss_org.program_unit
-- Generated from ORGANISATIONUNIT.dmx
-- ============================================================================

-- Create temporary mapping table for org unit IDs
CREATE TEMP TABLE IF NOT EXISTS temp_org_unit_mapping (
    legacy_id BIGINT,
    new_uuid UUID
);


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
    'ORG-12',
    '50 Stanislaus',
    'COUNTY'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC100',
    NULL,
    NULL,
    '2000-01-01',
    'MIGRATION',
    '2009-04-20 09:55:11',
    3,
    12
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 12, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 12;

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
    'ORG-13',
    '50 01 District Office',
    'DISTRICT'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC102',
    NULL,
    NULL,
    '2000-01-01',
    'MIGRATION',
    '2009-04-20 09:55:11',
    3,
    13
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 13, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 13;

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
    'ORG-14',
    '50 17 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC104',
    NULL,
    NULL,
    '2000-01-01',
    'MIGRATION',
    '2009-04-20 09:55:11',
    3,
    14
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 14, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 14;

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
    'ORG-9047731651387326464',
    '37 01 Chula Vista',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    'DO1',
    NULL,
    '2009-04-20',
    'MIGRATION',
    '2009-04-20 09:55:11',
    4,
    9047731651387326464
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 9047731651387326464, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 9047731651387326464;

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
    'ORG-8',
    '14 Payroll',
    'DISTRICT'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC102',
    NULL,
    NULL,
    '2010-03-30',
    'MIGRATION',
    '2010-03-30 09:55:11',
    1,
    8
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 8, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 8;

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
    'ORG-9',
    '19 Payroll',
    'DISTRICT'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC102',
    NULL,
    NULL,
    '2010-03-30',
    'MIGRATION',
    '2010-03-30 09:55:11',
    1,
    9
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 9, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 9;

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
    'ORG-10',
    '38 Payroll',
    'DISTRICT'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC102',
    NULL,
    NULL,
    '2010-03-30',
    'MIGRATION',
    '2010-03-30 09:55:11',
    1,
    10
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 10, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 10;

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
    'ORG-11',
    '43 Payroll',
    'DISTRICT'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC102',
    NULL,
    NULL,
    '2010-03-30',
    'MIGRATION',
    '2010-03-30 09:55:11',
    1,
    11
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 11, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 11;

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
    'ORG-5730830525828956160',
    '37 01 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2009-07-06',
    'MIGRATION',
    '2009-07-06 09:55:11',
    2,
    5730830525828956160
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 5730830525828956160, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 5730830525828956160;

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
    'ORG-2202260217784172544',
    '37 04 El Cajon',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    'DO2',
    NULL,
    '2009-04-20',
    'MIGRATION',
    '2009-04-20 09:55:11',
    4,
    2202260217784172544
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 2202260217784172544, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 2202260217784172544;

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
    'ORG-4902168194392784896',
    '37 04 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2009-06-15',
    'MIGRATION',
    '2009-06-15 09:55:11',
    4,
    4902168194392784896
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 4902168194392784896, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 4902168194392784896;

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
    'ORG--123848989752688640',
    '37 05 Escondido',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    NULL,
    NULL,
    '2009-07-13',
    'MIGRATION',
    '2009-07-13 09:55:11',
    1,
    -123848989752688640
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -123848989752688640, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -123848989752688640;

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
    'ORG--6609032453166202880',
    '37 06 Oceanside',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    NULL,
    NULL,
    '2009-07-13',
    'MIGRATION',
    '2009-07-13 09:55:11',
    1,
    -6609032453166202880
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -6609032453166202880, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -6609032453166202880;

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
    'ORG--3222325533383589888',
    '37 07 Kearney Mesa',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    NULL,
    NULL,
    '2009-07-13',
    'MIGRATION',
    '2009-07-13 09:55:11',
    1,
    -3222325533383589888
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -3222325533383589888, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -3222325533383589888;

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
    'ORG--8194299522000617472',
    '37 08 Hazard',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    NULL,
    NULL,
    '2009-07-13',
    'MIGRATION',
    '2009-07-13 09:55:11',
    1,
    -8194299522000617472
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -8194299522000617472, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -8194299522000617472;

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
    'ORG--4861635797746450432',
    '37 Payroll',
    'DISTRICT'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC102',
    NULL,
    NULL,
    '2009-07-08',
    'MIGRATION',
    '2009-07-08 09:55:11',
    2,
    -4861635797746450432
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -4861635797746450432, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -4861635797746450432;

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
    'ORG--3348426322949963776',
    '37 Public Authority',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC106',
    NULL,
    NULL,
    '2009-07-02',
    'MIGRATION',
    '2009-07-02 09:55:11',
    2,
    -3348426322949963776
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -3348426322949963776, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -3348426322949963776;

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
    'ORG--538180155470774272',
    '37 Quality Assurance',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC105',
    NULL,
    NULL,
    '2009-07-02',
    'MIGRATION',
    '2009-07-02 09:55:11',
    2,
    -538180155470774272
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -538180155470774272, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -538180155470774272;

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
    'ORG-1553741871442821120',
    '37 San Diego',
    'COUNTY'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC100',
    'County1',
    NULL,
    '2009-04-20',
    'MIGRATION',
    '2009-04-20 09:55:11',
    3,
    1553741871442821120
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 1553741871442821120, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 1553741871442821120;

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
    'ORG-5516909543528857600',
    '57 01 District Office',
    'DISTRICT'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    NULL,
    NULL,
    '2009-04-20',
    'MIGRATION',
    '2009-04-20 09:55:11',
    3,
    5516909543528857600
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 5516909543528857600, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 5516909543528857600;

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
    'ORG--3303390326676258816',
    '57 01 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2009-07-14',
    'MIGRATION',
    '2009-07-14 09:55:11',
    1,
    -3303390326676258816
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -3303390326676258816, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -3303390326676258816;

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
    'ORG-4003700068732370944',
    '57 02 District Office',
    'DISTRICT'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    NULL,
    NULL,
    '2009-04-20',
    'MIGRATION',
    '2009-04-20 09:55:11',
    3,
    4003700068732370944
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 4003700068732370944, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 4003700068732370944;

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
    'ORG--1294784892869017600',
    '57 02 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2009-06-08',
    'MIGRATION',
    '2009-06-08 09:55:11',
    3,
    -1294784892869017600
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -1294784892869017600, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -1294784892869017600;

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
    'ORG--6257751682231304192',
    '57 Payroll',
    'DISTRICT'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC102',
    NULL,
    NULL,
    '2009-07-14',
    'MIGRATION',
    '2009-07-14 09:55:11',
    1,
    -6257751682231304192
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -6257751682231304192, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -6257751682231304192;

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
    'ORG-4046484265192390656',
    '57 Public Authority',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC106',
    NULL,
    NULL,
    '2009-07-14',
    'MIGRATION',
    '2009-07-14 09:55:11',
    1,
    4046484265192390656
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 4046484265192390656, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 4046484265192390656;

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
    'ORG-2533274790395904000',
    '57 Quality Assurance',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC105',
    NULL,
    NULL,
    '2009-07-14',
    'MIGRATION',
    '2009-07-14 09:55:11',
    1,
    2533274790395904000
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 2533274790395904000, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 2533274790395904000;

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
    'ORG-5084563979301289984',
    '57 Yolo',
    'COUNTY'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC100',
    NULL,
    NULL,
    '2009-04-20',
    'MIGRATION',
    '2009-04-20 09:55:11',
    4,
    5084563979301289984
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 5084563979301289984, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 5084563979301289984;

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
    'ORG--2582814386296979456',
    'AP Fiscal Admin & Systems Bureau',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC104',
    NULL,
    NULL,
    '2009-07-16',
    'MIGRATION',
    '2009-07-16 09:55:11',
    1,
    -2582814386296979456
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -2582814386296979456, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -2582814386296979456;

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
    'ORG-884957326778302464',
    'CDSS Adult Programs Branch',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC104',
    NULL,
    NULL,
    '2009-07-13',
    'MIGRATION',
    '2009-07-13 09:55:11',
    2,
    884957326778302464
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 884957326778302464, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 884957326778302464;

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
    'ORG-2317102008282120192',
    'Fiscal and Admin Unit',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC104',
    NULL,
    NULL,
    '2009-07-16',
    'MIGRATION',
    '2009-07-16 09:55:11',
    1,
    2317102008282120192
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 2317102008282120192, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 2317102008282120192;

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
    'ORG-3830311483078606848',
    'Systems Unit',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC104',
    NULL,
    NULL,
    '2009-07-16',
    'MIGRATION',
    '2009-07-16 09:55:11',
    1,
    3830311483078606848
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 3830311483078606848, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 3830311483078606848;

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
    'ORG-1',
    'CDSS',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC104',
    NULL,
    NULL,
    '2003-01-01',
    'MIGRATION',
    '2003-01-01 09:55:11',
    1,
    1
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 1, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 1;

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
    'ORG-2',
    'CMIPS Customer Services and Technical Support',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC104',
    NULL,
    NULL,
    '2003-01-01',
    'MIGRATION',
    '2003-01-01 09:55:11',
    1,
    2
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 2, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 2;

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
    'ORG-3',
    'WPCS',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC108',
    NULL,
    NULL,
    '2003-01-01',
    'MIGRATION',
    '2003-01-01 09:55:11',
    1,
    3
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 3, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 3;

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
    'ORG-2533274790393322111',
    '57 Homemaker Unit',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC109',
    NULL,
    NULL,
    '2009-10-29',
    'MIGRATION',
    '2003-10-29 09:55:11',
    1,
    2533274790393322111
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 2533274790393322111, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 2533274790393322111;

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
    'ORG-8196551321814302720',
    'Operations and QA',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC104',
    NULL,
    NULL,
    '2009-12-02',
    'admin',
    '2009-12-02 09:55:11',
    3,
    8196551321814302720
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 8196551321814302720, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 8196551321814302720;

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
    'ORG--6232981884280766464',
    '37 Homemaker Unit',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC109',
    NULL,
    NULL,
    '2009-12-17',
    'admin',
    '2009-12-17 11:21:18',
    2,
    -6232981884280766464
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -6232981884280766464, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -6232981884280766464;

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
    'ORG--3332663724254167040',
    '14 Inyo',
    'COUNTY'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC100',
    NULL,
    NULL,
    '2010-01-19',
    'admin',
    '2010-01-19 14:00:26',
    2,
    -3332663724254167040
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -3332663724254167040, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -3332663724254167040;

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
    'ORG-9133300044307365888',
    '14 01 Main',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    'district office',
    NULL,
    '2010-01-19',
    'admin',
    '2010-01-19 14:13:59',
    2,
    9133300044307365888
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 9133300044307365888, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 9133300044307365888;

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
    'ORG-6611284252979888128',
    '14 01 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2010-01-19',
    'admin',
    '2010-01-19 14:17:26',
    2,
    6611284252979888128
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 6611284252979888128, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 6611284252979888128;

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
    'ORG-4791830003522207744',
    '19 Los Angeles',
    'COUNTY'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC100',
    NULL,
    NULL,
    '2010-03-01',
    'admin',
    '2010-03-01 16:08:26',
    1,
    4791830003522207744
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 4791830003522207744, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 4791830003522207744;

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
    'ORG-1116892707587883008',
    '19 01 Chatsworth',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    NULL,
    NULL,
    '2010-03-01',
    'admin',
    '2010-03-01 16:30:49',
    1,
    1116892707587883008
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 1116892707587883008, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 1116892707587883008;

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
    'ORG--1342072688956407808',
    '19 01 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 17:47:04',
    1,
    -1342072688956407808
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -1342072688956407808, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -1342072688956407808;

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
    'ORG--4449556431842050048',
    '19 19 Pomona',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 16:39:32',
    1,
    -4449556431842050048
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -4449556431842050048, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -4449556431842050048;

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
    'ORG--477381560501272576',
    '19 19 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 17:48:37',
    1,
    -477381560501272576
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -477381560501272576, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -477381560501272576;

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
    'ORG--6034823500676464640',
    '19 35 Lancaster',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 16:39:59',
    1,
    -6034823500676464640
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -6034823500676464640, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -6034823500676464640;

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
    'ORG--2206763817411543040',
    '19 35 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 17:50:09',
    1,
    -2206763817411543040
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -2206763817411543040, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -2206763817411543040;

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
    'ORG--1999598234552500224',
    '19 47 Metro',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 16:40:15',
    1,
    -1999598234552500224
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -1999598234552500224, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -1999598234552500224;

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
    'ORG--693554342615056384',
    '19 47 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 17:51:57',
    1,
    -693554342615056384
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -693554342615056384, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -693554342615056384;

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
    'ORG--7475975381435023360',
    '19 73 Burbank',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 16:40:31',
    1,
    -7475975381435023360
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -7475975381435023360, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -7475975381435023360;

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
    'ORG--1558245471070191616',
    '19 73 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 17:53:37',
    1,
    -1558245471070191616
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -1558245471070191616, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -1558245471070191616;

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
    'ORG--3440750115311058944',
    '19 74 El Monte',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 16:40:50',
    1,
    -3440750115311058944
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -3440750115311058944, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -3440750115311058944;

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
    'ORG--261208778387488768',
    '19 74 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 17:55:14',
    1,
    -261208778387488768
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -261208778387488768, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -261208778387488768;

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
    'ORG--4665729213955833856',
    '19 75 Rancho Park',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 16:41:12',
    1,
    -4665729213955833856
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -4665729213955833856, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -4665729213955833856;

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
    'ORG--1990591035297759232',
    '19 75 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 17:56:33',
    1,
    -1990591035297759232
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -1990591035297759232, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -1990591035297759232;

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
    'ORG--630503947831869440',
    '19 77 Hawthorne',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 16:41:32',
    1,
    -630503947831869440
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -630503947831869440, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -630503947831869440;

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
    'ORG--1125899906842624000',
    '19 77 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 17:58:05',
    1,
    -1125899906842624000
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -1125899906842624000, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -1125899906842624000;

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
    'ORG-1837468647967162368',
    '24 Merced',
    'COUNTY'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC100',
    NULL,
    NULL,
    '2010-03-01',
    'admin',
    '2010-03-01 16:16:53',
    1,
    1837468647967162368
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 1837468647967162368, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 1837468647967162368;

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
    'ORG--6287025079809212416',
    '24 01 Main',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 16:51:48',
    1,
    -6287025079809212416
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -6287025079809212416, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -6287025079809212416;

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
    'ORG--1720375057655529472',
    '24 01 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 17:13:21',
    1,
    -1720375057655529472
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -1720375057655529472, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -1720375057655529472;

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
    'ORG-8610882487532388352',
    '38 San Francisco',
    'COUNTY'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC100',
    NULL,
    NULL,
    '2010-03-01',
    'admin',
    '2010-03-01 16:17:19',
    1,
    8610882487532388352
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 8610882487532388352, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 8610882487532388352;

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
    'ORG-7322852994104426496',
    '38 01',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 17:28:25',
    1,
    7322852994104426496
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 7322852994104426496, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 7322852994104426496;

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
    'ORG-6674334647763075072',
    '38 01 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 17:29:19',
    1,
    6674334647763075072
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 6674334647763075072, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 6674334647763075072;

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
    'ORG-1774418253183975424',
    '38 12',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 17:30:29',
    1,
    1774418253183975424
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 1774418253183975424, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 1774418253183975424;

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
    'ORG-3864088480283885568',
    '38 12 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 17:31:14',
    1,
    3864088480283885568
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 3864088480283885568, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 3864088480283885568;

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
    'ORG-5881701113345867776',
    '38 15',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 17:32:27',
    1,
    5881701113345867776
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 5881701113345867776, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 5881701113345867776;

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
    'ORG-8691947280825057280',
    '38 15 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 17:33:20',
    1,
    8691947280825057280
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 8691947280825057280, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 8691947280825057280;

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
    'ORG-4584664420663164928',
    '38 22',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 17:34:22',
    1,
    4584664420663164928
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 4584664420663164928, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 4584664420663164928;

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
    'ORG-693554342615056384',
    '38 22 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 17:35:04',
    1,
    693554342615056384
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 693554342615056384, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 693554342615056384;

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
    'ORG-2485986994308513792',
    '43 Santa Clara',
    'COUNTY'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC100',
    NULL,
    NULL,
    '2010-03-01',
    'admin',
    '2010-03-01 16:17:45',
    1,
    2485986994308513792
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 2485986994308513792, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 2485986994308513792;

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
    'ORG-3215570133942534144',
    '43 01 Main',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 17:26:06',
    1,
    3215570133942534144
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 3215570133942534144, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 3215570133942534144;

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
    'ORG-1414130282994335744',
    '43 01 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 17:27:00',
    1,
    1414130282994335744
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 1414130282994335744, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 1414130282994335744;

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
    'ORG--3161526938414088192',
    '37 05 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 17:07:00',
    1,
    -3161526938414088192
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -3161526938414088192, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -3161526938414088192;

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
    'ORG--6620291452234629120',
    '37 08 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 17:09:53',
    1,
    -6620291452234629120
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -6620291452234629120, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -6620291452234629120;

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
    'ORG--3260606130216239104',
    '37 10',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 16:58:32',
    1,
    -3260606130216239104
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -3260606130216239104, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -3260606130216239104;

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
    'ORG--567453553048682496',
    '37 10 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2010-03-01',
    'unauthenticated',
    '2010-03-01 17:11:12',
    1,
    -567453553048682496
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT -567453553048682496, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = -567453553048682496;

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
    'ORG-4',
    '31 Placer',
    'COUNTY'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC100',
    'County1',
    NULL,
    '2010-03-26',
    'MIGRATION',
    '2010-03-26 09:55:11',
    1,
    4
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 4, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 4;

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
    'ORG-5',
    '31 01 Some DistrictOffice',
    'DISTRICT'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC101',
    'DO1',
    NULL,
    '2010-03-26',
    'MIGRATION',
    '2010-03-26 09:55:11',
    1,
    5
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 5, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 5;

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
    'ORG-6',
    '31 01 Unit 1',
    'FIELD_OFFICE'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC103',
    NULL,
    NULL,
    '2010-03-26',
    'MIGRATION',
    '2010-03-26 09:55:11',
    2,
    6
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 6, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 6;

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
    'ORG-7',
    '24 Payroll',
    'DISTRICT'::ihss_org.unit_type_enum,
    'ACTIVE'::ihss_org.unit_status_enum,
    'OUBTC102',
    NULL,
    NULL,
    '2010-03-29',
    'MIGRATION',
    '2010-03-29 09:55:11',
    1,
    7
);

INSERT INTO temp_org_unit_mapping (legacy_id, new_uuid) SELECT 7, program_unit_id FROM ihss_org.program_unit WHERE legacy_org_unit_id = 7;
-- ============================================================================
-- INSERT STATEMENTS FOR ihss_org.job_classification
-- Generated from JOB.dmx
-- ============================================================================

CREATE TEMP TABLE IF NOT EXISTS temp_job_mapping (
    legacy_id BIGINT,
    new_uuid UUID
);


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
    'JOB-1',
    'Executive',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    1
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT 1, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 1;

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
    'JOB-2',
    'Division Manager',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    2
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT 2, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 2;

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
    'JOB-3',
    'Board Member',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    3
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT 3, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3;

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
    'JOB-5',
    'Secretary',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    5
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT 5, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 5;

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
    'JOB-6',
    'Claims Analyst',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    6
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT 6, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 6;

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
    'JOB-10',
    'Hearing Official',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    10
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT 10, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 10;

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
    'JOB-11',
    'Hearing Reviewer',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    11
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT 11, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 11;

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
    'JOB-12',
    'Hearing Scheduler',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    12
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT 12, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 12;

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
    'JOB-13',
    'New User',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    13
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT 13, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 13;

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
    'JOB-4794081803335892992',
    'Chief',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    4794081803335892992
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT 4794081803335892992, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 4794081803335892992;

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
    'JOB--1763159254115549184',
    'Program Manager',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -1763159254115549184
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT -1763159254115549184, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -1763159254115549184;

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
    'JOB-3569102704691118080',
    'Manager',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    3569102704691118080
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT 3569102704691118080, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080;

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
    'JOB--8320400311566991360',
    'Supervisor',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -8320400311566991360
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT -8320400311566991360, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360;

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
    'JOB-7748443158890938368',
    'Case Worker',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    7748443158890938368
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT 7748443158890938368, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368;

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
    'JOB--2411677600456900608',
    'Analyst',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -2411677600456900608
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT -2411677600456900608, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -2411677600456900608;

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
    'JOB-326510972984360960',
    'Homemaker',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    326510972984360960
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT 326510972984360960, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 326510972984360960;

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
    'JOB--6663075648694648832',
    'Homemaker Supervisor',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -6663075648694648832
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT -6663075648694648832, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -6663075648694648832;

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
    'JOB-6235233684094451712',
    'Quality Assurance',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    6235233684094451712
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT 6235233684094451712, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 6235233684094451712;

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
    'JOB--1114640907774197760',
    'Security Admin',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -1114640907774197760
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT -1114640907774197760, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -1114640907774197760;

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
    'JOB-3280872328539406336',
    'Public Authority',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    3280872328539406336
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT 3280872328539406336, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3280872328539406336;

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
    'JOB--7311593995036000256',
    'IHO',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -7311593995036000256
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT -7311593995036000256, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -7311593995036000256;

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
    'JOB-9189595039649497088',
    'Fiscal Accounting',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    9189595039649497088
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT 9189595039649497088, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 9189595039649497088;

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
    'JOB--7671881965225639936',
    'County Contract Coordinator',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -7671881965225639936
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT -7671881965225639936, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -7671881965225639936;

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
    'JOB-5802888119866884096',
    'Unit Supervisor',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    5802888119866884096
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT 5802888119866884096, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 5802888119866884096;

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
    'JOB-14',
    'View Only',
    NULL,
    NULL,
    NULL,
    'MIGRATION',
    NOW(),
    1,
    14
);

INSERT INTO temp_job_mapping (legacy_id, new_uuid) SELECT 14, job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 14;
-- ============================================================================
-- INSERT STATEMENTS FOR ihss_org.staff_account
-- Generated from USERS.dmx
-- ============================================================================

CREATE TEMP TABLE IF NOT EXISTS temp_staff_mapping (
    legacy_username VARCHAR(120),
    new_uuid UUID
);


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
    'DBTOJMS',
    'DBTOJMS',
    NULL,
    'DBTOJMS',
    'DBTOJMS',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'DBTOJMS'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'DBTOJMS', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'DBTOJMS';

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
    'SYSTEM',
    'SYSTEM',
    NULL,
    'SYSTEM',
    'SYSTEM',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'SYSTEM'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'SYSTEM', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'SYSTEM';

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
    'WEBSVCS',
    'WEBSVCS',
    'WEBSVCS',
    'WEBSVCS',
    'WEBSVCS',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'WEBSVCS'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'WEBSVCS', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'WEBSVCS';

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
    'admin',
    'admin',
    'ADMINISTRATION',
    'USER',
    'ADMINISTRATION USER',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'admin'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'admin', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'admin';

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
    'advancepayworker',
    'advancepayworker',
    'AdvancePay',
    'Worker',
    'AdvancePay Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'advancepayworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'advancepayworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'advancepayworker';

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
    'caseloadassignmentworker',
    'caseloadassignmentworker',
    'CaseloadAssignment',
    'Worker',
    'CaseloadAssignment Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseloadassignmentworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseloadassignmentworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseloadassignmentworker';

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
    'casesupervisor',
    'casesupervisor',
    'Case',
    'Supervisor',
    'Case Supervisor',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor';

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
    'casesupervisor14',
    'casesupervisor14',
    'Case',
    'Supervisor14',
    'Case Supervisor14',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor14'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor14', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor14';

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
    'casesupervisor1401',
    'casesupervisor1401',
    'Case',
    'Supervisor1401',
    'Case Supervisor1401',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor1401'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor1401', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor1401';

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
    'casesupervisor1901',
    'casesupervisor1901',
    'Case',
    'Supervisor1901',
    'Case Supervisor1901',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor1901'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor1901', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor1901';

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
    'casesupervisor1919',
    'casesupervisor1919',
    'Case',
    'Supervisor1919',
    'Case Supervisor1919',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor1919'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor1919', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor1919';

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
    'casesupervisor1935',
    'casesupervisor1935',
    'Case',
    'Supervisor1935',
    'Case Supervisor1935',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor1935'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor1935', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor1935';

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
    'casesupervisor1947',
    'casesupervisor1947',
    'Case',
    'Supervisor1947',
    'Case Supervisor1947',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor1947'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor1947', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor1947';

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
    'casesupervisor1973',
    'casesupervisor1973',
    'Case',
    'Supervisor1973',
    'Case Supervisor1973',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor1973'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor1973', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor1973';

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
    'casesupervisor1974',
    'casesupervisor1974',
    'Case',
    'Supervisor1974',
    'Case Supervisor1974',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor1974'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor1974', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor1974';

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
    'casesupervisor1975',
    'casesupervisor1975',
    'Case',
    'Supervisor1975',
    'Case Supervisor1975',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor1975'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor1975', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor1975';

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
    'casesupervisor1977',
    'casesupervisor1977',
    'Case',
    'Supervisor1977',
    'Case Supervisor1977',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor1977'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor1977', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor1977';

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
    'casesupervisor2401',
    'casesupervisor2401',
    'Case',
    'Supervisor2401',
    'Case Supervisor2401',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor2401'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor2401', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor2401';

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
    'casesupervisor3701',
    'casesupervisor3701',
    'Case',
    'Supervisor3701',
    'Case Supervisor3701',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor3701'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor3701', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor3701';

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
    'casesupervisor3705',
    'casesupervisor3705',
    'Case',
    'Supervisor3705',
    'Case Supervisor3705',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor3705'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor3705', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor3705';

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
    'casesupervisor3708',
    'casesupervisor3708',
    'Case',
    'Supervisor3708',
    'Case Supervisor3708',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor3708'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor3708', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor3708';

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
    'casesupervisor3710',
    'casesupervisor3710',
    'Case',
    'Supervisor3710',
    'Case Supervisor3710',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor3710'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor3710', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor3710';

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
    'casesupervisor3801',
    'casesupervisor3801',
    'Case',
    'Supervisor3801',
    'Case Supervisor3801',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor3801'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor3801', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor3801';

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
    'casesupervisor3812',
    'casesupervisor3812',
    'Case',
    'Supervisor3812',
    'Case Supervisor3812',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor3812'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor3812', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor3812';

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
    'casesupervisor3815',
    'casesupervisor3815',
    'Case',
    'Supervisor3815',
    'Case Supervisor3815',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor3815'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor3815', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor3815';

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
    'casesupervisor3822',
    'casesupervisor3822',
    'Case',
    'Supervisor3822',
    'Case Supervisor3822',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor3822'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor3822', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor3822';

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
    'casesupervisor4301',
    'casesupervisor4301',
    'Case',
    'Supervisor4301',
    'Case Supervisor4301',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor4301'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor4301', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor4301';

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
    'casesupervisor5017',
    'casesupervisor5017',
    'Case',
    'Supervisor5017',
    'Case Supervisor5017',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor5017'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor5017', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor5017';

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
    'casesupervisor57',
    'casesupervisor57',
    'Case',
    'Supervisor57',
    'Case Supervisor57',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor57'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor57', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor57';

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
    'casesupervisor5701',
    'casesupervisor5701',
    'Case',
    'Supervisor5701',
    'Case Supervisor5701',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor5701'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor5701', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor5701';

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
    'casesupervisor5702',
    'casesupervisor5702',
    'Case',
    'Supervisor5702',
    'Case Supervisor5702',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'casesupervisor5702'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'casesupervisor5702', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'casesupervisor5702';

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
    'caseworker',
    'caseworker',
    'CASE',
    'WORKER',
    'CASE WORKER',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker';

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
    'caseworker14',
    'caseworker14',
    'CASE',
    'WORKER14',
    'CASE WORKER14',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker14'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker14', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker14';

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
    'caseworker1401',
    'caseworker1401',
    'CASE',
    'WORKER1401',
    'CASE WORKER1401',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker1401'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker1401', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker1401';

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
    'caseworker1411',
    'caseworker1411',
    'CASE',
    'WORKER1411',
    'CASE WORKER1411',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker1411'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker1411', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker1411';

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
    'caseworker1421',
    'caseworker1421',
    'CASE',
    'WORKER1421',
    'CASE WORKER1421',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker1421'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker1421', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker1421';

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
    'caseworker1431',
    'caseworker1431',
    'CASE',
    'WORKER1431',
    'CASE WORKER1431',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker1431'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker1431', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker1431';

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
    'caseworker1901',
    'caseworker1901',
    'CASE',
    'WORKER1901',
    'CASE WORKER1901',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker1901'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker1901', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker1901';

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
    'caseworker1919',
    'caseworker1919',
    'CASE',
    'WORKER1919',
    'CASE WORKER1919',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker1919'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker1919', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker1919';

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
    'caseworker1935',
    'caseworker1935',
    'CASE',
    'WORKER1935',
    'CASE WORKER1935',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker1935'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker1935', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker1935';

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
    'caseworker1947',
    'caseworker1947',
    'CASE',
    'WORKER1947',
    'CASE WORKER1947',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker1947'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker1947', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker1947';

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
    'caseworker1973',
    'caseworker1973',
    'CASE',
    'WORKER1973',
    'CASE WORKER1973',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker1973'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker1973', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker1973';

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
    'caseworker1974',
    'caseworker1974',
    'CASE',
    'WORKER1974',
    'CASE WORKER1974',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker1974'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker1974', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker1974';

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
    'caseworker1975',
    'caseworker1975',
    'CASE',
    'WORKER1975',
    'CASE WORKER1975',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker1975'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker1975', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker1975';

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
    'caseworker1977',
    'caseworker1977',
    'CASE',
    'WORKER1977',
    'CASE WORKER1977',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker1977'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker1977', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker1977';

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
    'caseworker2401',
    'caseworker2401',
    'CASE',
    'WORKER2401',
    'CASE WORKER2401',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker2401'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker2401', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker2401';

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
    'caseworker31',
    'caseworker31',
    'CASE',
    'WORKER31',
    'CASE WORKER31',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker31'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker31', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker31';

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
    'caseworker3701',
    'caseworker3701',
    'CASE',
    'WORKER3701',
    'CASE WORKER3701',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker3701'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker3701', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker3701';

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
    'caseworker3705',
    'caseworker3705',
    'CASE',
    'WORKER3705',
    'CASE WORKER3705',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker3705'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker3705', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker3705';

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
    'caseworker3708',
    'caseworker3708',
    'CASE',
    'WORKER3708',
    'CASE WORKER3708',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker3708'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker3708', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker3708';

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
    'caseworker3710',
    'caseworker3710',
    'CASE',
    'WORKER3710',
    'CASE WORKER3710',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker3710'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker3710', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker3710';

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
    'caseworker3801',
    'caseworker3801',
    'CASE',
    'WORKER3801',
    'CASE WORKER3801',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker3801'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker3801', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker3801';

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
    'caseworker3812',
    'caseworker3812',
    'CASE',
    'WORKER3812',
    'CASE WORKER3812',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker3812'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker3812', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker3812';

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
    'caseworker3815',
    'caseworker3815',
    'CASE',
    'WORKER3815',
    'CASE WORKER3815',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker3815'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker3815', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker3815';

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
    'caseworker3822',
    'caseworker3822',
    'CASE',
    'WORKER3822',
    'CASE WORKER3822',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker3822'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker3822', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker3822';

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
    'caseworker4301',
    'caseworker4301',
    'CASE',
    'WORKER4301',
    'CASE WORKER4301',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker4301'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker4301', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker4301';

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
    'caseworker5017',
    'caseworker5017',
    'CASE',
    'WORKER5017',
    'CASE WORKER5017',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker5017'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker5017', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker5017';

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
    'caseworker57',
    'caseworker57',
    'CASE',
    'WORKER57',
    'CASE WORKER57',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker57'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker57', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker57';

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
    'caseworker5701',
    'caseworker5701',
    'CASE',
    'WORKER5701',
    'CASE WORKER5701',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker5701'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker5701', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker5701';

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
    'caseworker5702',
    'caseworker5702',
    'CASE',
    'WORKER5702',
    'CASE WORKER5702',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'caseworker5702'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'caseworker5702', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'caseworker5702';

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
    'ccpayrollworker',
    'ccpayrollworker',
    'CrossCountyPayroll',
    'Worker',
    'CrossCountyPayroll Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'ccpayrollworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'ccpayrollworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'ccpayrollworker';

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
    'ccpayrollworker2',
    'ccpayrollworker2',
    'CrossCountyPayroll',
    'Worker2',
    'CrossCountyPayroll Worker2',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'ccpayrollworker2'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'ccpayrollworker2', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'ccpayrollworker2';

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
    'ccpayrollworker37',
    'ccpayrollworker37',
    'CrossCountyPayroll',
    'Worker37',
    'CrossCountyPayroll Worker37',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'ccpayrollworker37'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'ccpayrollworker37', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'ccpayrollworker37';

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
    'ccworker',
    'ccworker',
    'CountyContractor',
    'Worker',
    'CountyContractor Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'ccworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'ccworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'ccworker';

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
    'cdssbuyoutworker',
    'cdssbuyoutworker',
    'CDSS',
    'BuyoutWorker',
    'CDSS BuyoutWorker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'cdssbuyoutworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'cdssbuyoutworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'cdssbuyoutworker';

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
    'cdssfiscalworker',
    'cdssfiscalworker',
    'CDSSFiscal',
    'Worker',
    'CDSSFiscal Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'cdssfiscalworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'cdssfiscalworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'cdssfiscalworker';

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
    'cdssqaworker',
    'cdssqaworker',
    'CDSS',
    'QAWorker',
    'CDSS QAWorker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'cdssqaworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'cdssqaworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'cdssqaworker';

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
    'cdsssysadmin',
    'cdsssysadmin',
    'CDSS',
    'SYSADMIN',
    'CDSS SYSADMIN',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'cdsssysadmin'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'cdsssysadmin', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'cdsssysadmin';

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
    'cdssworker',
    'cdssworker',
    'CDSS',
    'Worker',
    'CDSS Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'cdssworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'cdssworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'cdssworker';

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
    'cmictworker',
    'cmictworker',
    'CMICT',
    'Worker',
    'CMICT Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'cmictworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'cmictworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'cmictworker';

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
    'cmpayrollworker',
    'cmpayrollworker',
    'CMPayroll',
    'Worker',
    'CMPayroll Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'cmpayrollworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'cmpayrollworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'cmpayrollworker';

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
    'cmpmpayrollworker',
    'cmpmpayrollworker',
    'CMPMPayroll',
    'Worker',
    'CMPMPayroll Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'cmpmpayrollworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'cmpmpayrollworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'cmpmpayrollworker';

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
    'cmpmworker',
    'cmpmworker',
    'CMPM',
    'Worker',
    'CMPM Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'cmpmworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'cmpmworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'cmpmworker';

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
    'cmshworker',
    'cmshworker',
    'CMSH',
    'Worker',
    'CMSH Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'cmshworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'cmshworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'cmshworker';

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
    'cmsshworker',
    'cmsshworker',
    'CMSSH',
    'Worker',
    'CMSSH Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'cmsshworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'cmsshworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'cmsshworker';

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
    'collectionworker',
    'collectionworker',
    'Collection',
    'Worker',
    'Collection Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'collectionworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'collectionworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'collectionworker';

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
    'convUser',
    'convUser',
    'conv',
    'USER',
    'convUser',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'convUser'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'convUser', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'convUser';

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
    'ctysysadmin',
    'ctysysadmin',
    'COUNTY',
    'SYSADMIN',
    'COUNTY SYSADMIN',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'ctysysadmin'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'ctysysadmin', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'ctysysadmin';

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
    'fundmanager',
    'fundmanager',
    'FUNDMANAGER',
    'USER',
    'FUND MANAGER USER',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'fundmanager'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'fundmanager', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'fundmanager';

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
    'helpdeskworker',
    'helpdeskworker',
    'helpdesk',
    'HELPDESK',
    'helpdesk worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'helpdeskworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'helpdeskworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'helpdeskworker';

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
    'hmsupervisor37',
    'hmsupervisor37',
    'Homemaker',
    'Supervisor37',
    'Homemaker Supervisor37',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'hmsupervisor37'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'hmsupervisor37', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'hmsupervisor37';

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
    'hmsupervisor57',
    'hmsupervisor57',
    'Homemaker',
    'Supervisor57',
    'Homemaker Supervisor57',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'hmsupervisor57'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'hmsupervisor57', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'hmsupervisor57';

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
    'hmworker37',
    'hmworker37',
    'Homemaker',
    'Worker37',
    'Homemaker Worker37',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'hmworker37'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'hmworker37', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'hmworker37';

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
    'hmworker57',
    'hmworker57',
    'Homemaker',
    'Worker57',
    'Homemaker Worker57',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'hmworker57'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'hmworker57', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'hmworker57';

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
    'newcaseworker',
    'newcaseworker',
    'New',
    'Caseworker',
    'New Caseworker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'newcaseworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'newcaseworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'newcaseworker';

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
    'overpaymentworker',
    'overpaymentworker',
    'Overpayment',
    'Worker',
    'Overpayment Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'overpaymentworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'overpaymentworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'overpaymentworker';

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
    'paworker',
    'paworker',
    'Pa',
    'Worker',
    'Pa Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'paworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'paworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'paworker';

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
    'payrollapprover14',
    'payrollapprover14',
    'Payroll',
    'Approver14',
    'Payroll Approver14',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'payrollapprover14'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'payrollapprover14', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'payrollapprover14';

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
    'payrollapprover19',
    'payrollapprover19',
    'Payroll',
    'Approver19',
    'Payroll Approver19',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'payrollapprover19'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'payrollapprover19', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'payrollapprover19';

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
    'payrollapprover24',
    'payrollapprover24',
    'Payroll',
    'Approver24',
    'Payroll Approver24',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'payrollapprover24'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'payrollapprover24', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'payrollapprover24';

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
    'payrollapprover37',
    'payrollapprover37',
    'Payroll',
    'Approver37',
    'Payroll Approver37',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'payrollapprover37'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'payrollapprover37', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'payrollapprover37';

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
    'payrollapprover38',
    'payrollapprover38',
    'Payroll',
    'Approver38',
    'Payroll Approver38',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'payrollapprover38'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'payrollapprover38', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'payrollapprover38';

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
    'payrollapprover43',
    'payrollapprover43',
    'Payroll',
    'Approver43',
    'Payroll Approver43',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'payrollapprover43'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'payrollapprover43', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'payrollapprover43';

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
    'payrollapprover57',
    'payrollapprover57',
    'Payroll',
    'Approver57',
    'Payroll Approver57',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'payrollapprover57'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'payrollapprover57', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'payrollapprover57';

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
    'payrollctyfiscalworker',
    'payrollctyfiscalworker',
    'PayrollCountyFiscal',
    'Worker',
    'PayrollCountyFiscal Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'payrollctyfiscalworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'payrollctyfiscalworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'payrollctyfiscalworker';

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
    'payrollworker',
    'payrollworker',
    'Payroll',
    'Worker',
    'Payroll Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'payrollworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'payrollworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'payrollworker';

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
    'pmpbworker',
    'pmpbworker',
    'PMPB',
    'Worker',
    'PMPB Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'pmpbworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'pmpbworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'pmpbworker';

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
    'pmpmworker',
    'pmpmworker',
    'PMPM',
    'Worker',
    'PMPM Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'pmpmworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'pmpmworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'pmpmworker';

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
    'providerbenefitsworker',
    'providerbenefitsworker',
    'Provider',
    'Benefitsworker',
    'Provider Benefitsworker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'providerbenefitsworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'providerbenefitsworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'providerbenefitsworker';

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
    'providermgmtworker',
    'providermgmtworker',
    'ProviderManagement',
    'Worker',
    'ProviderManagement Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'providermgmtworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'providermgmtworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'providermgmtworker';

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
    'qaworker',
    'qaworker',
    'QA',
    'Worker',
    'QA Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'qaworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'qaworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'qaworker';

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
    'referralintakeworker',
    'referralintakeworker',
    'ReferralIntake',
    'Worker',
    'ReferralIntake Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'referralintakeworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'referralintakeworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'referralintakeworker';

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
    'referralworker',
    'referralworker',
    'Referral',
    'Worker',
    'Referral Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'referralworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'referralworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'referralworker';

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
    'specialtransactionworker',
    'specialtransactionworker',
    'SpecialTransaction',
    'Worker',
    'SpecialTransaction Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'specialtransactionworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'specialtransactionworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'specialtransactionworker';

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
    'statehearingworker',
    'statehearingworker',
    'StateHearing',
    'Worker',
    'StateHearing Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'statehearingworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'statehearingworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'statehearingworker';

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
    'superuser',
    'superuser',
    'SUPER',
    'USER',
    'SUPER USER',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'superuser'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'superuser', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'superuser';

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
    'supervisor',
    'supervisor',
    'NoFirstName',
    'Supervisor',
    'NoFirstName Supervisor',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'supervisor'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'supervisor', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'supervisor';

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
    'sysadmin',
    'sysadmin',
    'SYSADMIN',
    'USER',
    'SYSADMIN USER',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'sysadmin'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'sysadmin', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'sysadmin';

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
    'tester-lead',
    'tester-lead',
    'TESTER',
    'LEAD',
    'TESTER LEAD',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester-lead'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester-lead', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester-lead';

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
    'tester1',
    'tester1',
    'Tester',
    '1',
    'Tester 1',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester1'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester1', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester1';

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
    'tester10',
    'tester10',
    'Tester',
    '10',
    'Tester 10',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester10'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester10', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester10';

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
    'tester11',
    'tester11',
    'Tester',
    '11',
    'Tester 11',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester11'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester11', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester11';

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
    'tester12',
    'tester12',
    'Tester',
    '12',
    'Tester 12',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester12'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester12', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester12';

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
    'tester13',
    'tester13',
    'Tester',
    '13',
    'Tester 13',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester13'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester13', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester13';

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
    'tester14',
    'tester14',
    'Tester',
    '14',
    'Tester 14',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester14'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester14', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester14';

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
    'tester15',
    'tester15',
    'Tester',
    '15',
    'Tester 15',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester15'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester15', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester15';

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
    'tester16',
    'tester16',
    'Tester',
    '16',
    'Tester 16',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester16'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester16', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester16';

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
    'tester17',
    'tester17',
    'Tester',
    '17',
    'Tester 17',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester17'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester17', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester17';

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
    'tester18',
    'tester18',
    'Tester',
    '18',
    'Tester 18',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester18'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester18', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester18';

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
    'tester19',
    'tester19',
    'Tester',
    '19',
    'Tester 19',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester19'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester19', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester19';

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
    'tester2',
    'tester2',
    'Tester',
    '2',
    'Tester 2',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester2'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester2', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester2';

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
    'tester20',
    'tester20',
    'Tester',
    '20',
    'Tester 20',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester20'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester20', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester20';

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
    'tester21',
    'tester21',
    'Tester',
    '21',
    'Tester 21',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester21'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester21', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester21';

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
    'tester22',
    'tester22',
    'Tester',
    '22',
    'Tester 22',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester22'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester22', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester22';

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
    'tester23',
    'tester23',
    'Tester',
    '23',
    'Tester 23',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester23'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester23', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester23';

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
    'tester24',
    'tester24',
    'Tester',
    '24',
    'Tester 24',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester24'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester24', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester24';

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
    'tester25',
    'tester25',
    'Tester',
    '25',
    'Tester 25',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester25'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester25', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester25';

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
    'tester26',
    'tester26',
    'Tester',
    '26',
    'Tester 26',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester26'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester26', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester26';

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
    'tester27',
    'tester27',
    'Tester',
    '27',
    'Tester 27',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester27'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester27', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester27';

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
    'tester28',
    'tester28',
    'Tester',
    '28',
    'Tester 28',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester28'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester28', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester28';

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
    'tester29',
    'tester29',
    'Tester',
    '29',
    'Tester 29',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester29'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester29', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester29';

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
    'tester3',
    'tester3',
    'Tester',
    '3',
    'Tester 3',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester3'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester3', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester3';

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
    'tester30',
    'tester30',
    'Tester',
    '30',
    'Tester 30',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester30'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester30', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester30';

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
    'tester31',
    'tester31',
    'Tester',
    '31',
    'Tester 31',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester31'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester31', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester31';

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
    'tester32',
    'tester32',
    'Tester',
    '32',
    'Tester 32',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester32'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester32', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester32';

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
    'tester33',
    'tester33',
    'Tester',
    '33',
    'Tester 33',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester33'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester33', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester33';

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
    'tester34',
    'tester34',
    'Tester',
    '34',
    'Tester 34',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester34'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester34', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester34';

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
    'tester35',
    'tester35',
    'Tester',
    '35',
    'Tester 35',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester35'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester35', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester35';

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
    'tester4',
    'tester4',
    'Tester',
    '4',
    'Tester 4',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester4'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester4', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester4';

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
    'tester5',
    'tester5',
    'Tester',
    '5',
    'Tester 5',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester5'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester5', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester5';

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
    'tester6',
    'tester6',
    'Tester',
    '6',
    'Tester 6',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester6'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester6', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester6';

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
    'tester7',
    'tester7',
    'Tester',
    '7',
    'Tester 7',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester7'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester7', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester7';

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
    'tester8',
    'tester8',
    'Tester',
    '8',
    'Tester 8',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester8'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester8', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester8';

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
    'tester9',
    'tester9',
    'Tester',
    '9',
    'Tester 9',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'tester9'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'tester9', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'tester9';

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
    'timesheetworker',
    'timesheetworker',
    'Timesheet',
    'Worker',
    'Timesheet Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'timesheetworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'timesheetworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'timesheetworker';

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
    'unauthenticated',
    'unauthenticated',
    'Unauthenticated',
    'Test User',
    'Unauthenticated Test User',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'unauthenticated'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'unauthenticated', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'unauthenticated';

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
    'warrantmgmtworker',
    'warrantmgmtworker',
    'WarrantManagement',
    'Worker',
    'WarrantManagement Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'warrantmgmtworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'warrantmgmtworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'warrantmgmtworker';

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
    'wpcsworker',
    'wpcsworker',
    'Wpcs',
    'Worker',
    'Wpcs Worker',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'wpcsworker'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'wpcsworker', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'wpcsworker';

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
    'wpcsworker2',
    'wpcsworker2',
    'Wpcs',
    'Worker2',
    'Wpcs Worker2',
    'SEPARATED'::ihss_org.employment_status_enum,
    TRUE,
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map to actual location
    'MIGRATION',
    NOW(),
    1,
    'wpcsworker2'
);

INSERT INTO temp_staff_mapping (legacy_username, new_uuid) SELECT 'wpcsworker2', staff_id FROM ihss_org.staff_account WHERE legacy_username = 'wpcsworker2';
-- ============================================================================
-- INSERT STATEMENTS FOR ihss_org.case_queue
-- Generated from WORKQUEUE.dmx
-- ============================================================================


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


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
    'QUEUE-',
    'Queue ',
    TRUE,
    NULL,
    NULL,
    TRUE,
    'MIGRATION',
    NOW(),
    1,
    
);


COMMIT;

-- ============================================================================
-- DATA LOAD COMPLETE
-- ============================================================================

-- ============================================================================
-- INSERT STATEMENTS FOR ihss_org.staffing_position
-- Generated from POSITION.dmx
-- Records: 132
-- ============================================================================

CREATE TEMP TABLE IF NOT EXISTS temp_position_mapping (
    legacy_id BIGINT,
    new_uuid UUID
);


INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-131',
    '50 New User',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 13),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-01-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    131
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 131, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 131;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-132',
    '50 Program Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -1763159254115549184),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-01-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    132
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 132, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 132;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-133',
    '50 17 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-01-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    133
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 133, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 133;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-134',
    '50 17 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-01-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    134
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 134, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 134;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-135',
    '50 17 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 5802888119866884096),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-01-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    135
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 135, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 135;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-123',
    '14 New User',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 13),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-01-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    123
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 123, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 123;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-124',
    '19 New User',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 13),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-01-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    124
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 124, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 124;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-125',
    '24 New User',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 13),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-01-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    125
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 125, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 125;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-126',
    '31 New User',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 13),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-01-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    126
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 126, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 126;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-127',
    '38 New User',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 13),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-01-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    127
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 127, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 127;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-128',
    '43 New User',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 13),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-01-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    128
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 128, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 128;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-129',
    '57 New User',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 13),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-01-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    129
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 129, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 129;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-136',
    '37 New User',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 13),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-11-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    136
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 136, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 136;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-130',
    '99 New User',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 13),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-01-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    130
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 130, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 130;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-4075757662770298880',
    '37 Program Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -1763159254115549184),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-04-20',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    4075757662770298880
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 4075757662770298880, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 4075757662770298880;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-122',
    '37 Default',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 13),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-04-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    122
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 122, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 122;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-118',
    '14 Payroll Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-30',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    118
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 118, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 118;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-119',
    '19 Payroll Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-30',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    119
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 119, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 119;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-120',
    '38 Payroll Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-30',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    120
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 120, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 120;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-121',
    '43 Payroll Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-30',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    121
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 121, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 121;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-114',
    '14 Payroll Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-30',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    114
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 114, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 114;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-115',
    '19 Payroll Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-30',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    115
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 115, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 115;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-116',
    '38 Payroll Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-30',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    116
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 116, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 116;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-117',
    '43 Payroll Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-30',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    117
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 117, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 117;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-113',
    '24 Payroll Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-29',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    113
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 113, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 113;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-7750694958704623616',
    '57 Program Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -1763159254115549184),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-04-20',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    7750694958704623616
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 7750694958704623616, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 7750694958704623616;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--6086614896391225344',
    '37 Contract Coordinator',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -7671881965225639936),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-02',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -6086614896391225344
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -6086614896391225344, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -6086614896391225344;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--7599824371187712000',
    '37 QA Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 6235233684094451712),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-02',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -7599824371187712000
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -7599824371187712000, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -7599824371187712000;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--3636656699101675520',
    '37 QA Analyst',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 6235233684094451712),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-02',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -3636656699101675520
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -3636656699101675520, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -3636656699101675520;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-5010254585449676800',
    '37 System Administrator',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -1114640907774197760),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-02',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    5010254585449676800
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 5010254585449676800, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 5010254585449676800;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--682295343546630144',
    '37 04 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 5802888119866884096),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-06',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -682295343546630144
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -682295343546630144, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -682295343546630144;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--7095421212922216448',
    '37 04 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-06',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -7095421212922216448
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -7095421212922216448, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -7095421212922216448;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-1551490071629135872',
    '37 01 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-06',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    1551490071629135872
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 1551490071629135872, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 1551490071629135872;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-4361736239108325376',
    '37 01 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 5802888119866884096),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-06',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    4361736239108325376
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 4361736239108325376, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 4361736239108325376;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-7027867218511659008',
    '37 01 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-06',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    7027867218511659008
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 7027867218511659008, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 7027867218511659008;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--5221923767936090112',
    '37 04 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-06',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -5221923767936090112
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -5221923767936090112, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -5221923767936090112;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--2195504818343116800',
    '37 Payroll Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-08',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -2195504818343116800
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -2195504818343116800, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -2195504818343116800;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--8392457905604919296',
    '37 Payroll Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-08',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -8392457905604919296
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -8392457905604919296, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -8392457905604919296;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--2844023164684468224',
    '37 Payroll Analyst',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -2411677600456900608),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-08',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -2844023164684468224
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -2844023164684468224, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -2844023164684468224;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-3064699546425622528',
    '37 Public Authority Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-08',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    3064699546425622528
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 3064699546425622528, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 3064699546425622528;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-8757249475421929472',
    '37 Public Authority Analyst',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -2411677600456900608),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-08',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    8757249475421929472
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 8757249475421929472, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 8757249475421929472;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-7865536749202571264',
    '57 System Administrator',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -1114640907774197760),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-14',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    7865536749202571264
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 7865536749202571264, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 7865536749202571264;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--132856189007429632',
    '57 Payroll Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-14',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -132856189007429632
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -132856189007429632, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -132856189007429632;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-8802285471695634432',
    '57 Payroll Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-14',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    8802285471695634432
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 8802285471695634432, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 8802285471695634432;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--4888657395510673408',
    '57 Payroll Analyst',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -2411677600456900608),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-14',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -4888657395510673408
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -4888657395510673408, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -4888657395510673408;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-6928788026709508096',
    '57 Public Authority Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3280872328539406336),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-14',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    6928788026709508096
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 6928788026709508096, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 6928788026709508096;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--6401866870307160064',
    '57 Public Authority Analyst',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3280872328539406336),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-14',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -6401866870307160064
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -6401866870307160064, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -6401866870307160064;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-8441997501505994752',
    '57 QA Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 6235233684094451712),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-14',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    8441997501505994752
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 8441997501505994752, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 8441997501505994752;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--5681290929927880704',
    '57 QA Analyst',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 6235233684094451712),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-14',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -5681290929927880704
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -5681290929927880704, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -5681290929927880704;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-3902369077116534784',
    '57 01 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-14',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    3902369077116534784
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 3902369077116534784, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 3902369077116534784;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--6041578900117520384',
    '57 01 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 5802888119866884096),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-14',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -6041578900117520384
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -6041578900117520384, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -6041578900117520384;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-2893562760585543680',
    '57 01 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-14',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    2893562760585543680
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 2893562760585543680, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 2893562760585543680;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-4406772235382030336',
    '57 02 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-14',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    4406772235382030336
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 4406772235382030336, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 4406772235382030336;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--8491537097407070208',
    'Branch Chief',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-16',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -8491537097407070208
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -8491537097407070208, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -8491537097407070208;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--6618039652420943872',
    'Fiscal Admin & Systems Bureau Chief',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-16',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -6618039652420943872
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -6618039652420943872, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -6618039652420943872;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-8586112689581850624',
    'Fiscal and Admin Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-16',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    8586112689581850624
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 8586112689581850624, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 8586112689581850624;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--5537175741852024832',
    'Fiscal and Admin Analyst',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -2411677600456900608),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-16',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -5537175741852024832
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -5537175741852024832, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -5537175741852024832;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-7577306373050859520',
    'Systems Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-16',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    7577306373050859520
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 7577306373050859520, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 7577306373050859520;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--5897463712041664512',
    'Systems Analyst',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -2411677600456900608),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-16',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -5897463712041664512
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -5897463712041664512, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -5897463712041664512;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-11223344',
    'Help Desk Support',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -2411677600456900608),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-16',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    11223344
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 11223344, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 11223344;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-223344555',
    'WPCS Worker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -2411677600456900608),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-07-16',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    223344555
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 223344555, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 223344555;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--6401866822207133364',
    '57 Homemaker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 326510972984360960),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-10-29',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -6401866822207133364
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -6401866822207133364, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -6401866822207133364;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--6401866844407133377',
    '57 Homemaker Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -6663075648694648832),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-10-29',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -6401866844407133377
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -6401866844407133377, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -6401866844407133377;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-1134907106097364992',
    'Operations and QA Bureau Chief',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 4794081803335892992),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-12-02',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    1134907106097364992
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 1134907106097364992, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 1134907106097364992;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-6494190662668255232',
    'Operations and QA Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-12-02',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    6494190662668255232
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 6494190662668255232, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 6494190662668255232;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-8583860889768165376',
    'Operations and QA Analyst',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -2411677600456900608),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-12-02',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    8583860889768165376
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 8583860889768165376, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 8583860889768165376;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--324259173170675712',
    '37 Homemaker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 326510972984360960),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-12-17',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -324259173170675712
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -324259173170675712, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -324259173170675712;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--7746191359077253120',
    '37 Homemaker Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -6663075648694648832),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2009-12-17',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -7746191359077253120
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -7746191359077253120, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -7746191359077253120;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--4989988387126509568',
    'Chief',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 4794081803335892992),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-01-19',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -4989988387126509568
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -4989988387126509568, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -4989988387126509568;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--7079658614226419712',
    '14 Program Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -1763159254115549184),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-01-19',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -7079658614226419712
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -7079658614226419712, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -7079658614226419712;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-3873095679538626560',
    '14 01 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-01-19',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    3873095679538626560
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 3873095679538626560, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 3873095679538626560;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-1206964700135292928',
    '14 01 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 5802888119866884096),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-01-19',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    1206964700135292928
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 1206964700135292928, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 1206964700135292928;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--5494391545392005120',
    '14 01 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-01-19',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -5494391545392005120
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -5494391545392005120, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -5494391545392005120;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-6449154666394550272',
    '19 Program Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -1763159254115549184),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    6449154666394550272
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 6449154666394550272, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 6449154666394550272;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--4728779608739020800',
    '19 01 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -4728779608739020800
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -4728779608739020800, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -4728779608739020800;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--8115486528521633792',
    '19 01 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -8115486528521633792
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -8115486528521633792, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -8115486528521633792;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--3143512539904606208',
    '19 01 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -3143512539904606208
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -3143512539904606208, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -3143512539904606208;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--6458161865649291264',
    '19 19 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -6458161865649291264
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -6458161865649291264, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -6458161865649291264;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--7250795400066498560',
    '19 19 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -7250795400066498560
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -7250795400066498560, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -7250795400066498560;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--4008203668359741440',
    '19 19 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -4008203668359741440
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -4008203668359741440, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -4008203668359741440;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--5593470737194156032',
    '19 35 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -5593470737194156032
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -5593470737194156032, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -5593470737194156032;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--8980177656976769024',
    '19 35 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -8980177656976769024
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -8980177656976769024, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -8980177656976769024;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--2350879005487398912',
    '19 35 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -2350879005487398912
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -2350879005487398912, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -2350879005487398912;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--6025816301421723648',
    '19 47 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -6025816301421723648
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -6025816301421723648, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -6025816301421723648;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--7683140964294066176',
    '19 47 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -7683140964294066176
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -7683140964294066176, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -7683140964294066176;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--4080261262397669376',
    '19 47 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -4080261262397669376
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -4080261262397669376, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -4080261262397669376;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--5161125172966588416',
    '19 73 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -5161125172966588416
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -5161125172966588416, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -5161125172966588416;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--8547832092749201408',
    '19 73 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -8547832092749201408
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -8547832092749201408, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -8547832092749201408;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--3215570133942534144',
    '19 73 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -3215570133942534144
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -3215570133942534144, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -3215570133942534144;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--6890507429876858880',
    '19 74 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -6890507429876858880
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -6890507429876858880, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -6890507429876858880;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--7034622617952714752',
    '19 74 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -7034622617952714752
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -7034622617952714752, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -7034622617952714752;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--3647915698170101760',
    '19 74 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -3647915698170101760
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -3647915698170101760, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -3647915698170101760;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--5233182767004516352',
    '19 75 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -5233182767004516352
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -5233182767004516352, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -5233182767004516352;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--8764004874862985216',
    '19 75 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -8764004874862985216
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -8764004874862985216, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -8764004874862985216;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--2783224569714966528',
    '19 75 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -2783224569714966528
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -2783224569714966528, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -2783224569714966528;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--6097873895459651584',
    '19 77 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -6097873895459651584
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -6097873895459651584, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -6097873895459651584;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--7899313746407849984',
    '19 77 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -7899313746407849984
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -7899313746407849984, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -7899313746407849984;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--4512606826625236992',
    '19 77 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -4512606826625236992
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -4512606826625236992, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -4512606826625236992;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--8700954480079798272',
    '24 Program Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -1763159254115549184),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -8700954480079798272
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -8700954480079798272, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -8700954480079798272;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--7484982580689764352',
    '24 01 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -7484982580689764352
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -7484982580689764352, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -7484982580689764352;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--6332061076082917376',
    '24 01 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -6332061076082917376
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -6332061076082917376, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -6332061076082917376;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--4026218066869223424',
    '24 01 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -4026218066869223424
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -4026218066869223424, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -4026218066869223424;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--3801038085500698624',
    '38 Program Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -1763159254115549184),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -3801038085500698624
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -3801038085500698624, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -3801038085500698624;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-1125899906842624000',
    '38 01 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    1125899906842624000
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 1125899906842624000, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 1125899906842624000;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-2422936599525326848',
    '38 01 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    2422936599525326848
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 2422936599525326848, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 2422936599525326848;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-8331659310635417600',
    '38 01 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    8331659310635417600
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 8331659310635417600, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 8331659310635417600;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-5089067578928660480',
    '38 12 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    5089067578928660480
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 5089067578928660480, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 5089067578928660480;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-8043428934483705856',
    '38 12 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    8043428934483705856
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 8043428934483705856, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 8043428934483705856;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-189151184349560832',
    '38 12 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    189151184349560832
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 189151184349560832, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 189151184349560832;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-3143512539904606208',
    '38 15 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    3143512539904606208
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 3143512539904606208, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 3143512539904606208;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-1630303065108119552',
    '38 15 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    1630303065108119552
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 1630303065108119552, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 1630303065108119552;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-5593470737194156032',
    '38 15 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    5593470737194156032
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 5593470737194156032, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 5593470737194156032;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-7106680211990642688',
    '38 22 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    7106680211990642688
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 7106680211990642688, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 7106680211990642688;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-6602277053725147136',
    '38 22 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    6602277053725147136
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 6602277053725147136, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 6602277053725147136;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-2639109381639110656',
    '38 22 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    2639109381639110656
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 2639109381639110656, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 2639109381639110656;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--6683341847017816064',
    '43 Program Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -1763159254115549184),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -6683341847017816064
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -6683341847017816064, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -6683341847017816064;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-9124292845052624896',
    '43 01 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    9124292845052624896
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 9124292845052624896, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 9124292845052624896;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-5233182767004516352',
    '43 01 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    5233182767004516352
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 5233182767004516352, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 5233182767004516352;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-4224376450473525248',
    '43 01 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    4224376450473525248
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 4224376450473525248, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 4224376450473525248;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--1963569437533536256',
    '37 05 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -1963569437533536256
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -1963569437533536256, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -1963569437533536256;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--7773212956841476096',
    '37 05 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -7773212956841476096
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -7773212956841476096, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -7773212956841476096;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--2008605433807241216',
    '37 05 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -2008605433807241216
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -2008605433807241216, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -2008605433807241216;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--7151716208264347648',
    '37 08 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -7151716208264347648
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -7151716208264347648, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -7151716208264347648;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--4314448443020935168',
    '37 08 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -4314448443020935168
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -4314448443020935168, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -4314448443020935168;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--8926134461448323072',
    '37 08 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -8926134461448323072
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -8926134461448323072, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -8926134461448323072;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--5467369947627782144',
    '37 10 DO Manager',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 3569102704691118080),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -5467369947627782144
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -5467369947627782144, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -5467369947627782144;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--5179139571476070400',
    '37 10 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -5179139571476070400
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -5179139571476070400, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -5179139571476070400;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS--2873296562262376448',
    '37 10 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    -2873296562262376448
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT -2873296562262376448, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = -2873296562262376448;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-405323966463344640',
    '57 02 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    405323966463344640
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 405323966463344640, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 405323966463344640;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-6314046677573435392',
    '57 02 Unit 1 Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-01',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    6314046677573435392
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 6314046677573435392, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 6314046677573435392;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-111',
    '31 01 Unit 1 Caseworker',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = 7748443158890938368),
    FALSE, -- TODO: Derive from job data
    FALSE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-26',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    111
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 111, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 111;

INSERT INTO ihss_org.staffing_position (
    staffing_position_id,
    position_code,
    position_name,
    program_unit_id,
    job_classification_id,
    is_supervisory,
    is_lead_position,
    status,
    effective_start,
    effective_end,
    created_by,
    created_at,
    version_number,
    legacy_position_id
) VALUES (
    gen_random_uuid(),
    'POS-112',
    '24 Payroll Supervisor',
    (SELECT program_unit_id FROM ihss_org.program_unit LIMIT 1), -- TODO: Map via POSITIONLOCATIONLINK
    (SELECT job_classification_id FROM ihss_org.job_classification WHERE legacy_job_id = -8320400311566991360),
    FALSE, -- TODO: Derive from job data
    TRUE,
    'ACTIVE'::ihss_org.position_status_enum,
    '2010-03-29',
    NULL,
    'MIGRATION',
    NOW(),
    1,
    112
);

INSERT INTO temp_position_mapping (legacy_id, new_uuid) SELECT 112, staffing_position_id FROM ihss_org.staffing_position WHERE legacy_position_id = 112;