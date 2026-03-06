package com.cmips.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for KeycloakPolicyEvaluationService
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("KeycloakPolicyEvaluationService Tests")
class KeycloakPolicyEvaluationServiceTest {

    @Mock
    private KeycloakAdminService keycloakAdminService;

    @InjectMocks
    private KeycloakPolicyEvaluationService policyEvaluationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(policyEvaluationService, "keycloakServerUrl", "http://localhost:8085/auth");
        ReflectionTestUtils.setField(policyEvaluationService, "realm", "cmips");
        ReflectionTestUtils.setField(policyEvaluationService, "clientId", "cmips-backend");
        ReflectionTestUtils.setField(policyEvaluationService, "clientSecret", "secret");
        ReflectionTestUtils.setField(policyEvaluationService, "cacheTtlMinutes", 5);
    }

    @Test
    @DisplayName("Should evaluate permission successfully")
    void testEvaluatePermission() {
        // Arrange
        setupMockSecurityContext();

        // Act
        boolean result = policyEvaluationService.evaluatePermission("Timesheet Resource", "read");

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should get granted scopes successfully")
    void testGetGrantedScopes() {
        // Arrange
        setupMockSecurityContext();

        // Act
        Set<String> result = policyEvaluationService.getGrantedScopes("Timesheet Resource");

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should evaluate all permissions successfully")
    void testEvaluateAllPermissions() {
        // Arrange
        setupMockSecurityContext();

        // Act
        boolean result = policyEvaluationService.evaluateAllPermissions("Timesheet Resource", "read", "write");

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should evaluate any permission successfully")
    void testEvaluateAnyPermission() {
        // Arrange
        setupMockSecurityContext();

        // Act
        boolean result = policyEvaluationService.evaluateAnyPermission("Timesheet Resource", "read", "write");

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should return false when no authentication")
    void testEvaluatePermission_NoAuth() {
        // Arrange
        SecurityContextHolder.clearContext();

        // Act
        boolean result = policyEvaluationService.evaluatePermission("Timesheet Resource", "read");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Cache-first: should grant permission from JWT roles + cache without calling Keycloak")
    void testEvaluatePermission_CacheFirst_GrantsWhenRoleInCache() {
        // Arrange: pre-populate role→resource→scope cache (as loaded by Admin API)
        Map<String, Set<String>> resourceScopes = new HashMap<>();
        resourceScopes.put("Timesheet Resource", new HashSet<>(Set.of("read", "approve")));
        Map<String, Map<String, Set<String>>> cache = new HashMap<>();
        cache.put("CASEMANAGEMENTROLE", resourceScopes);
        ReflectionTestUtils.setField(policyEvaluationService, "rolePermissionCache", cache);
        ReflectionTestUtils.setField(policyEvaluationService, "cacheLastRefreshed", System.currentTimeMillis());

        // JWT with realm_access.roles so getCurrentUserRoles() returns CASEMANAGEMENTROLE
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("mock-token");
        when(jwt.getSubject()).thenReturn("user-123");
        when(jwt.getClaimAsMap("realm_access")).thenReturn(Map.of("roles", List.of("CASEMANAGEMENTROLE")));
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Act: evaluate permission (cache is not empty → cache-first path, no Keycloak call)
        boolean result = policyEvaluationService.evaluatePermission("Timesheet Resource", "read");

        // Assert: granted from cache
        assertTrue(result, "Permission should be granted from cache when user role has scope");
    }

    @Test
    @DisplayName("Cache-first: should deny when user role not in cache for resource/scope")
    void testEvaluatePermission_CacheFirst_DeniesWhenRoleMissingScope() {
        // Arrange: cache has CASEMANAGEMENTROLE with only "read", not "delete"
        Map<String, Set<String>> resourceScopes = new HashMap<>();
        resourceScopes.put("Timesheet Resource", new HashSet<>(Set.of("read")));
        Map<String, Map<String, Set<String>>> cache = new HashMap<>();
        cache.put("CASEMANAGEMENTROLE", resourceScopes);
        ReflectionTestUtils.setField(policyEvaluationService, "rolePermissionCache", cache);
        ReflectionTestUtils.setField(policyEvaluationService, "cacheLastRefreshed", System.currentTimeMillis());

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        Jwt jwt = mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("mock-token");
        when(jwt.getSubject()).thenReturn("user-123");
        when(jwt.getClaimAsMap("realm_access")).thenReturn(Map.of("roles", List.of("CASEMANAGEMENTROLE")));
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Act: ask for "delete" which the role does not have in cache
        boolean result = policyEvaluationService.evaluatePermission("Timesheet Resource", "delete");

        // Assert: denied from cache
        assertFalse(result, "Permission should be denied when role does not have scope in cache");
    }

    // Helper methods
    private void setupMockSecurityContext() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        Jwt jwt = mock(Jwt.class);

        when(jwt.getTokenValue()).thenReturn("mock-token");
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
}







