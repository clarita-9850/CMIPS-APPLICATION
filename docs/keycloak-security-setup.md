# Keycloak Security Setup - CMIPS Application

This document covers the full re-enablement of Keycloak-based authentication and authorization across the CMIPS application stack. Security was previously disabled for development convenience; this work restores it with proper IHSS-appropriate roles, test users, and a working permission cache.

---

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [What Was Changed](#what-was-changed)
4. [Realm Configuration](#realm-configuration)
5. [Roles and Permissions](#roles-and-permissions)
6. [Test Users](#test-users)
7. [Frontend Auth Flow](#frontend-auth-flow)
8. [Backend Authorization Flow](#backend-authorization-flow)
9. [Permission Cache System](#permission-cache-system)
10. [Bug Fixes During Implementation](#bug-fixes-during-implementation)
11. [Deployment Steps](#deployment-steps)
12. [Verification](#verification)
13. [Troubleshooting](#troubleshooting)
14. [Files Modified](#files-modified)

---

## Overview

The CMIPS application uses Keycloak as its identity and access management (IAM) provider. The security stack covers:

- **Authentication**: Keycloak issues JWT tokens via OpenID Connect (OIDC)
- **Frontend route protection**: React `RequireAuth` and `ProtectedRoute` components guard pages
- **Backend API protection**: Spring Security validates JWTs; a custom `@RequirePermission` annotation + `AuthorizationAspect` enforces resource-level access control
- **API Gateway**: Validates JWT tokens before proxying to backend services

Previously, all of this was bypassed:
- Frontend: `REACT_APP_DISABLE_AUTH=true` prevented Keycloak initialization; `RequireAuth` and `ProtectedRoute` returned children directly
- Gateway: Missing client secret caused token exchange failures
- The realm config only had 3 generic roles (`CASE_WORKER`, `RECIPIENT`, `PROVIDER`) that didn't match the IHSS workflow

---

## Architecture

```
Browser (localhost:3000)
    |
    | [Keycloak OIDC login - PKCE S256]
    v
Keycloak (localhost:8080)
    |
    | [JWT access token]
    v
React Frontend (cmips-frontend client)
    |
    | [Authorization: Bearer <token>]
    v
API Gateway (localhost:8090)
    |
    | [JWT validation + proxy]
    v
Spring Boot Backend (localhost:8081)
    |
    | [@RequirePermission -> AuthorizationAspect -> KeycloakPolicyEvaluationService]
    v
Permission Cache (role -> resource -> scopes)
    |
    | [Built from Keycloak Admin API on startup, refreshed every 5 min]
    v
Keycloak Authorization Services (resources, policies, permissions)
```

---

## What Was Changed

### 1. Keycloak Realm Config (`backend/keycloak-complete-config.json`)

Complete replacement merging the reference app (`CMIPS3.0-main 2/dev/keycloak/cmips-realm.json`) and the main app's authorization services config. Key additions:

- **5 IHSS-appropriate realm roles** (replacing the old 3 generic roles)
- **5 test users** with passwords, FID attributes, and role assignments
- **PKCE (S256)** on the `cmips-frontend` public client
- **Authorization services** on `cmips-backend` with resources, policies, and permissions
- **FID protocol mapper** for field-level authorization
- **Client roles mapper** and **audience resolver** in the `roles` client scope
- **Service account client roles** for `cmips-backend` to access Keycloak Admin API
- **Brute force protection**, **security headers**, **event logging**

### 2. Docker Compose (`docker-compose.yml`)

- `REACT_APP_DISABLE_AUTH: "true"` changed to `"false"` on `cmips-frontend`
- Added `GATEWAY_CLIENT_SECRET: X6282J5tQzu2tzqLcglKmjhwfidB0vh9` to `api-gateway`

### 3. Frontend Auth Guards

- **RequireAuth.js**: Removed `return <>{children}</>` bypass; restored real Keycloak auth flow
- **AuthContext.js**: Removed `return children;` bypass in `ProtectedRoute`; restored role checking

### 4. Setup Script (`setup_keycloak_resources.py`)

- Role reference changed from `CASE_WORKER` to `CM_WORKER`
- Policy name changed from `CaseWorker Full Access` to `CM Worker Full Access`

### 5. Backend Permission Cache (`KeycloakPolicyEvaluationService.java`)

Three code fixes to make the permission cache populate correctly:
- **API fallback** for resource/scope resolution when Keycloak list API returns minimal data
- **Role UUID resolution** using a realm roles lookup table (instead of using policy names as cache keys)
- **Aggregate policy recursion** to resolve composite policies to their constituent role policies

---

## Realm Configuration

### Realm-Level Settings

| Setting | Value |
|---------|-------|
| Realm name | `cmips` |
| SSL required | `none` (dev mode) |
| Registration allowed | `false` |
| Login with email | `true` |
| Brute force protection | `true` (5 failures, 15 min lockout) |
| Access token lifespan | 300s (5 min) |
| SSO session idle timeout | 1800s (30 min) |
| SSO session max lifespan | 36000s (10 hr) |
| Event logging | `LOGIN`, `LOGIN_ERROR`, `LOGOUT`, `REFRESH_TOKEN`, `UPDATE_PASSWORD` |
| Admin events | Enabled with details |

### Clients

#### cmips-backend (confidential)
- **Type**: Confidential client with client secret
- **Secret**: `X6282J5tQzu2tzqLcglKmjhwfidB0vh9`
- **Authorization services**: Enabled (resources, policies, permissions)
- **Service accounts**: Enabled (for Admin API access)
- **Direct access grants**: Enabled (for password grant in scripts)

#### cmips-frontend (public)
- **Type**: Public client (no secret)
- **PKCE**: S256 code challenge method
- **Redirect URIs**: `http://localhost:3000/*`, `http://localhost:5173/*`
- **Web origins**: `http://localhost:3000`, `http://localhost:5173`
- **Front-channel logout**: Enabled

### Client Scopes - `roles`

The `roles` client scope includes four protocol mappers:

| Mapper | Type | Purpose |
|--------|------|---------|
| `realm roles` | `oidc-usermodel-realm-role-mapper` | Maps realm roles to `realm_access.roles` in JWT |
| `client roles` | `oidc-usermodel-client-role-mapper` | Maps client roles to `resource_access.${client_id}.roles` in JWT |
| `audience resolve` | `oidc-audience-resolve-mapper` | Ensures correct `aud` claim in JWT |
| `fids-mapper` | `oidc-usermodel-attribute-mapper` | Maps user `fids` attribute to `fids` claim in JWT |

### Service Account Roles

The `cmips-backend` service account is granted the following `realm-management` client roles (required for the backend to call Keycloak Admin REST API):

- `manage-clients`
- `view-clients`
- `manage-authorization`
- `view-authorization`
- `view-users`
- `view-realm`

### Security Headers

```
Content-Security-Policy: frame-src 'self'; frame-ancestors 'self'; object-src 'none';
X-Content-Type-Options: nosniff
X-Frame-Options: SAMEORIGIN
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
```

---

## Roles and Permissions

### Realm Roles

| Role | Description | Typical Access |
|------|-------------|----------------|
| `CM_WORKER` | Case Management Worker | View/edit cases, submit approvals, manage providers |
| `CM_SUPERVISOR` | Case Management Supervisor | All CM_WORKER access + approve/reject, assign cases |
| `HELP_DESK` | Help Desk Support | Read-only case/evidence viewing |
| `ADMIN` | System Administrator | Full access to all resources and admin functions |
| `user` | Default user role | Base role assigned to all users |

### Authorization Resources (Keycloak)

Resources defined in `cmips-backend` authorization settings (imported via realm config + seeded via `setup_keycloak_resources.py`):

**From realm config:**
- Timesheet Resource (20 scopes including field-level)
- EVV Resource (CRUD)
- Provider-Recipient Resource (CRUD)
- Provider Resource (view, create, edit, approve, reinstate, enroll)

**From setup script (20 resources):**
- Provider Assignment Resource, Provider CORI Resource
- Overtime Violation Resource, Task Resource
- Case Resource (10 scopes), Case Notes Resource, Case Contacts Resource
- Case Management Resource, Referral Resource
- Recipient Resource, Recipient Waiver Resource
- Service Eligibility Resource, Health Care Certification Resource
- Application Resource, Batch Job Resource
- Work Queue Resource, Quality Assurance Resource
- Warrant Resource, Normal Login Resource

### Authorization Policies

| Policy | Type | Grants Access To |
|--------|------|-----------------|
| CM Worker Policy | Role | CM_WORKER role holders |
| CM Supervisor Policy | Role | CM_SUPERVISOR role holders |
| Admin Policy | Role | ADMIN role holders |
| Help Desk Policy | Role | HELP_DESK role holders |
| Worker or Supervisor Policy | Aggregate | CM_WORKER OR CM_SUPERVISOR |
| Any Authenticated User Policy | Aggregate | Any of the 4 roles above |
| CM Worker Full Access | Role | CM_WORKER (used by setup script for all resources) |

---

## Test Users

| Username | Password | Roles | FID Permissions |
|----------|----------|-------|-----------------|
| `admin` | `password` | ADMIN, CM_SUPERVISOR, CM_WORKER, user | All FIDs (case, evidence, approval, payment, admin) |
| `supervisor` | `password` | CM_SUPERVISOR, CM_WORKER, user | Case CRUD + assign/close, evidence + verify, approval full |
| `cmworker` | `password` | CM_WORKER, user | Case view/create/edit, evidence view/upload, approval submit |
| `helpdesk` | `password` | HELP_DESK, user | Case view, evidence view (read-only) |
| `caseworker1` | `password123` | CM_WORKER, user | Same as cmworker (backward compat with existing test data) |

### FID (Field Identifier) Attributes

FIDs control field-level authorization. Each user has a `fids` attribute containing a list of permission strings:

```
FID_CASE_VIEW, FID_CASE_CREATE, FID_CASE_EDIT, FID_CASE_DELETE,
FID_CASE_ASSIGN, FID_CASE_CLOSE, FID_EVIDENCE_VIEW, FID_EVIDENCE_UPLOAD,
FID_EVIDENCE_VERIFY, FID_EVIDENCE_DELETE, FID_APPROVAL_SUBMIT,
FID_APPROVAL_REVIEW, FID_APPROVAL_APPROVE, FID_APPROVAL_REJECT,
FID_APPROVAL_OVERRIDE, FID_PAYMENT_VIEW, FID_PAYMENT_CALCULATE,
FID_PAYMENT_AUTHORIZE, FID_PAYMENT_ISSUE, FID_ADMIN_USER_MANAGE,
FID_ADMIN_CONFIG_EDIT, FID_ADMIN_AUDIT_VIEW
```

---

## Frontend Auth Flow

### Keycloak Initialization

When `REACT_APP_DISABLE_AUTH` is `"false"`, the frontend initializes Keycloak on startup:

1. `keycloak.js` creates a Keycloak instance with the `cmips-frontend` client
2. `keycloak.init()` is called with `onLoad: 'check-sso'`
3. If authenticated, the JWT is stored and `AuthProvider` populates user state
4. If not authenticated, `RequireAuth` triggers `keycloak.login()`

### RequireAuth Component

**File**: `frontend new/src/auth/RequireAuth.js`

Wraps all protected routes. Flow:

```
Component renders
    |
    +--> Keycloak available?
    |       |
    |       +--> YES: Check authenticated?
    |       |       |
    |       |       +--> YES: Render children
    |       |       +--> NO: Show loading + trigger login() via useEffect
    |       |
    |       +--> NO (no-auth mode): Check localStorage
    |               |
    |               +--> Authenticated: Render children
    |               +--> Not authenticated: Navigate to /login
    |
    +--> Loading? Show AuthLoadingScreen (spinner)
```

### ProtectedRoute Component

**File**: `frontend new/src/auth/AuthContext.js`

Wraps routes that require specific roles. Flow:

```
ProtectedRoute({ roles: ["CM_WORKER", "CM_SUPERVISOR"] })
    |
    +--> Loading? Show loading spinner
    +--> Not authenticated? Show "Authentication Required"
    +--> roles specified?
    |       |
    |       +--> requireAll? hasAllRoles(roles) : hasAnyRole(roles)
    |       |       |
    |       |       +--> YES: Render children
    |       |       +--> NO: Show "Access Denied"
    |       |
    |       +--> No roles specified: Render children (auth-only check)
```

---

## Backend Authorization Flow

### Request Processing

```
HTTP Request with Authorization: Bearer <JWT>
    |
    v
Spring Security BearerTokenAuthenticationFilter
    | [Validates JWT signature against Keycloak JWKS endpoint]
    | [Extracts realm_access.roles -> ROLE_CM_WORKER, ROLE_ADMIN, etc.]
    v
Controller method with @RequirePermission(resource="Provider Resource", scope="view")
    |
    v
AuthorizationAspect.checkPermission()
    | [Intercepts @RequirePermission via AOP]
    v
KeycloakPolicyEvaluationService.evaluatePermission("Provider Resource", "view")
    |
    +--> Permission cache non-empty?
    |       |
    |       +--> YES (normal path): evaluateFromCache()
    |       |       | [Checks: for each user role, does cache[role][resource] contain scope?]
    |       |       +--> GRANTED or DENIED
    |       |
    |       +--> NO (fallback): queryKeycloakForPermission() via UMA
    |               | [Sends token to Keycloak UMA endpoint]
    |               +--> GRANTED or DENIED
    |
    v
AuthorizationAspect returns:
    - GRANTED: Proceed to controller method
    - DENIED: Return 403 {"error": "Access denied"}
```

### @RequirePermission Annotation

Controllers declare required permissions:

```java
@RequirePermission(resource = "Provider Resource", scope = "view")
@GetMapping
public ResponseEntity<?> getAllProviders(...) { ... }

@RequirePermission(resource = "Provider Resource", scope = "create")
@PostMapping
public ResponseEntity<?> createProvider(...) { ... }
```

---

## Permission Cache System

### How It Works

The `KeycloakPolicyEvaluationService` builds an in-memory cache on startup and refreshes it every 5 minutes. The cache structure is:

```
rolePermissionCache: Map<String, Map<String, Set<String>>>

Example:
  "CM_WORKER" -> {
    "Provider Resource" -> {"view", "create", "edit", "enroll", "reinstate"},
    "Case Resource" -> {"view", "create", "edit", ...},
    ...
  },
  "CM_SUPERVISOR" -> {
    "Provider Resource" -> {"view", "create", "edit", "approve", ...},
    "Timesheet Resource" -> {"approve", "reject", ...},
    ...
  },
  "HELP_DESK" -> {
    "Provider Resource" -> {"view"},
    ...
  },
  "ADMIN" -> {
    // same as CM_SUPERVISOR + admin resources
  }
```

### Cache Build Process

1. **Fetch resources** from Keycloak Admin API -> build `resourceId -> resourceName` map
2. **Fetch scopes** -> build `scopeId -> scopeName` map
3. **Fetch realm roles** -> build `roleId -> roleName` map (for UUID resolution)
4. **Fetch role policies** -> for each policy, parse `config.roles` JSON to extract role UUID, resolve to role name using the map from step 3
5. **Fetch permissions** -> for each permission:
   - Try to resolve resources/scopes from inline data
   - If empty, try parsing the permission name (`Permission-ROLE-Resource` convention)
   - If still empty, **call individual permission API endpoints** (`getPermissionResources`, `getPermissionScopes`)
   - Resolve roles from associated policies (including recursive resolution of aggregate policies)
   - Build cache entry: `cache[roleName][resourceName].add(scope)`

### Cache Evaluation

```java
private boolean evaluateFromCache(String resourceName, String scope, Set<String> userRoles) {
    for (String role : userRoles) {
        Map<String, Set<String>> resourceMap = rolePermissionCache.get(role);
        if (resourceMap != null) {
            Set<String> allowedScopes = resourceMap.get(resourceName);
            if (allowedScopes != null && allowedScopes.contains(scope)) {
                return true;  // GRANTED
            }
        }
    }
    return false;  // DENIED
}
```

---

## Bug Fixes During Implementation

### 1. FID Mapper JSON Type Error

**Problem**: Keycloak 23.0.0 threw `RuntimeException: cannot map type for token claim` on every login attempt.

**Root cause**: The `fids-mapper` protocol mapper was configured with `jsonType.label: "JSON"`. Keycloak 23 can't map multivalued user attributes as JSON type.

**Fix**: Changed `jsonType.label` from `"JSON"` to `"String"` in the fids-mapper config.

### 2. Service Account Missing Admin API Roles

**Problem**: Backend's permission cache failed with `Failed to get resources` -> `403 Forbidden` when calling Keycloak Admin REST API.

**Root cause**: The `cmips-backend` service account token (from `client_credentials` grant) didn't have `realm-management` client roles, so it couldn't access the Admin API.

**Fix**:
- Assigned `manage-clients`, `view-clients`, `manage-authorization`, `view-authorization`, `view-users`, `view-realm` roles to the service account
- Added `serviceAccountClientRoles` to realm config for persistence

### 3. Missing Client Roles Mapper

**Problem**: Even after assigning roles, the service account JWT didn't include `resource_access` claim (client roles were missing from the token).

**Root cause**: The custom `roles` client scope only had a `realm roles` mapper, not a `client roles` mapper. Client roles weren't being included in tokens.

**Fix**: Added `oidc-usermodel-client-role-mapper` and `oidc-audience-resolve-mapper` to the `roles` client scope.

### 4. Permission Cache Empty (0 roles, 0 mappings)

**Problem**: Cache loaded 23 resources but resolved 0 role-permission mappings. All permissions were skipped.

**Root cause (three issues)**:

a) **Resource/scope resolution**: Keycloak's list permissions API (`?first=0&max=500`) returns only `id`, `name`, `type` — no inline `resources` or `scopes` fields. The code's name-parsing fallback only handled `Permission-*` naming conventions, which didn't match our names like `"Provider Resource - Full Access"`.

b) **Role UUID resolution**: The `extractRoleNameFromPolicy` method couldn't resolve role UUIDs from `config.roles` (e.g., `[{"id":"c79434bf-..."}]`) to actual role names. It fell back to using the policy name (e.g., `"CM Worker Policy"`) as the cache key, which didn't match JWT role names (`"CM_WORKER"`).

c) **Aggregate policy resolution**: Permissions linked to aggregate policies (like `"Worker or Supervisor Policy"`) couldn't resolve to role names because aggregate policies aren't in the `policyIdToRoleName` map.

**Fix**:
- Added **API fallback** calls to `getPermissionResources()` and `getPermissionScopes()` when inline data and name parsing both fail
- Added a **realm roles lookup table** (`roleId -> roleName`) and used it in `extractRoleNameFromPolicy` to parse `config.roles` JSON and resolve UUIDs
- Added **aggregate policy recursion** in `resolveRoleNames` — when an associated policy has `type: "aggregate"`, recursively fetch its sub-policies and resolve each to a role name

---

## Deployment Steps

### Fresh Deployment

```bash
cd CMIPS-APPLICATION-main

# 1. Full teardown (clear existing Keycloak realm data)
docker compose down

# 2. Remove postgres volume so Keycloak reimports the realm
docker volume rm cmips-application-main_postgres_data

# 3. Build and start all services
docker compose up -d --build

# 4. Wait for Keycloak to be healthy (~30s)
docker compose ps  # verify keycloak shows "healthy"

# 5. Seed authorization resources (run from host with localhost URL)
sed 's|http://keycloak:8080|http://localhost:8080|g' setup_keycloak_resources.py | python3
```

### Backend-Only Rebuild

If only Java code changed:

```bash
docker compose build cmips-backend
docker compose up -d cmips-backend
```

### Frontend-Only Rebuild

```bash
docker compose build cmips-frontend
docker compose up -d cmips-frontend
```

### Keycloak Realm Changes

Any change to `backend/keycloak-complete-config.json` requires:

```bash
docker compose down
docker volume rm cmips-application-main_postgres_data
docker compose up -d
# Re-run setup script after Keycloak is healthy
```

---

## Verification

### Quick Smoke Test

```bash
# 1. Login as admin
curl -s -X POST http://localhost:8080/realms/cmips/protocol/openid-connect/token \
  -d "username=admin&password=password&grant_type=password&client_id=cmips-frontend"
# Should return JSON with access_token

# 2. Call API with token
TOKEN=<access_token from above>
curl -H "Authorization: Bearer $TOKEN" http://localhost:8090/api/providers
# Should return 200 with provider list

# 3. Call API without token
curl http://localhost:8090/api/providers
# Should return 401

# 4. Open browser
# http://localhost:3000 -> should redirect to Keycloak login page
```

### Full Test Matrix

| User | Login | GET /api/providers | Expected |
|------|-------|--------------------|----------|
| admin / password | 200 | 200 | Full access |
| supervisor / password | 200 | 200 | Supervisor + worker access |
| cmworker / password | 200 | 200 | Worker access |
| helpdesk / password | 200 | 200 | Read-only access |
| caseworker1 / password123 | 200 | 200 | Worker access (backward compat) |
| No token | N/A | 401 | Rejected |
| Invalid token | N/A | 401 | Rejected |

### Permission Cache Health

Check backend logs for successful cache build:

```bash
docker logs cmips-backend 2>&1 | grep "Permission cache refreshed"
# Should show: "Permission cache refreshed in Xms: 4 roles, 104 total resource-scope mappings"
```

If you see "0 roles, 0 mappings", check:
1. Keycloak is healthy and reachable from backend
2. Service account has realm-management roles
3. `setup_keycloak_resources.py` was run after Keycloak started

---

## Troubleshooting

### Frontend doesn't redirect to login

- Check `REACT_APP_DISABLE_AUTH` is `"false"` in docker-compose.yml
- Rebuild frontend: `docker compose build --no-cache cmips-frontend && docker compose up -d cmips-frontend`

### 401 on all API calls

- Verify Keycloak is healthy: `curl http://localhost:8080/health/ready`
- Check JWT issuer matches: the backend expects `http://keycloak:8080/realms/cmips` (internal Docker hostname)
- Verify `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` in docker-compose

### 403 on authenticated API calls

- Check backend logs: `docker logs cmips-backend 2>&1 | grep "Authorization"`
- If "Permission cache is empty": the cache didn't build. Check for `Failed to get resources` errors
- If "DENIED (cache)": the user's role doesn't have the required permission. Check the cache was built with the correct role mappings

### "Failed to get resources" in backend logs

- Service account can't access Keycloak Admin API
- Verify service account roles: check `serviceAccountClientRoles` in realm config
- Ensure `client roles` mapper exists in the `roles` client scope (so the token includes `resource_access.realm-management.roles`)

### Keycloak "unknown_error" on login

- Usually a protocol mapper error. Check Keycloak logs: `docker logs cmips-keycloak 2>&1 | grep ERROR`
- Common cause: `jsonType.label: "JSON"` on multivalued attribute mappers (must be `"String"`)

### Permission cache shows 0 roles

- Check that `setup_keycloak_resources.py` was run after Keycloak started
- Check that the `extractRoleNameFromPolicy` fix is in place (role UUID resolution)
- Restart backend: `docker restart cmips-backend`

---

## Files Modified

| File | Description |
|------|-------------|
| `backend/keycloak-complete-config.json` | Complete Keycloak realm configuration with roles, users, clients, authorization, security settings |
| `docker-compose.yml` | Enabled frontend auth (`REACT_APP_DISABLE_AUTH: "false"`), added gateway client secret |
| `frontend new/src/auth/RequireAuth.js` | Removed auth bypass, restored Keycloak authentication flow |
| `frontend new/src/auth/AuthContext.js` | Removed ProtectedRoute bypass, restored role-based access control |
| `setup_keycloak_resources.py` | Updated role reference `CASE_WORKER` -> `CM_WORKER`, policy name update |
| `backend/src/main/java/com/cmips/service/KeycloakPolicyEvaluationService.java` | Fixed permission cache: API fallback, role UUID resolution, aggregate policy recursion |
