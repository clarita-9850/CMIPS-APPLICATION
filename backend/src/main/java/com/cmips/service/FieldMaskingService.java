package com.cmips.service;

import com.cmips.model.FieldMaskingRule;
import com.cmips.model.FieldMaskingRules;
import com.cmips.model.UserRole;
import com.cmips.entity.Timesheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FieldMaskingService {

    @Autowired(required = false)
    private JwtDecoder jwtDecoder;
    
    @Autowired
    private KeycloakAdminService keycloakAdminService;

    private Map<String, List<FieldMaskingRule>> roleBasedRules = new HashMap<>();
    private Map<String, List<String>> roleSelectedFields = new HashMap<>();

    public FieldMaskingService() {
        System.out.println("🔧 FieldMaskingService: Constructor called - initializing...");
        try {
            initializeDefaultRules();
            System.out.println("✅ FieldMaskingService: Constructor completed successfully");
        } catch (Exception e) {
            System.err.println("❌ FieldMaskingService: Constructor failed with error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Get field masking rules for a specific user role and report type (JWT-based)
     */
    public FieldMaskingRules getMaskingRules(String userRole, String reportType, String jwtToken) {
        System.out.println("🔧 FieldMaskingService: getMaskingRules called for role: " + userRole);
        
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            System.out.println("⚠️ FieldMaskingService: JWT token missing, using default rules");
            return buildDefaultRules(userRole, reportType);
        }
        
        try {
            // Try to get rules from Keycloak via KeycloakAdminService
            FieldMaskingRules keycloakRules = getMaskingRulesFromKeycloak(userRole, reportType, jwtToken);
            
            if (keycloakRules != null && keycloakRules.getRules() != null && !keycloakRules.getRules().isEmpty()) {
                System.out.println("✅ FieldMaskingService: Retrieved " + keycloakRules.getRules().size() + " rules from Keycloak");
                return keycloakRules;
            } else {
                System.out.println("⚠️ FieldMaskingService: No rules from Keycloak, using defaults");
                return buildDefaultRules(userRole, reportType);
            }
            
        } catch (Exception e) {
            System.err.println("❌ FieldMaskingService: Error getting masking rules: " + e.getMessage());
            return buildDefaultRules(userRole, reportType);
        }
    }

    /**
     * Get masking rules from Keycloak
     */
    private FieldMaskingRules getMaskingRulesFromKeycloak(String userRole, String reportType, String jwtToken) {
        try {
            // Extract role from JWT
            UserRole role = UserRole.from(userRole);
            
            // Get rules from Keycloak attributes (simplified - use existing KeycloakAdminService)
            // For now, return default rules - can be enhanced to fetch from Keycloak
            return buildDefaultRules(userRole, reportType);
            
        } catch (Exception e) {
            System.err.println("❌ Error getting rules from Keycloak: " + e.getMessage());
            return null;
        }
    }

    /**
     * Apply field masking to timesheet data
     */
    public Map<String, Object> applyMaskingToRecord(Timesheet timesheet, FieldMaskingRules rules) {
        Map<String, Object> maskedData = new HashMap<>();
        
        if (rules == null || rules.getRules() == null || rules.getRules().isEmpty()) {
            // No rules, return all fields
            return convertTimesheetToMap(timesheet);
        }
        
        for (FieldMaskingRule rule : rules.getRules()) {
            if (rule.getAccessLevel() != FieldMaskingRule.AccessLevel.HIDDEN_ACCESS) {
                Object value = getFieldValue(timesheet, rule.getFieldName());
                Object maskedValue = applyMaskingRule(value, rule);
                maskedData.put(rule.getFieldName(), maskedValue);
            }
        }
        
        return maskedData;
    }

    /**
     * Get field value from timesheet entity
     */
    private Object getFieldValue(Timesheet timesheet, String fieldName) {
        switch (fieldName.toLowerCase()) {
            case "id":
            case "timesheetid":
                return timesheet.getId();
            case "employeeid":
            case "providerid":
                return timesheet.getEmployeeId();
            case "employeename":
            case "providername":
                return timesheet.getEmployeeName();
            case "department":
                return timesheet.getDepartment();
            case "location":
            case "providercounty":
                return timesheet.getLocation();
            case "totalhours":
                return timesheet.getTotalHours();
            case "status":
                return timesheet.getStatus() != null ? timesheet.getStatus().name() : null;
            case "submittedat":
                return timesheet.getSubmittedAt();
            case "approvedat":
                return timesheet.getApprovedAt();
            default:
                return null;
        }
    }

    /**
     * Apply masking rule to field value
     */
    private Object applyMaskingRule(Object value, FieldMaskingRule rule) {
        if (value == null) {
            return null;
        }

        switch (rule.getMaskingType()) {
            case NONE:
                return value;
            case HIDDEN:
                return "***HIDDEN***";
            case PARTIAL_MASK:
                String strValue = value.toString();
                if (strValue.length() > 4) {
                    return "***" + strValue.substring(strValue.length() - 4);
                }
                return "***";
            case HASH_MASK:
                return "HASH_" + Math.abs(value.toString().hashCode());
            case ANONYMIZE:
                return "ANONYMIZED_" + Math.abs(value.toString().hashCode() % 1000);
            case AGGREGATE:
                return "AGGREGATED";
            default:
                return value;
        }
    }

    /**
     * Convert timesheet to map
     */
    private Map<String, Object> convertTimesheetToMap(Timesheet timesheet) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", timesheet.getId());
        map.put("employeeId", timesheet.getEmployeeId());
        map.put("employeeName", timesheet.getEmployeeName());
        map.put("department", timesheet.getDepartment());
        map.put("location", timesheet.getLocation());
        map.put("totalHours", timesheet.getTotalHours());
        map.put("status", timesheet.getStatus() != null ? timesheet.getStatus().name() : null);
        map.put("submittedAt", timesheet.getSubmittedAt());
        map.put("approvedAt", timesheet.getApprovedAt());
        return map;
    }

    /**
     * Update masking rules for a user role
     */
    public void updateRules(String userRole, List<FieldMaskingRule> rules, List<String> selectedFields) {
        System.out.println("🔧 FieldMaskingService: updateRules called for role: " + userRole);
        
        String roleType = UserRole.from(userRole).name();
        roleBasedRules.put(roleType, rules);
        if (selectedFields != null) {
            roleSelectedFields.put(roleType, selectedFields);
        }
        
        System.out.println("✅ FieldMaskingService: Rules updated successfully");
    }

    /**
     * Get selected fields for a user role
     */
    public List<String> getSelectedFields(String userRole) {
        String roleType = UserRole.from(userRole).name();
        return roleSelectedFields.getOrDefault(roleType, new ArrayList<>());
    }

    /**
     * Initialize default masking rules
     */
    private void initializeDefaultRules() {
        // ADMIN - Full access
        roleBasedRules.put(UserRole.ADMIN.name(), Arrays.asList(
            createRule("id", "NONE", "FULL_ACCESS"),
            createRule("employeeId", "NONE", "FULL_ACCESS"),
            createRule("employeeName", "NONE", "FULL_ACCESS"),
            createRule("department", "NONE", "FULL_ACCESS"),
            createRule("location", "NONE", "FULL_ACCESS"),
            createRule("totalHours", "NONE", "FULL_ACCESS"),
            createRule("status", "NONE", "FULL_ACCESS")
        ));

        // SUPERVISOR - Masked personal data
        roleBasedRules.put(UserRole.SUPERVISOR.name(), Arrays.asList(
            createRule("id", "NONE", "FULL_ACCESS"),
            createRule("employeeId", "HASH_MASK", "MASKED_ACCESS"),
            createRule("employeeName", "ANONYMIZE", "MASKED_ACCESS"),
            createRule("department", "NONE", "FULL_ACCESS"),
            createRule("location", "NONE", "FULL_ACCESS"),
            createRule("totalHours", "NONE", "FULL_ACCESS"),
            createRule("status", "NONE", "FULL_ACCESS")
        ));

        // CASE_WORKER - Limited access
        roleBasedRules.put(UserRole.CASE_WORKER.name(), Arrays.asList(
            createRule("id", "NONE", "FULL_ACCESS"),
            createRule("employeeId", "HASH_MASK", "MASKED_ACCESS"),
            createRule("employeeName", "ANONYMIZE", "MASKED_ACCESS"),
            createRule("department", "NONE", "FULL_ACCESS"),
            createRule("location", "NONE", "FULL_ACCESS"),
            createRule("totalHours", "NONE", "FULL_ACCESS"),
            createRule("status", "NONE", "FULL_ACCESS")
        ));
    }

    private FieldMaskingRule createRule(String fieldName, String maskingType, String accessLevel) {
        FieldMaskingRule rule = new FieldMaskingRule();
        rule.setFieldName(fieldName);
        rule.setMaskingType(FieldMaskingRule.MaskingType.valueOf(maskingType));
        rule.setAccessLevel(FieldMaskingRule.AccessLevel.valueOf(accessLevel));
        rule.setEnabled(true);
        return rule;
    }

    private FieldMaskingRules buildDefaultRules(String userRole, String reportType) {
        FieldMaskingRules defaultRules = new FieldMaskingRules();
        defaultRules.setUserRole(userRole);
        defaultRules.setReportType(reportType);

        String roleType = UserRole.from(userRole).name();
        List<FieldMaskingRule> defaults = roleBasedRules
            .getOrDefault(roleType, Collections.emptyList())
            .stream()
            .map(this::cloneRule)
            .collect(Collectors.toList());
        defaultRules.setRules(defaults);
        return defaultRules;
    }

    private FieldMaskingRule cloneRule(FieldMaskingRule source) {
        if (source == null) {
            return null;
        }
        FieldMaskingRule clone = new FieldMaskingRule();
        clone.setFieldName(source.getFieldName());
        clone.setMaskingType(source.getMaskingType());
        clone.setAccessLevel(source.getAccessLevel());
        clone.setMaskingPattern(source.getMaskingPattern());
        clone.setReportType(source.getReportType());
        clone.setDescription(source.getDescription());
        clone.setEnabled(source.isEnabled());
        return clone;
    }
}

