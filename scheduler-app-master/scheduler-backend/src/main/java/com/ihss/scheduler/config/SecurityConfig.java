package com.ihss.scheduler.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${keycloak.external-issuer-uri:}")
    private String externalIssuerUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/actuator/health",
                    "/actuator/info",
                    "/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()

                // All scheduler endpoints require authentication
                // Fine-grained authorization is handled by @RequirePermission at method level
                .requestMatchers("/api/scheduler/**").authenticated()

                // All other requests need authentication
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(multiIssuerJwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    /**
     * Custom JwtDecoder that accepts tokens from multiple issuers.
     * This handles cases where tokens come from different network paths:
     * - Through API Gateway (may use https, external hostname)
     * - Direct internal calls (http, docker network hostname)
     * - Local development (localhost)
     */
    @Bean
    public JwtDecoder multiIssuerJwtDecoder() {
        // Build JWK Set URI from issuer
        String jwkSetUri = issuerUri + "/protocol/openid-connect/certs";
        log.info("Configuring JwtDecoder with JWK Set URI: {}", jwkSetUri);

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        // Configure custom JWT validator that accepts multiple issuer formats
        decoder.setJwtValidator(token -> {
            String tokenIssuer = token.getIssuer() != null ? token.getIssuer().toString() : "";
            log.debug("JWT Issuer from token: {}", tokenIssuer);

            // List of valid issuers - supports http/https and different hostnames
            List<String> validIssuers = Arrays.asList(
                // Docker network hostnames
                "http://cmips-keycloak:8080/realms/cmips",
                "https://cmips-keycloak:8080/realms/cmips",
                "http://cmips-keycloak/realms/cmips",
                "https://cmips-keycloak/realms/cmips",
                // Legacy hostname (for backward compatibility)
                "http://keycloak:8080/realms/cmips",
                "https://keycloak:8080/realms/cmips",
                // Local development
                "http://localhost:8085/realms/cmips",
                "https://localhost:8085/realms/cmips",
                // Configured issuers
                issuerUri,
                externalIssuerUri
            );

            // Check if token issuer matches any valid issuer (case-insensitive)
            if (validIssuers.stream()
                    .filter(v -> v != null && !v.isEmpty())
                    .anyMatch(valid -> valid.equalsIgnoreCase(tokenIssuer))) {
                log.debug("JWT Issuer validated successfully: {}", tokenIssuer);
                return OAuth2TokenValidatorResult.success();
            }

            // Fallback: Accept any issuer containing "cmips-keycloak" or "keycloak" and "/realms/cmips"
            // This handles edge cases like different ports or protocols
            if ((tokenIssuer.contains("cmips-keycloak") || tokenIssuer.contains("keycloak"))
                    && tokenIssuer.contains("/realms/cmips")) {
                log.debug("JWT Issuer accepted via pattern match: {}", tokenIssuer);
                return OAuth2TokenValidatorResult.success();
            }

            log.error("JWT Issuer validation failed. Token issuer: {}, Expected one of: {}", tokenIssuer, validIssuers);
            return OAuth2TokenValidatorResult.failure(
                new OAuth2Error("invalid_issuer", "Invalid issuer: " + tokenIssuer, null)
            );
        });

        return decoder;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3002",
            "http://localhost:3000",
            "http://localhost:3001"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakGrantedAuthoritiesConverter());
        return converter;
    }

    /**
     * Custom converter to extract roles from Keycloak JWT token.
     * Keycloak stores roles in different locations:
     * - realm_access.roles: Realm-level roles
     * - resource_access.{client-id}.roles: Client-level roles
     */
    static class KeycloakGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            // Extract realm roles
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<String> realmRoles = (List<String>) realmAccess.get("roles");
                authorities.addAll(realmRoles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList()));
            }

            // Extract client roles (for batch-scheduler-app client)
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get("batch-scheduler-app");
                if (clientAccess != null && clientAccess.containsKey("roles")) {
                    @SuppressWarnings("unchecked")
                    List<String> clientRoles = (List<String>) clientAccess.get("roles");
                    authorities.addAll(clientRoles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                        .collect(Collectors.toList()));
                }
            }

            // Map common roles to scheduler roles
            // If user has ADMIN role, also give them SCHEDULER_ADMIN
            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                authorities.add(new SimpleGrantedAuthority("ROLE_SCHEDULER_ADMIN"));
            }

            return authorities;
        }
    }
}
