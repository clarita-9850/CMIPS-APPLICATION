package com.ihss.scheduler.util;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Utility class for security-related operations.
 * Provides helper methods that work both with and without security enabled.
 */
public class SecurityUtils {

    private static final String DEFAULT_TEST_USER = "test-user";

    /**
     * Extracts the username from a JWT token.
     * Returns a default test user when security is disabled (jwt is null).
     *
     * @param jwt The JWT token (can be null when security is disabled)
     * @return The username from the token or a default test user
     */
    public static String getUsername(Jwt jwt) {
        if (jwt == null) {
            return DEFAULT_TEST_USER;
        }
        String username = jwt.getClaimAsString("preferred_username");
        return username != null ? username : DEFAULT_TEST_USER;
    }
}
