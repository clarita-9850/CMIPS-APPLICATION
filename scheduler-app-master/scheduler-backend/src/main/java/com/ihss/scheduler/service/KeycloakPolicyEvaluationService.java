package com.ihss.scheduler.service;

import jakarta.annotation.PostConstruct;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.representations.idm.authorization.AuthorizationRequest;
import org.keycloak.representations.idm.authorization.AuthorizationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Dynamic Keycloak Policy Evaluation Service for Scheduler App.
 *
 * Zero hardcoded role-permission mappings. All authorization logic is
 * fetched from Keycloak at runtime and cached.
 *
 * Primary path: Keycloak UMA evaluation via AuthzClient
 * Fallback path: Dynamic permission cache from Keycloak Admin API
 */
@Service
public class KeycloakPolicyEvaluationService {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakPolicyEvaluationService.class);

    @Value("${keycloak.auth-server-url:${KEYCLOAK_URL:http://localhost:8085}/}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm:${KEYCLOAK_REALM:cmips}}")
    private String realm;

    @Value("${keycloak.resource:${KEYCLOAK_CLIENT_ID:batch-scheduler-app}}")
    private String clientId;

    @Value("${keycloak.credentials.secret:${KEYCLOAK_CLIENT_SECRET:}}")
    private String clientSecret;

    @Value("${keycloak.permission-cache.ttl-minutes:5}")
    private int cacheTtlMinutes;

    private volatile AuthzClient authzClient;
    private final RestTemplate restTemplate = new RestTemplate();

    // Dynamic permission cache: roleName -> { resourceName -> Set<scopeName> }
    private volatile Map<String, Map<String, Set<String>>> rolePermissionCache = new ConcurrentHashMap<>();
    private volatile long cacheLastRefreshed = 0;
    private final AtomicBoolean cacheRefreshInProgress = new AtomicBoolean(false);

    // Admin token cache
    private String adminToken;
    private long tokenExpiryTime = 0;

    @PostConstruct
    private void init() {
        // Initialize AuthzClient for UMA evaluation
        try {
            String serverUrl = keycloakServerUrl.endsWith("/") ? keycloakServerUrl : keycloakServerUrl + "/";
            Configuration configuration = new Configuration(
                serverUrl,
                realm,
                clientId,
                Map.of("secret", clientSecret),
                null
            );
            this.authzClient = AuthzClient.create(configuration);
            logger.info("AuthzClient initialized: server={}, realm={}, client={}", serverUrl, realm, clientId);
        } catch (Exception e) {
            logger.error("Failed to initialize AuthzClient: {}", e.getMessage());
        }

        // Build initial permission cache
        try {
            refreshPermissionCache();
        } catch (Exception e) {
            logger.warn("Failed to build initial permission cache (will retry): {}", e.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "#{${keycloak.permission-cache.ttl-minutes:5} * 60000}")
    public void scheduledCacheRefresh() {
        try {
            refreshPermissionCache();
        } catch (Exception e) {
            logger.error("Scheduled permission cache refresh failed: {}", e.getMessage());
        }
    }

    /**
     * Refreshes the dynamic permission cache from Keycloak Admin API.
     */
    public void refreshPermissionCache() {
        if (!cacheRefreshInProgress.compareAndSet(false, true)) {
            logger.info("Scheduler permission cache refresh already in progress, skipping");
            return;
        }

        logger.info("Refreshing scheduler permission cache from Keycloak...");
        long start = System.currentTimeMillis();

        try {
            String token = getAdminToken();
            String clientInternalId = getClientInternalId(token);

            // 1. Fetch resources
            List<Map<String, Object>> resources = fetchList(token,
                keycloakServerUrl + "admin/realms/" + realm + "/clients/" + clientInternalId + "/authz/resource-server/resource");
            Map<String, String> resourceIdToName = new HashMap<>();
            for (Map<String, Object> r : resources) {
                String id = (String) r.get("_id");
                if (id == null) id = (String) r.get("id");
                String name = (String) r.get("name");
                if (id != null && name != null) resourceIdToName.put(id, name);
            }

            // 2. Fetch scopes
            List<Map<String, Object>> scopes = fetchList(token,
                keycloakServerUrl + "admin/realms/" + realm + "/clients/" + clientInternalId + "/authz/resource-server/scope");
            Map<String, String> scopeIdToName = new HashMap<>();
            for (Map<String, Object> s : scopes) {
                String id = (String) s.get("id");
                String name = (String) s.get("name");
                if (id != null && name != null) scopeIdToName.put(id, name);
            }

            // 3. Fetch role-type policies
            List<Map<String, Object>> rolePolicies = fetchList(token,
                keycloakServerUrl + "admin/realms/" + realm + "/clients/" + clientInternalId + "/authz/resource-server/policy?type=role&first=0&max=500");
            Map<String, String> policyIdToRoleName = new HashMap<>();
            for (Map<String, Object> p : rolePolicies) {
                String policyId = (String) p.get("id");
                String policyName = (String) p.get("name");
                if (policyId == null || policyName == null) continue;
                String roleName = policyName.startsWith("Policy-") ? policyName.substring(7) : policyName;
                policyIdToRoleName.put(policyId, roleName);
            }

            // 4. Fetch permissions and build cache
            List<Map<String, Object>> permissions = fetchList(token,
                keycloakServerUrl + "admin/realms/" + realm + "/clients/" + clientInternalId + "/authz/resource-server/permission");
            Map<String, Map<String, Set<String>>> newCache = new ConcurrentHashMap<>();

            int skippedPermissions = 0;
            for (Map<String, Object> perm : permissions) {
                String permId = (String) perm.get("id");
                if (permId == null) continue;

                Set<String> resourceNames = resolveIds(perm.get("resources"), resourceIdToName);
                Set<String> scopeNames = resolveIds(perm.get("scopes"), scopeIdToName);
                Set<String> roleNames = resolveIds(perm.get("policies"), policyIdToRoleName);

                // If no roles found inline, try fetching associated policies
                if (roleNames.isEmpty()) {
                    try {
                        List<Map<String, Object>> assocPolicies = fetchList(token,
                            keycloakServerUrl + "admin/realms/" + realm + "/clients/" + clientInternalId
                                + "/authz/resource-server/policy/" + permId + "/associatedPolicies");
                        for (Map<String, Object> ap : assocPolicies) {
                            String apId = (String) ap.get("id");
                            String apName = (String) ap.get("name");
                            if (apId != null && policyIdToRoleName.containsKey(apId)) {
                                roleNames.add(policyIdToRoleName.get(apId));
                            } else if (apName != null && apName.startsWith("Policy-")) {
                                roleNames.add(apName.substring(7));
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to fetch associated policies for permission {}", permId);
                    }
                }

                // Skip permissions with incomplete data
                if (resourceNames.isEmpty() || scopeNames.isEmpty() || roleNames.isEmpty()) {
                    skippedPermissions++;
                    continue;
                }

                for (String roleName : roleNames) {
                    Map<String, Set<String>> resourceMap = newCache.computeIfAbsent(roleName, k -> new ConcurrentHashMap<>());
                    for (String resourceName : resourceNames) {
                        Set<String> scopeSet = resourceMap.computeIfAbsent(resourceName, k -> ConcurrentHashMap.newKeySet());
                        scopeSet.addAll(scopeNames);
                    }
                }
            }
            if (skippedPermissions > 0) {
                logger.warn("Skipped {} permissions with incomplete data", skippedPermissions);
            }

            this.rolePermissionCache = newCache;
            this.cacheLastRefreshed = System.currentTimeMillis();

            long elapsed = System.currentTimeMillis() - start;
            logger.info("Scheduler permission cache refreshed in {}ms: {} roles, {} total mappings",
                elapsed, newCache.size(), countTotalMappings(newCache));

        } catch (Exception e) {
            logger.error("Failed to refresh scheduler permission cache: {}", e.getMessage(), e);
            throw e;
        } finally {
            cacheRefreshInProgress.set(false);
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String> resolveIds(Object listObj, Map<String, String> idToName) {
        Set<String> names = new HashSet<>();
        if (listObj instanceof List) {
            for (Object item : (List<Object>) listObj) {
                if (item instanceof String) {
                    String name = idToName.get(item);
                    if (name != null) names.add(name);
                } else if (item instanceof Map) {
                    String name = (String) ((Map<String, Object>) item).get("name");
                    if (name != null) {
                        // For policy objects with "Policy-" prefix
                        if (name.startsWith("Policy-")) {
                            names.add(name.substring(7));
                        } else {
                            names.add(name);
                        }
                    }
                }
            }
        }
        return names;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchList(String token, String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), List.class);
        return response.getBody() != null ? response.getBody() : new ArrayList<>();
    }

    private String getAdminToken() {
        if (adminToken != null && System.currentTimeMillis() < tokenExpiryTime - 10000) {
            return adminToken;
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String tokenUrl = keycloakServerUrl + "realms/" + realm + "/protocol/openid-connect/token";
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, new HttpEntity<>(body, headers), Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            adminToken = (String) response.getBody().get("access_token");
            Integer expiresIn = (Integer) response.getBody().get("expires_in");
            tokenExpiryTime = System.currentTimeMillis() + (expiresIn * 1000L);
            return adminToken;
        }

        throw new RuntimeException("Failed to get admin token for scheduler");
    }

    @SuppressWarnings("unchecked")
    private String getClientInternalId(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        String url = keycloakServerUrl + "admin/realms/" + realm + "/clients?clientId=" + clientId;
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), List.class);

        if (response.getBody() != null && !response.getBody().isEmpty()) {
            Map<String, Object> client = (Map<String, Object>) response.getBody().get(0);
            return (String) client.get("id");
        }

        throw new RuntimeException("Client not found: " + clientId);
    }

    private int countTotalMappings(Map<String, Map<String, Set<String>>> cache) {
        int count = 0;
        for (Map<String, Set<String>> rm : cache.values()) {
            for (Set<String> s : rm.values()) count += s.size();
        }
        return count;
    }

    // UMA evaluation result: distinguishes explicit denial from errors
    public enum UmaResult { GRANTED, DENIED, ERROR }

    // ======================== PUBLIC API ========================

    public boolean evaluatePermission(String resourceName, String scope) {
        String userId = getCurrentUserId();
        logger.info("Evaluating permission: {}:{} user={}", resourceName, scope, userId);

        try {
            String accessToken = getCurrentUserAccessToken();
            if (accessToken == null) {
                logger.warn("No access token found for user={} - denying access", userId);
                return false;
            }

            // Try Keycloak UMA first
            UmaResult umaResult = queryKeycloakForPermission(accessToken, resourceName, scope);

            if (umaResult == UmaResult.GRANTED) {
                logger.info("Permission GRANTED: {}:{} user={} (via Keycloak UMA)", resourceName, scope, userId);
                return true;
            }

            if (umaResult == UmaResult.DENIED) {
                // Keycloak explicitly denied — do NOT fall back to cache
                logger.info("Permission DENIED: {}:{} user={} (via Keycloak UMA)", resourceName, scope, userId);
                return false;
            }

            // UMA ERROR — fall back to dynamic cache
            Set<String> userRoles = getCurrentUserRoles();
            boolean cachePermission = evaluateFromCache(resourceName, scope, userRoles);
            logger.info("Permission {}: {}:{} user={} (via dynamic cache)",
                cachePermission ? "GRANTED" : "DENIED", resourceName, scope, userId);
            return cachePermission;

        } catch (Exception e) {
            logger.error("Error evaluating permission for {}:{} user={} - {}", resourceName, scope, userId, e.getMessage());
            Set<String> userRoles = getCurrentUserRoles();
            boolean cachePermission = evaluateFromCache(resourceName, scope, userRoles);
            logger.info("Permission {} (error fallback): {}:{} user={}", cachePermission ? "GRANTED" : "DENIED", resourceName, scope, userId);
            return cachePermission;
        }
    }

    private boolean evaluateFromCache(String resourceName, String scope, Set<String> userRoles) {
        if (rolePermissionCache.isEmpty() ||
            System.currentTimeMillis() - cacheLastRefreshed > cacheTtlMinutes * 60_000L) {
            try {
                refreshPermissionCache();
            } catch (Exception e) {
                logger.warn("Cache refresh failed, using existing cache: {}", e.getMessage());
            }
        }

        if (rolePermissionCache.isEmpty()) {
            logger.warn("Permission cache is empty - denying access for {}:{}", resourceName, scope);
            return false;
        }

        for (String role : userRoles) {
            Map<String, Set<String>> resourceMap = rolePermissionCache.get(role);
            if (resourceMap != null) {
                Set<String> allowedScopes = resourceMap.get(resourceName);
                if (allowedScopes != null && allowedScopes.contains(scope)) {
                    return true;
                }
            }
        }

        return false;
    }

    private String getCurrentUserAccessToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                return ((Jwt) authentication.getPrincipal()).getTokenValue();
            }
        } catch (Exception e) {
            logger.error("Error extracting access token from JWT", e);
        }
        return null;
    }

    private UmaResult queryKeycloakForPermission(String accessToken, String resourceName, String scope) {
        try {
            if (authzClient == null) {
                logger.warn("AuthzClient not initialized, skipping UMA evaluation");
                return UmaResult.ERROR;
            }

            AuthorizationRequest request = new AuthorizationRequest();
            request.setAudience(clientId);
            request.addPermission(resourceName, scope);

            AuthorizationResponse response = authzClient.authorization(accessToken).authorize(request);
            return (response != null && response.getToken() != null) ? UmaResult.GRANTED : UmaResult.DENIED;

        } catch (org.keycloak.authorization.client.util.HttpResponseException e) {
            // HTTP 403 = explicit denial from Keycloak
            if (e.getStatusCode() == 403) {
                logger.info("Keycloak UMA explicitly denied {}:{}", resourceName, scope);
                return UmaResult.DENIED;
            }
            logger.warn("Keycloak UMA HTTP error for {}:{} - status={} {}", resourceName, scope, e.getStatusCode(), e.getMessage());
            return UmaResult.ERROR;
        } catch (Exception e) {
            logger.warn("Keycloak UMA error for {}:{} - {}", resourceName, scope, e.getMessage());
            return UmaResult.ERROR;
        }
    }

    public Set<String> getCurrentUserRoles() {
        Set<String> roles = new HashSet<>();

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();

                // Extract realm roles
                Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
                if (realmAccess != null && realmAccess.containsKey("roles")) {
                    @SuppressWarnings("unchecked")
                    List<String> rolesList = (List<String>) realmAccess.get("roles");
                    roles.addAll(rolesList);
                }

                // Extract client roles
                Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
                if (resourceAccess != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);
                    if (clientAccess != null && clientAccess.containsKey("roles")) {
                        @SuppressWarnings("unchecked")
                        List<String> clientRoles = (List<String>) clientAccess.get("roles");
                        roles.addAll(clientRoles);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error extracting roles from JWT", e);
        }

        return roles;
    }

    public String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                return ((Jwt) authentication.getPrincipal()).getSubject();
            }
        } catch (Exception e) {
            logger.error("Error extracting user ID from JWT", e);
        }
        return null;
    }

    public String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                return ((Jwt) authentication.getPrincipal()).getClaimAsString("preferred_username");
            }
        } catch (Exception e) {
            logger.error("Error extracting username from JWT", e);
        }
        return null;
    }
}
