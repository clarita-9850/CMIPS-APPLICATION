package com.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final List<String> ALLOWED_ORIGINS = List.of(
        "http://localhost:3000", "http://localhost:3001",
        "http://127.0.0.1:3000", "http://127.0.0.1:3001"
    );

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            // Disable Spring Security CORS - our custom CorsGlobalFilter handles it at highest priority
            .cors(cors -> cors.disable())
            .authorizeExchange(exchanges -> exchanges
                // CRITICAL: Permit ALL OPTIONS requests FIRST (before any other rules)
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Public auth endpoints - no authentication required
                .pathMatchers("/api/auth/**").permitAll()
                // Actuator endpoints - public for health checks
                .pathMatchers("/actuator/**").permitAll()
                // All other endpoints (including legacy routes without /api/ prefix) require authentication
                .anyExchange().authenticated()
            )
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtDecoder(jwtDecoder()))
            );

        return http.build();
    }

    /**
     * Global CORS WebFilter that runs BEFORE Spring Security.
     * This ensures CORS headers are added to ALL responses including 401/403 from Spring Security.
     * Without this, the browser blocks reading 401 responses (no Access-Control-Allow-Origin),
     * preventing the frontend httpClient from detecting token expiry and refreshing.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebFilter corsGlobalFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            String origin = exchange.getRequest().getHeaders().getOrigin();

            if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
                ServerHttpResponse response = exchange.getResponse();
                response.getHeaders().set("Access-Control-Allow-Origin", origin);
                response.getHeaders().set("Access-Control-Allow-Credentials", "true");
                response.getHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
                response.getHeaders().set("Access-Control-Allow-Headers", "*");
                response.getHeaders().set("Access-Control-Max-Age", "3600");

                // Short-circuit OPTIONS preflight
                if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
                    response.setStatusCode(HttpStatus.OK);
                    return Mono.empty();
                }
            }

            return chain.filter(exchange);
        };
    }

    @Bean
    public org.springframework.security.oauth2.jwt.ReactiveJwtDecoder jwtDecoder() {
        return org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
                .withIssuerLocation("http://cmips-keycloak:8080/realms/cmips")
                .build();
    }
}
