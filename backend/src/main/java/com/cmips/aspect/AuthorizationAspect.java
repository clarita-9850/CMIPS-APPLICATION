package com.cmips.aspect;

import com.cmips.annotation.RequirePermission;
import com.cmips.service.KeycloakPolicyEvaluationService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Authorization Aspect for method-level security using Keycloak policies
 *
 * This aspect intercepts methods annotated with @RequirePermission and
 * evaluates authorization using Keycloak's policy engine before allowing
 * the method to execute.
 */
@Aspect
@Component
public class AuthorizationAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationAspect.class);

    @Autowired
    private KeycloakPolicyEvaluationService keycloakPolicyEvaluationService;

    /**
     * Around advice for @RequirePermission annotation
     * Evaluates authorization using Keycloak before method execution
     */
    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {

        String resource = requirePermission.resource();
        String scope = requirePermission.scope();
        String methodName = joinPoint.getSignature().getName();
        String userId = keycloakPolicyEvaluationService.getCurrentUserId();
        Set<String> userRoles = keycloakPolicyEvaluationService.getCurrentUserRoles();

        logger.info("Authorization check: user={} method={} resource={}:{} roles={}", userId, methodName, resource, scope, userRoles);

        // Query Keycloak for authorization decision
        boolean hasPermission = keycloakPolicyEvaluationService.evaluatePermission(resource, scope);

        if (hasPermission) {
            logger.info("Authorization GRANTED: user={} method={} resource={}:{}", userId, methodName, resource, scope);
            return joinPoint.proceed();
        } else {
            logger.warn("Authorization DENIED: user={} method={} resource={}:{} roles={}", userId, methodName, resource, scope, userRoles);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("error", "Access denied", "message", requirePermission.message()));
        }
    }
}
