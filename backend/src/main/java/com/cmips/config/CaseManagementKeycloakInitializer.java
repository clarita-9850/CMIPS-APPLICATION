package com.cmips.config;

import com.cmips.service.KeycloakAdminService;
import com.cmips.service.KeycloakPolicyEvaluationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Validates Keycloak authorization configuration on startup.
 *
 * All roles, resources, scopes, policies, and permissions are managed externally
 * via the setup_keycloak_authorization.py script and the Keycloak Admin Console.
 * This initializer only validates and logs the current state, then refreshes
 * the in-memory permission cache.
 */
@Component
@Order(2)
public class CaseManagementKeycloakInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CaseManagementKeycloakInitializer.class);

    private final KeycloakAdminService keycloakAdminService;
    private final KeycloakPolicyEvaluationService policyEvaluationService;

    @Value("${cmips.keycloak.init-resources:false}")
    private boolean initResources;

    public CaseManagementKeycloakInitializer(KeycloakAdminService keycloakAdminService,
                                            KeycloakPolicyEvaluationService policyEvaluationService) {
        this.keycloakAdminService = keycloakAdminService;
        this.policyEvaluationService = policyEvaluationService;
    }

    @Override
    public void run(String... args) {
        if (!initResources) {
            log.info("Keycloak resource initialization is disabled. Set cmips.keycloak.init-resources=true to enable.");
            return;
        }

        log.info("Validating Keycloak authorization configuration...");

        try {
            validateKeycloakState();

            // Refresh the permission cache so it picks up any recent changes
            if (policyEvaluationService != null) {
                policyEvaluationService.refreshPermissionCache();
                log.info("Permission cache refreshed successfully");
            }

            log.info("Keycloak authorization validation completed successfully");

        } catch (Exception e) {
            log.error("Error validating Keycloak authorization: {}", e.getMessage(), e);
        }
    }

    /**
     * Validates that Keycloak has the expected authorization objects configured.
     * Logs counts and warns if key resources or policies are missing.
     */
    private void validateKeycloakState() {
        try {
            List<Map<String, Object>> resources = keycloakAdminService.getAllResources();
            List<Map<String, Object>> policies = keycloakAdminService.getRolePolicies();
            List<Map<String, Object>> permissions = keycloakAdminService.getAllPermissions();
            List<Map<String, Object>> scopes = keycloakAdminService.getAllScopes();

            log.info("Keycloak authorization state: {} resources, {} scopes, {} policies, {} permissions",
                resources.size(), scopes.size(), policies.size(), permissions.size());

            // Validate expected key resources exist
            Set<String> resourceNames = new HashSet<>();
            for (Map<String, Object> r : resources) {
                String name = (String) r.get("name");
                if (name != null) resourceNames.add(name);
            }

            List<String> expectedResources = Arrays.asList(
                "Case Management Resource", "Provider Management Resource",
                "Referral Intake Resource", "Timesheet Resource",
                "Task Resource", "Work Queue Resource"
            );

            for (String expected : expectedResources) {
                if (!resourceNames.contains(expected)) {
                    log.warn("Expected resource '{}' not found in Keycloak. Run setup_keycloak_authorization.py to create it.", expected);
                }
            }

            if (policies.size() < 51) {
                log.warn("Expected at least 51 role policies but found {}. Run setup_keycloak_authorization.py to create them.", policies.size());
            }

            if (permissions.isEmpty()) {
                log.warn("No permissions found in Keycloak. Run setup_keycloak_authorization.py to create them.");
            }

            log.info("Available resources: {}", resourceNames);

        } catch (Exception e) {
            log.error("Error validating Keycloak state: {}", e.getMessage(), e);
        }
    }
}
