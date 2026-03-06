package com.cmips.service;

import com.cmips.model.UserRole;
import com.cmips.util.RoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class KeycloakAuthorizationService {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakAuthorizationService.class);

    /**
     * @deprecated Use @RequirePermission annotation instead. This method is kept for backward compatibility
     * but should be phased out in favor of Keycloak Authorization Services.
     */
    @Deprecated
    public boolean hasPermission(String accessToken, String resourceName, String scope) {
        return hasTimesheetPermission(accessToken, scope);
    }

    /**
     * @deprecated Use @RequirePermission annotation instead. This method duplicates
     * what Keycloak Authorization Services already handle via policies.
     * Kept only for backward compatibility during migration.
     */
    @Deprecated
    public boolean hasTimesheetPermission(String accessToken, String scope) {
        try {
            Set<UserRole> userRoles = extractUserRoles();
            if (userRoles.isEmpty()) {
                logger.warn("No roles found in JWT token");
                return false;
            }

            logger.debug("Checking permission for scope: {} with roles: {}", scope, userRoles);

            // Check permissions based on normalized roles and scope
            boolean permitted = switch (scope) {
                case "create", "submit" -> userRoles.contains(UserRole.PROVIDER);
                case "read" -> userRoles.contains(UserRole.PROVIDER)
                            || userRoles.contains(UserRole.RECIPIENT)
                            || userRoles.contains(UserRole.CASE_WORKER)
                            || userRoles.contains(UserRole.SUPERVISOR)
                            || userRoles.contains(UserRole.ADMIN);
                case "approve" -> userRoles.contains(UserRole.RECIPIENT)
                               || userRoles.contains(UserRole.CASE_WORKER)
                               || userRoles.contains(UserRole.SUPERVISOR);
                case "reject" -> userRoles.contains(UserRole.CASE_WORKER)
                              || userRoles.contains(UserRole.SUPERVISOR)
                              || userRoles.contains(UserRole.ADMIN);
                default -> {
                    logger.warn("Unknown scope: {}", scope);
                    yield false;
                }
            };

            if (permitted) {
                logger.debug("✅ Access granted for scope: {} with roles: {}", scope, userRoles);
            } else {
                logger.warn("❌ Access denied for scope: {} with roles: {}", scope, userRoles);
            }
            return permitted;

        } catch (Exception e) {
            logger.error("Error checking permission for scope '{}': {}", scope, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Extracts and normalizes user roles from the current JWT token using RoleMapper.
     * @return Set of normalized UserRole enums
     */
    public Set<UserRole> extractUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            logger.warn("No valid JWT token found");
            return Set.of();
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        List<String> roles = extractRolesFromJwt(jwt);

        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }

        // Normalize all roles through RoleMapper
        return roles.stream()
            .map(RoleMapper::map)
            .collect(Collectors.toSet());
    }

    /**
     * Extracts raw role strings from JWT, trying multiple claim locations.
     */
    @SuppressWarnings("unchecked")
    private List<String> extractRolesFromJwt(Jwt jwt) {
        // Method 1: Try realm_access.roles (primary location)
        try {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                List<String> roles = (List<String>) realmAccess.get("roles");
                if (roles != null && !roles.isEmpty()) {
                    logger.debug("Roles from realm_access: {}", roles);
                    return roles;
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to extract roles from realm_access: {}", e.getMessage());
        }

        // Method 2: Try resource_access.cmips-frontend.roles
        try {
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null) {
                Map<String, Object> cmipsFrontend = (Map<String, Object>) resourceAccess.get("cmips-frontend");
                if (cmipsFrontend != null && cmipsFrontend.containsKey("roles")) {
                    List<String> roles = (List<String>) cmipsFrontend.get("roles");
                    if (roles != null && !roles.isEmpty()) {
                        logger.debug("Roles from resource_access.cmips-frontend: {}", roles);
                        return roles;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to extract roles from resource_access: {}", e.getMessage());
        }

        // Method 3: Try direct roles claim (fallback)
        try {
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null && !roles.isEmpty()) {
                logger.debug("Roles from direct roles claim: {}", roles);
                return roles;
            }
        } catch (Exception e) {
            logger.warn("Failed to extract roles from direct roles claim: {}", e.getMessage());
        }

        return List.of();
    }

    /**
     * Checks if the user has a specific role (normalized through RoleMapper).
     *
     * @param roles Set of user roles (raw strings from JWT)
     * @param role The role to check (will be normalized)
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(Set<String> roles, String role) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        UserRole targetRole = RoleMapper.map(role);
        return roles.stream()
            .map(RoleMapper::map)
            .anyMatch(r -> r == targetRole);
    }

    /**
     * Checks if the user has a specific UserRole.
     *
     * @param roles Set of normalized UserRole enums
     * @param role The UserRole to check
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(Set<UserRole> roles, UserRole role) {
        return roles != null && roles.contains(role);
    }

    /**
     * Checks if the user has any of the specified roles (normalized through RoleMapper).
     *
     * @param roles Set of user roles (raw strings from JWT)
     * @param roleNames The roles to check (will be normalized)
     * @return true if user has any of the roles, false otherwise
     */
    public boolean hasAnyRole(Set<String> roles, String... roleNames) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        Set<UserRole> normalizedUserRoles = roles.stream()
            .map(RoleMapper::map)
            .collect(Collectors.toSet());

        for (String roleName : roleNames) {
            UserRole targetRole = RoleMapper.map(roleName);
            if (normalizedUserRoles.contains(targetRole)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the user has any of the specified UserRoles.
     *
     * @param roles Set of normalized UserRole enums
     * @param targetRoles The UserRoles to check
     * @return true if user has any of the roles, false otherwise
     */
    public boolean hasAnyRole(Set<UserRole> roles, UserRole... targetRoles) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        for (UserRole targetRole : targetRoles) {
            if (roles.contains(targetRole)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the primary (highest priority) role for the current user.
     * Priority: ADMIN > SUPERVISOR > CASE_WORKER > PROVIDER > RECIPIENT
     */
    public UserRole getPrimaryRole() {
        Set<UserRole> roles = extractUserRoles();
        if (roles.contains(UserRole.ADMIN)) return UserRole.ADMIN;
        if (roles.contains(UserRole.SUPERVISOR)) return UserRole.SUPERVISOR;
        if (roles.contains(UserRole.CASE_WORKER)) return UserRole.CASE_WORKER;
        if (roles.contains(UserRole.PROVIDER)) return UserRole.PROVIDER;
        if (roles.contains(UserRole.RECIPIENT)) return UserRole.RECIPIENT;
        if (roles.contains(UserRole.SYSTEM_SCHEDULER)) return UserRole.SYSTEM_SCHEDULER;
        return UserRole.defaultRole();
    }
}
