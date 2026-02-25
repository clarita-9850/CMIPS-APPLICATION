# CMIPS Authentication & Authorization — Request Flow

## 1. Scope

- **Authentication:** “Who is this?” — prove identity and get tokens (login) and validate the token on each request.
- **Authorization:** “Is this user allowed to do this?” — check resource/scope using roles and permissions (no password, no login; uses the token and cached permission data).

---

## 2. High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                    BROWSER (User)                                         │
│  • Runs Frontend (React CRA)                                                              │
│  • Stores: access_token, refresh_token (e.g. localStorage)                                │
│  • Sends: Authorization: Bearer <access_token> on every API call                         │
└────────────────────────────┬────────────────────────────────────────────────────────────┘
                              │
                              │  All API requests go to Gateway (e.g. https://host:8090)
                              ▼
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                              API GATEWAY (Spring Cloud Gateway)                           │
│  • Single entry for all /api/*                                                            │
│  • /api/auth/* → no JWT required; forward to Backend                                     │
│  • Other /api/* → require valid JWT; validate locally (JWK); add X-User-*; forward      │
└────────────────────────────┬────────────────────────────────────────────────────────────┘
                              │
                              │  Forwards to Backend (e.g. http://cmips-backend:8081)
                              ▼
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                              BACKEND (Spring Boot)                                       │
│  • Auth endpoints: proxy to Keycloak (token, refresh, logout)                            │
│  • Other endpoints: validate JWT locally; check permission (cache); run controller      │
└────────────────────────────┬────────────────────────────────────────────────────────────┘
                              │
         ┌────────────────────┼────────────────────┐
         │                    │                    │
         │  (auth only)       │  (no call          │  (periodic cache
         ▼                    │   per request)      │   refresh)
┌─────────────────┐           │                    ▼
│    KEYCLOAK     │           │           ┌─────────────────┐
│  • Issue tokens │           │           │ In-memory        │
│  • Validate     │           │           │ permission cache │
│    credentials  │           │           │ (role→resource   │
│  • JWK Set      │           │           │  →scopes)        │
└─────────────────┘           │           └─────────────────┘
                              │
                              ▼
                    Controller runs → DB / business logic → response
```

---

## 3. Frontend in Use

The CMIPS UI used for these flows is the **React (Create React App)** frontend at `frontend new` (`cmipsapplication/frontend new`). It uses `react-scripts`, React Router, and Keycloak JS. All API calls go through the API Gateway (e.g. `REACT_APP_API_BASE_URL=http://localhost:8090`). A separate Next.js frontend exists in the repo (`frontend-nextjs`) but is not the one described here.

---

## 4. Components and Ports

| Component | Role | Port (external) | Port (internal / Docker) |
|-----------|------|-----------------|---------------------------|
| **Frontend** | React (CRA) UI; calls Gateway only | 3000 | — |
| **API Gateway** | Single entry point; routing; JWT validation | 8090 | 8080 |
| **Backend** | Spring Boot APIs; auth proxy to Keycloak | — | 8081 |
| **Keycloak** | Identity and tokens; permissions (UMA) | 8080 | 8080 |

The browser never talks to the Backend or Keycloak directly — only to the Frontend (3000) and the API Gateway (8090).

---

# PART A — AUTHENTICATION

## A.1 What Authentication Achieves

- User proves identity with **username + password**.
- Server (Keycloak) returns **access_token** (JWT) and **refresh_token**.
- Later requests prove “same user” by sending the **access_token**; nobody re-sends the password.
- **Validation** of the token on each request is done **locally** using public keys (JWK), so every request does **not** hit Keycloak.

---

## A.2 Login Flow — Step-by-Step (Theory + Technique)

### Step 1: User submits credentials in the browser

- **Theoretical:** User proves identity by something only they know (password).
- **Technical:** React app (e.g. in `frontend new`) sends:
  - **Method:** `POST`
  - **URL:** `{API_BASE}/api/auth/login` (e.g. `http://localhost:8090/api/auth/login`)
  - **Body:** `{ "username": "...", "password": "..." }`
  - **Headers:** `Content-Type: application/json`
- **No** `Authorization` header; this is the login request itself.

---

### Step 2: Request hits API Gateway

- **Theoretical:** One entry point for all APIs; gateway can enforce CORS, routing, and “this path is public”.
- **Technical:**
  - Gateway receives `POST /api/auth/login`.
  - Route: path `/api/auth/**` → upstream `http://cmips-backend:8081`.
  - Security: `pathMatchers("/api/auth/**").permitAll()` → **no JWT check**.
  - Gateway forwards the same method, path, and body to the backend. **Keycloak is not called at the gateway.**

---

### Step 3: Backend receives login and calls Keycloak

- **Theoretical:** Backend does not store passwords; it delegates “verify user and issue tokens” to Keycloak (IdP).
- **Technical:**
  - Backend `AuthController` receives `POST /api/auth/login` and body.
  - Builds a **form-urlencoded** request to Keycloak:
    - **URL:** `{keycloak.auth-server-url}realms/{realm}/protocol/openid-connect/token`
    - **Body:** `grant_type=password`, `client_id=...`, `client_secret=...`, `username=...`, `password=...`
  - Backend performs **HTTP POST** to Keycloak (backend → Keycloak, e.g. port 8080).

---

### Step 4: Keycloak validates and returns tokens

- **Theoretical:** Keycloak checks user + password and client credentials; if valid, it issues tokens and records session.
- **Technical:**
  - Keycloak validates username/password against its user store.
  - Validates `client_id` and `client_secret` for the configured client (e.g. `cmips-frontend` or the one in config).
  - **If valid:** HTTP 200 with JSON, e.g.:
    - `access_token` (JWT)
    - `refresh_token` (opaque)
    - `expires_in`, `token_type`, `scope`
  - **If invalid:** HTTP 401; backend will forward an error to the client.
  - JWT contents (typical): `sub` (user id), `preferred_username`, `realm_access.roles`, `exp`, `iss`, etc.

---

### Step 5: Backend returns Keycloak response to Gateway

- **Theoretical:** Backend acts as a proxy for auth; it does not create tokens itself.
- **Technical:** Backend returns the same status and JSON body it received from Keycloak (200 + tokens or 401 + error).

---

### Step 6: Gateway forwards response to browser

- **Theoretical:** Gateway is transparent for auth responses; CORS is applied so the browser accepts the response.
- **Technical:** Gateway forwards backend response; may deduplicate CORS headers. No token storage at gateway.

---

### Step 7: Frontend stores tokens and updates UI

- **Theoretical:** Tokens are kept in the client; the access token is the proof of identity for subsequent requests.
- **Technical:**
  - Frontend reads `access_token` and `refresh_token` from the response body.
  - Stores them (e.g. `localStorage.setItem('token', accessToken)` and same for refresh).
  - May decode the JWT (client-side only) to show username/roles; **no** server call for “who am I?” at this step.
  - On error (e.g. 401), show “Invalid credentials” or similar.

---

## A.3 Login Flow — Single Diagram

```
  BROWSER              API GATEWAY              BACKEND                 KEYCLOAK
     │                       │                       │                       │
     │  POST /api/auth/login │                       │                       │
     │  { username, pwd }   │                       │                       │
     │──────────────────────►│                       │                       │
     │                       │  Forward same request│                       │
     │                       │──────────────────────►│                       │
     │                       │                       │  POST .../token       │
     │                       │                       │  grant_type=password  │
     │                       │                       │  client_id, secret   │
     │                       │                       │  username, password  │
     │                       │                       │──────────────────────►│
     │                       │                       │                       │
     │                       │                       │     Validate user     │
     │                       │                       │     & client;         │
     │                       │                       │     create session    │
     │                       │                       │                       │
     │                       │                       │  200 { access_token,  │
     │                       │                       │       refresh_token }│
     │                       │                       │◄──────────────────────│
     │                       │  200 { access_token,  │                       │
     │                       │       refresh_token }│                       │
     │                       │◄──────────────────────│                       │
     │  200 { access_token,  │                       │                       │
     │       refresh_token } │                       │                       │
     │◄──────────────────────│                       │                       │
     │                       │                       │                       │
     │  Store tokens;        │                       │                       │
     │  decode JWT for UI    │                       │                       │
     │                       │                       │                       │
```

---

## A.4 Validating the Token on Later Requests (No Login, No Keycloak Call)

- **Theoretical:** The access token is a signed statement from Keycloak (“this is user X with roles R”). The backend and gateway only need to **verify the signature** and **check expiry**, not ask Keycloak again.
- **Technical:**
  - Gateway and backend each use a **JWT decoder** (e.g. Nimbus) with Keycloak’s **JWK Set URI** (e.g. `.../realms/cmips/protocol/openid-connect/certs`).
  - Decoder **fetches the public keys once** (or when rotated) and **caches** them.
  - For each request:
    1. Read `Authorization: Bearer <access_token>`.
    2. Verify signature with cached public key.
    3. Check `exp` (and optionally `iss`, `aud`).
  - If valid → build `Authentication` (e.g. `sub` + `realm_access.roles` → `ROLE_*`). If invalid → 401.
  - **No** HTTP call to Keycloak for this validation; only occasional JWK refresh.

So: **authentication** on each request = **local JWT verification**; **login** is the only step that sends credentials to Keycloak.

---

# PART B — AUTHORIZATION

## B.1 What Authorization Achieves

- After we know **who** the user is (from the JWT), we decide **whether** they may perform an action (e.g. “view cases”, “approve timesheet”).
- Model: **resource** (e.g. “Case Resource”) + **scope** (e.g. “view”, “edit”). Permissions are “role R can do scope S on resource Res”.
- To avoid a network call on every check, we use a **cache**: “role → (resource → set of scopes)” built from Keycloak and **evaluate from cache** using the user’s roles from the JWT. Keycloak UMA is **not** called on the hot path.

---

## B.2 Protected API Request — Step-by-Step (Theory + Technique)

### Step 1: Frontend sends request with access token

- **Theoretical:** The token is the proof of identity; the backend will use it to both authenticate and authorize.
- **Technical:**
  - e.g. `GET {API_BASE}/api/cases` (or `/api/recipients`, etc.).
  - Header: `Authorization: Bearer <access_token>` (from localStorage).
  - Same-origin or CORS: request goes to the API Gateway (e.g. 8090).

---

### Step 2: Gateway receives request and applies security

- **Theoretical:** Gateway enforces “everything except /api/auth and a few public paths requires a valid token”.
- **Technical:**
  - Path is e.g. `/api/cases` → not `/api/auth/**` → rule is `anyExchange().authenticated()`.
  - **OAuth2 Resource Server** runs:
    - Extract Bearer token.
    - Decode JWT with **NimbusReactiveJwtDecoder** (issuer = Keycloak realm URL).
    - JWK Set is fetched from Keycloak and **cached**; validation is **local** (signature + exp).
  - If token missing/invalid/expired → **401**; request not forwarded.
  - If valid → build `Authentication`; then **JwtValidationFilter** runs.

---

### Step 3: Gateway adds user headers and forwards to backend

- **Theoretical:** Backend can use standard headers for user id and roles without parsing the JWT again (optional; backend still validates JWT).
- **Technical:**
  - From JWT: `sub` → `X-User-Id`, `preferred_username` → `X-User-Name`, `email` → `X-User-Email`, `realm_access.roles` → `X-User-Roles`.
  - Request to backend: same method and path, **same** `Authorization: Bearer <access_token>`, plus these headers.
  - Route: `/api/**` → `http://cmips-backend:8081`.

---

### Step 4: Backend validates JWT again and builds security context

- **Theoretical:** Backend does not trust the gateway for identity; it validates the token itself.
- **Technical:**
  - Spring Security OAuth2 Resource Server:
    - Reads `Authorization: Bearer <access_token>`.
    - Uses **NimbusJwtDecoder** with Keycloak JWK Set URI (cached); validates signature and `exp`.
  - Builds `Authentication` (principal = Jwt, authorities = roles from `realm_access.roles` with `ROLE_` prefix).
  - No call to Keycloak; all from JWT + cached JWK.

---

### Step 5: Dispatcher invokes controller; aspect runs before method

- **Theoretical:** Permission is enforced at the method boundary; one place to check “resource + scope”.
- **Technical:**
  - Request matches e.g. `GET /api/cases` → `CaseManagementController` method.
  - Method is annotated e.g. `@RequirePermission(resource = "Case Resource", scope = "view")`.
  - **AuthorizationAspect** runs **before** the method:
    - Reads `resource` and `scope` from the annotation.
    - Calls `KeycloakPolicyEvaluationService.evaluatePermission("Case Resource", "view")`.

---

### Step 6: Permission evaluation (cache-first, no Keycloak)

- **Theoretical:** “Does this user have this permission?” is answered from (a) who they are (roles in JWT) and (b) what those roles are allowed (cached map from Keycloak).
- **Technical:**
  - **User roles:** From JWT already in memory: `realm_access.roles` (e.g. `["CASEMANAGEMENTROLE","BASESECURITYGROUP"]`). No call to Keycloak.
  - **Permission cache:** In-memory map: `roleName → (resourceName → Set<scopeName>)`. Example: `CASEMANAGEMENTROLE` → `"Case Resource"` → `{view, create, edit, ...}`.
  - **Logic:** For the requested (resource, scope), check if **any** of the user’s roles has that (resource, scope) in the cache.
  - Cache is filled from Keycloak Admin API (resources, scopes, role policies, permissions) on a **schedule** (e.g. every 5 minutes) and at startup. **No** UMA or token call to Keycloak during this evaluation.
  - **Result:** `true` → allow; `false` → deny.

---

### Step 7: Allow or deny and return response

- **Theoretical:** One consistent place (aspect) returns 403 with a clear message.
- **Technical:**
  - If `evaluatePermission` returns **true** → aspect calls `proceed()` → controller runs (e.g. load cases from DB, return JSON).
  - If **false** → aspect returns **403** with body e.g. `{ "error": "Access denied", "message": "..." }` (no controller run).
  - Backend sends this response back to the gateway, which forwards it to the browser.

---

## B.3 Authorization Flow — Single Diagram

```
  BROWSER              API GATEWAY              BACKEND                      KEYCLOAK
     │                       │                       │                           │
     │  GET /api/cases       │                       │                           │
     │  Authorization:       │                       │                           │
     │  Bearer <JWT>         │                       │                           │
     │──────────────────────►│                       │                           │
     │                       │ 1. Route /api/**      │                           │
     │                       │ 2. JWT valid?         │                           │
     │                       │    (local JWK cache)  │                           │
     │                       │ 3. Add X-User-Id,     │                           │
     │                       │    X-User-Roles       │                           │
     │                       │ 4. Forward + Auth header                          │
     │                       │──────────────────────►│                           │
     │                       │                       │ 5. JWT valid? (local JWK) │
     │                       │                       │ 6. Build Authentication    │
     │                       │                       │    (roles from JWT)       │
     │                       │                       │ 7. @RequirePermission      │
     │                       │                       │    → evaluatePermission   │
     │                       │                       │    (resource, scope)      │
     │                       │                       │ 8. Roles from JWT +        │
     │                       │                       │    in-memory cache        │
     │                       │                       │    (no Keycloak call)     │
     │                       │                       │ 9. Allow → controller     │
     │                       │                       │    Deny → 403             │
     │                       │                       │                           │
     │                       │                       │ 10. Controller: DB, etc. │
     │                       │  200 OK + JSON         │                           │
     │                       │  (or 403 Forbidden)   │                           │
     │                       │◄──────────────────────│                           │
     │  200 OK + JSON        │                       │                           │
     │  (or 403)             │                       │                           │
     │◄──────────────────────│                       │                           │
     │                       │                       │                           │
```

---

## B.4 Where the Permission Cache Comes From (Not on Every Request)

- **Theoretical:** Keycloak remains the source of truth for “which role has which permission”; we only cache a snapshot to avoid calling it on every request.
- **Technical:**
  - Backend (or a scheduled job) calls Keycloak **Admin API** (with client/service credentials): list resources, scopes, role policies, permissions.
  - Builds map: for each permission, which roles (via “Policy-ROLENAME”) get which (resource, scope) → store in `rolePermissionCache`.
  - Runs at startup and on a timer (e.g. every 5 minutes). So **permission changes in Keycloak** take effect within that TTL; **no** Keycloak call during `evaluatePermission`.

---

# PART C — COMBINED FLOW SUMMARY

## C.1 Authentication vs Authorization

| Concept | When it happens | Where | Keycloak called? |
|---------|-----------------|-------|------------------|
| **Login** | User submits username/password | Backend → Keycloak token endpoint | Yes (once per login) |
| **Token validation** | Every request with Bearer token | Gateway + Backend (local JWT verify) | No (JWK cached) |
| **Permission check** | Every request to a @RequirePermission endpoint | Backend (cache + JWT roles) | No (cache refreshed periodically) |
| **Refresh token** | When access token expires | Backend → Keycloak token endpoint | Yes |
| **Logout** | User logs out | Backend → Keycloak end-session | Yes |

---

## C.2 End-to-End Flow (One Diagram)

```
                    LOGIN (once)
  ┌────────┐    POST /api/auth/login     ┌─────────┐    POST /token     ┌──────────┐
  │Browser │ ──── { user, pwd } ───────►│ Gateway │ ──────────────────►│ Backend  │ ───►│ Keycloak │
  └────────┘                             └─────────┘                    └─────────┘     └──────────┘
       ▲                                       │                             │                │
       │ 200 + { access_token, refresh_token }  │                             │  200 + tokens  │
       │◄──────────────────────────────────────┼◄────────────────────────────┼◄───────────────┘
       │  Store tokens                         │                             │

                    PROTECTED REQUEST (every API call)
  ┌────────┐    GET /api/cases              ┌─────────┐    Forward +       ┌─────────┐
  │Browser │    Authorization: Bearer JWT   │ Gateway │    X-User-*         │ Backend │
  └────────┘ ─────────────────────────────►│         │ ──────────────────►│         │
       ▲                                    │ Validate│                    │ Validate│
       │                                    │ JWT     │                    │ JWT     │
       │                                    │ (local) │                    │ (local) │
       │                                    └─────────┘                    │ Check   │
       │                                                                   │ permission
       │                                                                   │ (cache +
       │  200 + body (or 403)                                              │ JWT roles)
       │◄─────────────────────────────────────────────────────────────────┘
       │                                    No Keycloak call in this path
```

---

## C.3 Order of Operations (Checklist)

**Login**

1. User enters credentials in React app.
2. Browser POSTs to Gateway `/api/auth/login`.
3. Gateway allows without JWT; forwards to Backend.
4. Backend POSTs to Keycloak token endpoint with grant_type=password.
5. Keycloak validates user and client; returns tokens.
6. Backend returns tokens to Gateway; Gateway to browser.
7. Frontend stores tokens; may decode JWT for UI.

**Protected request**

1. Browser sends request with `Authorization: Bearer <access_token>`.
2. Gateway validates JWT locally (cached JWK); rejects if invalid.
3. Gateway adds X-User-* from JWT and forwards to Backend.
4. Backend validates JWT locally; builds Authentication (roles from JWT).
5. Aspect runs for @RequirePermission(resource, scope).
6. Service evaluates permission from JWT roles + in-memory cache (no Keycloak).
7. If allowed → controller runs; if denied → 403.
8. Response returned back through Gateway to browser.
