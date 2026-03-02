package com.cmips.service;

import jakarta.annotation.PostConstruct;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.representations.idm.authorization.AuthorizationRequest;
import org.keycloak.representations.idm.authorization.AuthorizationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Cache-first Keycloak Policy Evaluation Service
 *
 * Permission checks use JWT roles + in-memory role→resource→scope cache.
 * Keycloak Admin API is used only to populate/refresh the cache on startup
 * and every N minutes (configurable). No Keycloak call per request when
 * the cache has data. Zero hardcoded role-permission mappings.
 *
 * Primary path: JWT roles + in-memory cache (no Keycloak call)
 * Fallback path: Keycloak UMA (only when cache is empty)
 *
 * Adding/removing roles, resources, or permissions in Keycloak requires
 * zero code changes — the cache auto-refreshes on schedule.
 */
@Service
public class KeycloakPolicyEvaluationService {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakPolicyEvaluationService.class);

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    @Value("${keycloak.permission-cache.ttl-minutes:5}")
    private int cacheTtlMinutes;

    @Autowired
    private KeycloakAdminService keycloakAdminService;

    private volatile AuthzClient authzClient;

    // Dynamic permission cache: roleName -> { resourceName -> Set<scopeName> }
    private volatile Map<String, Map<String, Set<String>>> rolePermissionCache = new ConcurrentHashMap<>();
    private volatile long cacheLastRefreshed = 0;
    private final AtomicBoolean cacheRefreshInProgress = new AtomicBoolean(false);

    @PostConstruct
    private void init() {
        // Initialize AuthzClient for UMA evaluation
        try {
            Configuration configuration = new Configuration(
                keycloakServerUrl,
                realm,
                clientId,
                Map.of("secret", clientSecret),
                null
            );
            this.authzClient = AuthzClient.create(configuration);
            logger.info("AuthzClient initialized: server={}, realm={}, client={}", keycloakServerUrl, realm, clientId);
        } catch (Exception e) {
            logger.error("Failed to initialize AuthzClient: {}", e.getMessage());
        }

        // Build initial permission cache (non-blocking on failure)
        try {
            refreshPermissionCache();
        } catch (Exception e) {
            logger.warn("Failed to build initial permission cache (will retry on next request): {}", e.getMessage());
        }
    }

    /**
     * Periodically refresh the permission cache from Keycloak Admin API.
     * Runs every 5 minutes (configurable via keycloak.permission-cache.ttl-minutes).
     */
    @Scheduled(fixedDelayString = "#{${keycloak.permission-cache.ttl-minutes:5} * 60000}")
    public void scheduledCacheRefresh() {
        try {
            refreshPermissionCache();
        } catch (Exception e) {
            logger.error("Scheduled permission cache refresh failed: {}", e.getMessage());
        }
    }

    /**
     * Refreshes the dynamic permission cache by fetching all resources, scopes,
     * policies, and permissions from Keycloak Admin REST API.
     * All permissions for all roles are cached (role -> resource -> allowed scopes).
     * Evaluation is cache-first: allowed/denied from this cache without calling Keycloak UMA.
     *
     * Builds: roleName -> { resourceName -> Set<scopeName> }
     */
    public void refreshPermissionCache() {
        // Prevent concurrent refreshes
        if (!cacheRefreshInProgress.compareAndSet(false, true)) {
            logger.info("Permission cache refresh already in progress, skipping");
            return;
        }

        logger.info("Refreshing dynamic permission cache from Keycloak...");
        long start = System.currentTimeMillis();

        try {
            // 1. Fetch all resources -> build resourceId -> resourceName map and known names set
            List<Map<String, Object>> resources = keycloakAdminService.getAllResources();
            Map<String, String> resourceIdToName = new HashMap<>();
            Set<String> knownResourceNames = new HashSet<>();
            for (Map<String, Object> resource : resources) {
                String id = (String) resource.get("_id");
                if (id == null) id = (String) resource.get("id");
                String name = (String) resource.get("name");
                if (id != null && name != null) {
                    resourceIdToName.put(id, name);
                    knownResourceNames.add(name);
                }
            }
            logger.debug("Loaded {} resources", resourceIdToName.size());

            // 2. Fetch all scopes -> build scopeId -> scopeName map and known names set
            List<Map<String, Object>> scopes = keycloakAdminService.getAllScopes();
            Map<String, String> scopeIdToName = new HashMap<>();
            Set<String> knownScopeNames = new HashSet<>();
            for (Map<String, Object> scope : scopes) {
                String id = (String) scope.get("id");
                if (id == null) id = (String) scope.get("_id");
                String name = (String) scope.get("name");
                if (id != null && name != null) {
                    scopeIdToName.put(id, name);
                    knownScopeNames.add(name);
                }
            }
            logger.debug("Loaded {} scopes", scopeIdToName.size());

            // 3. Fetch all role-type policies -> build policyId -> roleName map
            List<Map<String, Object>> rolePolicies = keycloakAdminService.getRolePolicies();
            Map<String, String> policyIdToRoleName = new HashMap<>();
            for (Map<String, Object> policy : rolePolicies) {
                String policyId = (String) policy.get("id");
                String policyName = (String) policy.get("name");
                if (policyId == null || policyName == null) continue;

                // Extract role name from policy name convention: "Policy-ROLENAME"
                String roleName = extractRoleNameFromPolicy(policy, policyName);
                if (roleName != null) {
                    policyIdToRoleName.put(policyId, roleName);
                }
            }
            logger.debug("Loaded {} role policies -> role mappings", policyIdToRoleName.size());

            // 4. Fetch all permissions -> resolve references and build cache
            List<Map<String, Object>> permissions = keycloakAdminService.getAllPermissions();
            Map<String, Map<String, Set<String>>> newCache = new ConcurrentHashMap<>();

            int skippedPermissions = 0;
            boolean loggedSample = false;
            for (Map<String, Object> permission : permissions) {
                String permissionId = (String) permission.get("id");
                String permissionName = (String) permission.get("name");
                if (permissionId == null) continue;

                // Resolve resource names from the permission
                Set<String> resourceNames = resolveResourceNames(permission, resourceIdToName, knownResourceNames);

                // Resolve scope names from the permission
                Set<String> scopeNames = resolveScopeNames(permission, scopeIdToName, knownScopeNames);

                // Fallback: Keycloak list-permission API often returns only id, name, type (no resources/scopes).
                // Try parsing permission name in two formats:
                // 1. Old format: "Permission-Task-Resource-view" -> resource "Task Resource", scope "view"
                // 2. New DSD format: "Permission-{ROLENAME}-{ResourceNameNoSpaces}" -> resource + role from name, scopes via API
                Set<String> roleNames = new HashSet<>();
                if ((resourceNames.isEmpty() || scopeNames.isEmpty()) && permissionName != null) {
                    ParsedPermissionName parsed = parsePermissionName(permissionName, knownResourceNames);
                    if (parsed != null) {
                        if (parsed.scopeName != null) {
                            // Old format: got resource + scope from name
                            if (resourceNames.isEmpty()) resourceNames.add(parsed.resourceName);
                            if (scopeNames.isEmpty()) scopeNames.add(parsed.scopeName);
                        } else {
                            // New DSD format: got resource + role from name, fetch scopes via API
                            if (resourceNames.isEmpty()) resourceNames.add(parsed.resourceName);
                            if (parsed.roleName != null) roleNames.add(parsed.roleName);
                            if (scopeNames.isEmpty()) {
                                List<Map<String, Object>> permScopes = keycloakAdminService.getPermissionScopes(permissionId);
                                for (Map<String, Object> s : permScopes) {
                                    String name = (String) s.get("name");
                                    if (name != null) scopeNames.add(name);
                                }
                            }
                        }
                    }
                }

                // Debug: log structure of first permission that still has empty resources/scopes
                if (!loggedSample && (resourceNames.isEmpty() || scopeNames.isEmpty())) {
                    logger.info("Permission sample (id={}, name={}): keys={}, resourceNames={}, scopeNames={}",
                        permissionId, permissionName, permission.keySet(), resourceNames, scopeNames);
                    loggedSample = true;
                }

                // Resolve role names from associated policies (if not already resolved from name)
                if (roleNames.isEmpty()) {
                    roleNames = resolveRoleNames(permission, policyIdToRoleName, permissionId);
                }

                // Skip permissions with incomplete data
                if (resourceNames.isEmpty() || scopeNames.isEmpty() || roleNames.isEmpty()) {
                    skippedPermissions++;
                    logger.warn("Skipping permission [{}] '{}': resources={}, scopes={}, roles={}",
                        permissionId, permissionName, resourceNames.size(), scopeNames.size(), roleNames.size());
                    continue;
                }

                // Build the cache: for each role × resource × scope
                for (String roleName : roleNames) {
                    Map<String, Set<String>> resourceMap = newCache.computeIfAbsent(roleName, k -> new ConcurrentHashMap<>());
                    for (String resourceName : resourceNames) {
                        Set<String> scopeSet = resourceMap.computeIfAbsent(resourceName, k -> ConcurrentHashMap.newKeySet());
                        scopeSet.addAll(scopeNames);
                    }
                }
            }
            if (skippedPermissions > 0) {
                logger.warn("Skipped {} permissions with incomplete data out of {} total", skippedPermissions, permissions.size());
            }

            // 5. Atomically swap the cache
            this.rolePermissionCache = newCache;
            this.cacheLastRefreshed = System.currentTimeMillis();

            long elapsed = System.currentTimeMillis() - start;
            logger.info("Permission cache refreshed in {}ms: {} roles, {} total resource-scope mappings",
                elapsed, newCache.size(), countTotalMappings(newCache));

            // Log sample entries for debugging
            if (logger.isDebugEnabled()) {
                for (Map.Entry<String, Map<String, Set<String>>> entry : newCache.entrySet()) {
                    logger.debug("  Role [{}]: {} resources", entry.getKey(), entry.getValue().size());
                    for (Map.Entry<String, Set<String>> resEntry : entry.getValue().entrySet()) {
                        logger.debug("    {} -> {}", resEntry.getKey(), resEntry.getValue());
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Failed to refresh permission cache: {}", e.getMessage(), e);
            throw e;
        } finally {
            cacheRefreshInProgress.set(false);
        }
    }

    /**
     * Extract the role name from a Keycloak policy.
     * Tries "Policy-ROLENAME" naming convention first, then falls back to parsing roles array.
     */
    @SuppressWarnings("unchecked")
    private String extractRoleNameFromPolicy(Map<String, Object> policy, String policyName) {
        // Try naming convention: "Policy-ROLENAME"
        if (policyName.startsWith("Policy-")) {
            return policyName.substring("Policy-".length());
        }

        // Fallback: try to extract from the roles array in the policy config
        Object rolesObj = policy.get("roles");
        if (rolesObj instanceof List) {
            List<Map<String, Object>> roles = (List<Map<String, Object>>) rolesObj;
            if (!roles.isEmpty()) {
                // The role object may have an "id" field — we'd need to resolve it
                // But for now, return the policy name as-is (strip common prefixes)
                return policyName;
            }
        }

        // Try config field (some Keycloak versions put role info in config.roles)
        Object configObj = policy.get("config");
        if (configObj instanceof Map) {
            Map<String, Object> config = (Map<String, Object>) configObj;
            Object configRoles = config.get("roles");
            if (configRoles instanceof String) {
                // Parse JSON string like [{"id":"uuid","required":true}]
                // For simplicity, use the policy name
                return policyName;
            }
        }

        return policyName;
    }

    /**
     * Resolve resource names from a permission object.
     * Handles: "resources" array (IDs or names), "resource" singular (ID or name), and embedded objects with "name".
     */
    @SuppressWarnings("unchecked")
    private Set<String> resolveResourceNames(Map<String, Object> permission, Map<String, String> resourceIdToName,
                                             Set<String> knownResourceNames) {
        Set<String> names = new HashSet<>();
        // List: "resources" (array of ID or name or {name})
        Object resourcesObj = permission.get("resources");
        if (resourcesObj instanceof List) {
            for (Object item : (List<Object>) resourcesObj) {
                if (item instanceof String) {
                    String s = (String) item;
                    String name = resourceIdToName.get(s);
                    if (name != null) names.add(name);
                    else if (knownResourceNames.contains(s)) names.add(s);
                } else if (item instanceof Map) {
                    Map<String, Object> m = (Map<String, Object>) item;
                    String name = (String) m.get("name");
                    if (name != null) names.add(name);
                    else {
                        String id = (String) m.get("_id");
                        if (id == null) id = (String) m.get("id");
                        if (id != null) {
                            String resolved = resourceIdToName.get(id);
                            if (resolved != null) names.add(resolved);
                        }
                    }
                }
            }
        }
        // Singular: "resource" (ID or name string - some Keycloak versions)
        Object resourceObj = permission.get("resource");
        if (resourceObj instanceof String) {
            String s = (String) resourceObj;
            String name = resourceIdToName.get(s);
            if (name != null) names.add(name);
            else if (knownResourceNames.contains(s)) names.add(s);
        } else if (resourceObj instanceof Map) {
            Map<String, Object> m = (Map<String, Object>) resourceObj;
            String name = (String) m.get("name");
            if (name != null) names.add(name);
            else {
                String id = (String) m.get("_id");
                if (id == null) id = (String) m.get("id");
                if (id != null) {
                    String resolved = resourceIdToName.get(id);
                    if (resolved != null) names.add(resolved);
                }
            }
        }
        return names;
    }

    /**
     * Resolve scope names from a permission object.
     * Handles: "scopes" array (IDs or names), and embedded objects with "name".
     */
    @SuppressWarnings("unchecked")
    private Set<String> resolveScopeNames(Map<String, Object> permission, Map<String, String> scopeIdToName,
                                          Set<String> knownScopeNames) {
        Set<String> names = new HashSet<>();
        Object scopesObj = permission.get("scopes");
        if (scopesObj instanceof List) {
            for (Object item : (List<Object>) scopesObj) {
                if (item instanceof String) {
                    String s = (String) item;
                    String name = scopeIdToName.get(s);
                    if (name != null) names.add(name);
                    else if (knownScopeNames.contains(s)) names.add(s);
                } else if (item instanceof Map) {
                    Map<String, Object> m = (Map<String, Object>) item;
                    String name = (String) m.get("name");
                    if (name != null) names.add(name);
                    else {
                        String id = (String) m.get("id");
                        if (id == null) id = (String) m.get("_id");
                        if (id != null) {
                            String resolved = scopeIdToName.get(id);
                            if (resolved != null) names.add(resolved);
                        }
                    }
                }
            }
        }
        return names;
    }

    /**
     * Parsed result from a permission name. Contains resource name and optionally scope/role.
     * Old format (Permission-Task-Resource-view): resourceName + scopeName set.
     * New DSD format (Permission-ROLENAME-ResourceNoSpaces): resourceName + roleName set, scopeName null.
     */
    private static class ParsedPermissionName {
        final String resourceName;
        final String scopeName;
        final String roleName;

        ParsedPermissionName(String resourceName, String scopeName, String roleName) {
            this.resourceName = resourceName;
            this.scopeName = scopeName;
            this.roleName = roleName;
        }
    }

    /**
     * Parse permission name in two formats:
     * 1. Old format: "Permission-Task-Resource-view" -> resource "Task Resource", scope "view"
     * 2. New DSD format: "Permission-{ROLENAME}-{ResourceNameNoSpaces}" -> resource + role
     *    (scopes must be fetched separately via API)
     */
    private ParsedPermissionName parsePermissionName(String permissionName, Set<String> knownResourceNames) {
        if (permissionName == null || !permissionName.startsWith("Permission-")) return null;

        // Try old format first: "Permission-{ResourcePart}-Resource-{scope}"
        int resourceIdx = permissionName.indexOf("-Resource-");
        if (resourceIdx >= 0) {
            String afterPrefix = permissionName.substring("Permission-".length());
            int resPartEnd = afterPrefix.indexOf("-Resource-");
            if (resPartEnd >= 0) {
                String resourcePart = afterPrefix.substring(0, resPartEnd).replace('-', ' ');
                String scopePart = afterPrefix.substring(resPartEnd + "-Resource-".length()).trim();
                if (!scopePart.isEmpty()) {
                    return new ParsedPermissionName(resourcePart + " Resource", scopePart, null);
                }
            }
        }

        // Try new DSD format: "Permission-{ROLENAME}-{ResourceNameNoSpaces}"
        // Match longest resource name first to avoid ambiguous matches
        // (e.g. "Case Management Resource" must match before "Default Resource")
        if (knownResourceNames != null) {
            String afterPrefix = permissionName.substring("Permission-".length());
            String bestResourceName = null;
            String bestRoleName = null;
            int bestLength = 0;
            for (String knownName : knownResourceNames) {
                String noSpaces = knownName.replace(" ", "");
                if (afterPrefix.endsWith("-" + noSpaces) && noSpaces.length() > bestLength) {
                    String roleName = afterPrefix.substring(0, afterPrefix.length() - noSpaces.length() - 1);
                    if (!roleName.isEmpty()) {
                        bestResourceName = knownName;
                        bestRoleName = roleName;
                        bestLength = noSpaces.length();
                    }
                }
            }
            if (bestResourceName != null) {
                return new ParsedPermissionName(bestResourceName, null, bestRoleName);
            }
        }

        return null;
    }

    /**
     * Resolve role names from a permission's associated policies.
     * Uses inline policy references first, falls back to API call for associated policies.
     */
    @SuppressWarnings("unchecked")
    private Set<String> resolveRoleNames(Map<String, Object> permission, Map<String, String> policyIdToRoleName, String permissionId) {
        Set<String> roleNames = new HashSet<>();

        // Try inline policy references
        Object policiesObj = permission.get("policies");
        if (policiesObj instanceof List) {
            List<Object> policies = (List<Object>) policiesObj;
            for (Object item : policies) {
                if (item instanceof String) {
                    String roleName = policyIdToRoleName.get(item);
                    if (roleName != null) roleNames.add(roleName);
                } else if (item instanceof Map) {
                    String name = (String) ((Map<String, Object>) item).get("name");
                    if (name != null && name.startsWith("Policy-")) {
                        roleNames.add(name.substring("Policy-".length()));
                    }
                }
            }
        }

        // If no roles found inline, fetch associated policies via API
        if (roleNames.isEmpty()) {
            try {
                List<Map<String, Object>> associatedPolicies = keycloakAdminService.getPermissionAssociatedPolicies(permissionId);
                for (Map<String, Object> assocPolicy : associatedPolicies) {
                    String assocPolicyId = (String) assocPolicy.get("id");
                    String assocPolicyName = (String) assocPolicy.get("name");

                    // Check in our policyId map first
                    if (assocPolicyId != null && policyIdToRoleName.containsKey(assocPolicyId)) {
                        roleNames.add(policyIdToRoleName.get(assocPolicyId));
                    } else if (assocPolicyName != null && assocPolicyName.startsWith("Policy-")) {
                        roleNames.add(assocPolicyName.substring("Policy-".length()));
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to fetch associated policies for permission {}: {}", permissionId, e.getMessage());
            }
        }

        return roleNames;
    }

    private int countTotalMappings(Map<String, Map<String, Set<String>>> cache) {
        int count = 0;
        for (Map<String, Set<String>> resourceMap : cache.values()) {
            for (Set<String> scopes : resourceMap.values()) {
                count += scopes.size();
            }
        }
        return count;
    }

    /**
     * Evaluates if the current user has permission for a specific resource and scope.
     *
     * Cache-first: when the cache is non-empty, the decision is made entirely from
     * JWT roles + in-memory role→resource→scope cache. No Keycloak call per request.
     *
     * UMA fallback: used only when the cache is empty (before first refresh or after
     * repeated Admin API failures). A warning is logged so operators can see fallback usage.
     */
    public boolean evaluatePermission(String resourceName, String scope) {
        String userId = getCurrentUserId();
        logger.debug("Evaluating permission: {}:{} for user={}", resourceName, scope, userId);

        Set<String> userRoles = getCurrentUserRoles();
        if (userRoles == null) {
            userRoles = Collections.emptySet();
        }

        // ── Cache path (normal) ──────────────────────────────────────────
        if (!rolePermissionCache.isEmpty()) {
            boolean granted = evaluateFromCache(resourceName, scope, userRoles);
            logger.info("Permission {}: {}:{} user={} roles={} (cache)",
                granted ? "GRANTED" : "DENIED", resourceName, scope, userId, userRoles);
            return granted;
        }

        // ── UMA fallback (cache empty) ───────────────────────────────────
        logger.warn("Permission cache is empty — falling back to Keycloak UMA for {}:{} user={}", resourceName, scope, userId);
        String accessToken = getCurrentUserAccessToken();
        if (accessToken == null) {
            logger.warn("No access token and cache empty — denying {}:{} user={}", resourceName, scope, userId);
            return false;
        }
        UmaResult umaResult = queryKeycloakForPermission(accessToken, resourceName, scope);
        boolean granted = (umaResult == UmaResult.GRANTED);
        logger.info("Permission {}: {}:{} user={} (UMA fallback)",
            granted ? "GRANTED" : "DENIED", resourceName, scope, userId);
        return granted;
    }

    /**
     * Pure cache lookup — no Keycloak calls. Checks if any of the user's JWT roles
     * grant access to the given resource + scope in the in-memory cache.
     */
    private boolean evaluateFromCache(String resourceName, String scope, Set<String> userRoles) {
        for (String role : userRoles) {
            Map<String, Set<String>> resourceMap = rolePermissionCache.get(role);
            if (resourceMap != null) {
                Set<String> allowedScopes = resourceMap.get(resourceName);
                if (allowedScopes != null && allowedScopes.contains(scope)) {
                    logger.debug("Cache hit: role={} grants {}:{}", role, resourceName, scope);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets all granted scopes for a specific resource based on the current user's JWT roles.
     * Cache-first: uses in-memory cache when available; UMA fallback only when cache is empty.
     * Useful for field-level authorization and dynamic UI rendering.
     */
    public Set<String> getGrantedScopes(String resourceName) {
        logger.debug("Getting granted scopes for resource: {}", resourceName);

        Set<String> userRoles = getCurrentUserRoles();
        if (userRoles == null) {
            userRoles = Collections.emptySet();
        }

        // ── Cache path (normal) ──────────────────────────────────────────
        if (!rolePermissionCache.isEmpty()) {
            Set<String> grantedScopes = new HashSet<>();
            for (String role : userRoles) {
                Map<String, Set<String>> resourceMap = rolePermissionCache.get(role);
                if (resourceMap != null) {
                    Set<String> scopes = resourceMap.get(resourceName);
                    if (scopes != null) {
                        grantedScopes.addAll(scopes);
                    }
                }
            }
            logger.info("Granted scopes for {} (cache): {}", resourceName, grantedScopes);
            return grantedScopes;
        }

        // ── UMA fallback (cache empty) ───────────────────────────────────
        logger.warn("Permission cache is empty — falling back to Keycloak UMA for granted scopes on {}", resourceName);
        try {
            String accessToken = getCurrentUserAccessToken();
            if (accessToken == null) {
                logger.warn("No access token and cache empty — returning empty scopes");
                return new HashSet<>();
            }
            Set<String> grantedScopes = queryKeycloakForGrantedScopes(accessToken, resourceName);
            logger.info("Granted scopes for {} (UMA fallback): {}", resourceName, grantedScopes);
            return grantedScopes;
        } catch (Exception e) {
            logger.error("Error getting granted scopes for {} — {}", resourceName, e.getMessage(), e);
            return new HashSet<>();
        }
    }

    /**
     * Evaluates multiple permissions at once.
     * @return true if ALL scopes are granted
     */
    public boolean evaluateAllPermissions(String resourceName, String... scopes) {
        for (String scope : scopes) {
            if (!evaluatePermission(resourceName, scope)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Evaluates if user has ANY of the specified permissions.
     * @return true if ANY scope is granted
     */
    public boolean evaluateAnyPermission(String resourceName, String... scopes) {
        for (String scope : scopes) {
            if (evaluatePermission(resourceName, scope)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the current cache contents (for admin/debug endpoints).
     */
    public Map<String, Map<String, Set<String>>> getCacheContents() {
        return Collections.unmodifiableMap(rolePermissionCache);
    }

    /**
     * Returns cache metadata for monitoring.
     */
    public Map<String, Object> getCacheStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("rolesCount", rolePermissionCache.size());
        status.put("totalMappings", countTotalMappings(rolePermissionCache));
        status.put("lastRefreshed", cacheLastRefreshed > 0 ? new Date(cacheLastRefreshed).toString() : "never");
        status.put("ttlMinutes", cacheTtlMinutes);
        status.put("stale", System.currentTimeMillis() - cacheLastRefreshed > cacheTtlMinutes * 60_000L);
        return status;
    }

    private String getCurrentUserAccessToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                return jwt.getTokenValue();
            }
        } catch (Exception e) {
            logger.error("Error extracting access token from JWT", e);
        }
        return null;
    }

    /**
     * Result of querying Keycloak UMA.
     * GRANTED = permission was granted by Keycloak.
     * DENIED = Keycloak explicitly denied the permission.
     * ERROR = Keycloak was unreachable or threw an error (should fall back to cache).
     */
    private enum UmaResult { GRANTED, DENIED, ERROR }

    private UmaResult queryKeycloakForPermission(String accessToken, String resourceName, String scope) {
        try {
            if (authzClient == null) {
                logger.warn("AuthzClient not initialized, skipping Keycloak policy evaluation");
                return UmaResult.ERROR;
            }

            AuthorizationRequest request = new AuthorizationRequest();
            request.setAudience(clientId);
            // addPermission(resourceName, scope) — resource and scope as SEPARATE arguments
            request.addPermission(resourceName, scope);

            AuthorizationResponse response = authzClient.authorization(accessToken).authorize(request);

            if (response != null && response.getToken() != null) {
                logger.debug("Keycloak granted permission for {}:{}", resourceName, scope);
                return UmaResult.GRANTED;
            }

            logger.debug("Keycloak denied permission for {}:{}", resourceName, scope);
            return UmaResult.DENIED;

        } catch (org.keycloak.authorization.client.util.HttpResponseException e) {
            // HTTP 403 = explicit denial from Keycloak
            if (e.getStatusCode() == 403) {
                logger.info("Keycloak UMA explicitly denied {}:{}", resourceName, scope);
                return UmaResult.DENIED;
            }
            logger.warn("Keycloak UMA HTTP error for {}:{} - status={} {}", resourceName, scope, e.getStatusCode(), e.getMessage());
            return UmaResult.ERROR;
        } catch (Exception e) {
            logger.warn("Keycloak authorization request failed for {}:{} - {}", resourceName, scope, e.getMessage());
            return UmaResult.ERROR;
        }
    }

    private Set<String> queryKeycloakForGrantedScopes(String accessToken, String resourceName) {
        Set<String> grantedScopes = new HashSet<>();

        try {
            if (authzClient == null) {
                logger.warn("AuthzClient not initialized, skipping granted scopes query");
                return grantedScopes;
            }

            AuthorizationRequest request = new AuthorizationRequest();
            request.setAudience(clientId);
            request.addPermission(resourceName);

            AuthorizationResponse response = authzClient.authorization(accessToken).authorize(request);

            if (response != null && response.getToken() != null) {
                grantedScopes = extractScopesFromAuthToken(response.getToken());
            }

        } catch (Exception e) {
            logger.warn("Error querying Keycloak for granted scopes: {}", e.getMessage());
        }

        return grantedScopes;
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractScopesFromAuthToken(String authToken) {
        Set<String> scopes = new HashSet<>();

        try {
            String[] tokenParts = authToken.split("\\.");
            if (tokenParts.length >= 2) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(tokenParts[1]));
                Map<String, Object> claims = new com.fasterxml.jackson.databind.ObjectMapper().readValue(payload, Map.class);

                Map<String, Object> authorization = (Map<String, Object>) claims.get("authorization");
                if (authorization != null) {
                    List<Map<String, Object>> permissions = (List<Map<String, Object>>) authorization.get("permissions");
                    if (permissions != null) {
                        for (Map<String, Object> permission : permissions) {
                            List<String> permissionScopes = (List<String>) permission.get("scopes");
                            if (permissionScopes != null) {
                                scopes.addAll(permissionScopes);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing authorization token", e);
        }

        return scopes;
    }

    public String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                return jwt.getSubject();
            }
        } catch (Exception e) {
            logger.error("Error extracting user ID from JWT", e);
        }
        return null;
    }

    public Set<String> getCurrentUserRoles() {
        Set<String> roles = new HashSet<>();

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
                if (realmAccess != null && realmAccess.containsKey("roles")) {
                    @SuppressWarnings("unchecked")
                    List<String> rolesList = (List<String>) realmAccess.get("roles");
                    roles.addAll(rolesList);
                }
            }
        } catch (Exception e) {
            logger.error("Error extracting roles from JWT", e);
        }

        return roles;
    }
}
