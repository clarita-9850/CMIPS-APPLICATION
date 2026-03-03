#!/usr/bin/env python3
"""
CMIPS Keycloak Authorization Setup Script
==========================================

Sets up the complete Keycloak authorization configuration for the CMIPS application:
  - Creates the 'cmips' realm (imports from cmips-realm-export.json)
  - 52 resources, 49 scopes, 52 role policies, 1496 scope-based permissions
  - 52 business roles (51 DSD core + CASE_WORKER)
  - 3 application users (cmipsadmin, testuser1, system_scheduler)
  - Role assignments for each user

This script is run by the keycloak-setup Docker service on first startup.
It is idempotent — safe to re-run on an existing setup.

Environment variables (set by docker-compose):
  KC_URL        - Keycloak base URL (default: http://keycloak:8080)
  KC_REALM      - Target realm name (default: cmips)
  KC_CLIENT_ID  - Backend client ID (default: cmips-backend)
  KC_ADMIN_USER - Master realm admin username (default: admin)
  KC_ADMIN_PASS - Master realm admin password (default: admin123)

Usage:
  python setup_keycloak_authorization.py [--wait]

  --wait  Wait up to 120s for Keycloak to become ready before starting setup.
"""

import argparse
import json
import os
import sys
import time
import urllib.request
import urllib.error
import urllib.parse

# ---------------------------------------------------------------------------
# Configuration from environment
# ---------------------------------------------------------------------------
KC_URL = os.environ.get("KC_URL", "http://keycloak:8080")
KC_REALM = os.environ.get("KC_REALM", "cmips")
KC_CLIENT_ID = os.environ.get("KC_CLIENT_ID", "cmips-backend")
KC_ADMIN_USER = os.environ.get("KC_ADMIN_USER", "admin")
KC_ADMIN_PASS = os.environ.get("KC_ADMIN_PASS", "admin123")

# Realm export JSON path (mounted by docker-compose)
REALM_JSON_PATH = os.environ.get(
    "REALM_JSON_PATH",
    os.path.join(os.path.dirname(os.path.abspath(__file__)), "cmips-realm-export.json"),
)

# ---------------------------------------------------------------------------
# 51 DSD Core Business Roles + CASE_WORKER
# ---------------------------------------------------------------------------
CORE_BUSINESS_ROLES = [
    # Case Management (5)
    "CASEMANAGEMENTROLE",
    "CASEMANAGEMENTSUPERVISORROLE",
    "CASEMANAGEMENTWITHAPPROVALROLE",
    "CASEMGMTPROVMGMTROLE",
    "CASEMGMTWITHAPPROVALPROVMGMTROLE",
    # Case Management + Payroll (8)
    "CASEMGMTPAYROLLROLE",
    "CASEMGMTPAYROLLAPPROVERROLE",
    "CASEMGMTPROVMGMTPAYROLLROLE",
    "CASEMGMTPROVMGMTPAYROLLAPPROVERROLE",
    "CASEMGMTWITHAPPROVALPAYROLLROLE",
    "CASEMGMTWITHAPPROVALPAYROLLAPPROVERROLE",
    "CASEMGMTWITHAPPROVALPROVMGMTPAYROLLROLE",
    "CASEMGMTWITHAPPROVALPROVMGMTPAYROLLAPPROVERROLE",
    # Eligibility & Medi-Cal (6)
    "ELIGIBILITYROLE",
    "ELIGIBILITYSUPERVISORROLE",
    "HPMEDSELIGBA1ROLE",
    "HPMEDSELIGBA2ROLE",
    "HPMEDSELIGBA3ROLE",
    "HPMEDSELIGBA4ROLE",
    # Intake & Referral (4)
    "INTAKEROLE",
    "INTAKESUPERVISORROLE",
    "REFERRALINTAKEROLE",
    "CALLCENTERROLE",
    # Provider Management (3)
    "PROVIDERMANAGEMENTROLE",
    "PROVIDERMGMTPAYROLLAPPROVERROLE",
    "PROVIDERMGMTPAYROLLMGMTROLE",
    # Payroll & Financial Processing (6)
    "PAYROLLROLE",
    "PAYROLLMGMTROLE",
    "CROSSCOUNTYPAYROLLROLE",
    "WPCSROLE",
    "HPWARRANTREPLACEMENTROLE",
    "HPETRAVELCLAIMPAYMENTROLE",
    # Timesheet Management (4)
    "TIMESHEETROLE",
    "BVINOAANDTIMESHEETMANAGEMENTROLE",
    "ETIMESHEETHELPDESKROLE",
    "HDBVINOAANDTSMGMTROLE",
    # Public Authority (4)
    "PUBLICAUTHORITYROLE",
    "PABENEFITSROLE",
    "PAPROVIDERENROLLROLE",
    "PAPROVIDERENROLLBENEFITSROLE",
    # CDSS (4)
    "CDSSVIEWROLE",
    "CDSSFISCALROLE",
    "CDSSMODIFYROLE",
    "CDSSPROGRAMMGMTROLE",
    # Program Management (1)
    "PROGRAMMGMTROLE",
    # Homemaker (1)
    "HOMEMAKERROLE",
    # Help Desk (1)
    "HELPDESKROLE",
    # Investigation (1)
    "INVESTIGATORROLE",
    # View/Inquiry (2)
    "COUNTYVIEWONLYROLE",
    "HPINQUIRY",
    # TPF (1)
    "TPF",
    # Application-level role
    "CASE_WORKER",
]

# ---------------------------------------------------------------------------
# Users to create with their role assignments
# ---------------------------------------------------------------------------
USERS = [
    {
        "username": "cmipsadmin",
        "password": "password123",
        "firstName": "CMIPS",
        "lastName": "Admin",
        "email": "cmipsadmin@cmips.ca.gov",
        "enabled": True,
        "roles": CORE_BUSINESS_ROLES,  # all 52 roles — full admin access
    },
    {
        "username": "testuser1",
        "password": "testuser1",
        "firstName": "Test",
        "lastName": "User",
        "email": "testuser1@cmips.ca.gov",
        "enabled": True,
        "roles": ["CASEMANAGEMENTROLE"],  # basic case worker
    },
    {
        "username": "system_scheduler",
        "password": "system_scheduler_pass_123!",
        "firstName": "System",
        "lastName": "Scheduler",
        "email": "scheduler@cmips.ca.gov",
        "enabled": True,
        "roles": [],  # service account — no business roles needed
    },
]

# ---------------------------------------------------------------------------
# HTTP helpers (no external dependencies — uses only urllib)
# ---------------------------------------------------------------------------
_admin_token = None
_token_expiry = 0


def _request(url, method="GET", data=None, headers=None, expect_codes=(200,)):
    """Make an HTTP request and return (status_code, response_body)."""
    hdrs = headers or {}
    body = None
    if data is not None:
        if isinstance(data, dict):
            body = json.dumps(data).encode("utf-8")
            hdrs.setdefault("Content-Type", "application/json")
        elif isinstance(data, str):
            body = data.encode("utf-8")
            hdrs.setdefault("Content-Type", "application/x-www-form-urlencoded")
        elif isinstance(data, bytes):
            body = data
            hdrs.setdefault("Content-Type", "application/json")
    req = urllib.request.Request(url, data=body, headers=hdrs, method=method)
    try:
        with urllib.request.urlopen(req) as resp:
            resp_body = resp.read().decode("utf-8", errors="replace")
            return resp.status, resp_body
    except urllib.error.HTTPError as e:
        resp_body = e.read().decode("utf-8", errors="replace") if e.fp else ""
        return e.code, resp_body


def get_admin_token():
    """Obtain (and cache) a master-realm admin token."""
    global _admin_token, _token_expiry
    now = time.time()
    if _admin_token and now < _token_expiry:
        return _admin_token
    payload = urllib.parse.urlencode(
        {
            "client_id": "admin-cli",
            "username": KC_ADMIN_USER,
            "password": KC_ADMIN_PASS,
            "grant_type": "password",
        }
    )
    status, body = _request(
        f"{KC_URL}/realms/master/protocol/openid-connect/token",
        method="POST",
        data=payload,
    )
    if status != 200:
        raise RuntimeError(f"Failed to get admin token: {status} {body}")
    data = json.loads(body)
    _admin_token = data["access_token"]
    _token_expiry = now + data.get("expires_in", 300) - 30
    return _admin_token


def kc_get(path):
    """GET request to Keycloak Admin API."""
    token = get_admin_token()
    return _request(
        f"{KC_URL}/admin{path}",
        headers={"Authorization": f"Bearer {token}"},
    )


def kc_post(path, data):
    """POST request to Keycloak Admin API."""
    token = get_admin_token()
    return _request(
        f"{KC_URL}/admin{path}",
        method="POST",
        data=data,
        headers={"Authorization": f"Bearer {token}"},
    )


def kc_put(path, data):
    """PUT request to Keycloak Admin API."""
    token = get_admin_token()
    return _request(
        f"{KC_URL}/admin{path}",
        method="PUT",
        data=data,
        headers={"Authorization": f"Bearer {token}"},
    )


# ---------------------------------------------------------------------------
# Wait for Keycloak readiness
# ---------------------------------------------------------------------------
def wait_for_keycloak(timeout_seconds=120):
    """Poll Keycloak health endpoint until ready."""
    print(f"Waiting for Keycloak at {KC_URL} ...")
    deadline = time.time() + timeout_seconds
    while time.time() < deadline:
        try:
            status, body = _request(f"{KC_URL}/health/ready")
            if status == 200:
                print("  Keycloak is ready.")
                return
        except Exception:
            pass
        time.sleep(3)
    raise TimeoutError(f"Keycloak not ready after {timeout_seconds}s")


# ---------------------------------------------------------------------------
# Realm setup
# ---------------------------------------------------------------------------
def realm_exists():
    """Check if the target realm already exists."""
    status, _ = kc_get(f"/realms/{KC_REALM}")
    return status == 200


def import_realm():
    """Import the realm from the exported JSON file."""
    if not os.path.isfile(REALM_JSON_PATH):
        # If no JSON file, fall back to creating realm + roles via API
        print(f"  Realm JSON not found at {REALM_JSON_PATH}, creating realm via API...")
        create_realm_minimal()
        return

    print(f"  Importing realm from {REALM_JSON_PATH} ({os.path.getsize(REALM_JSON_PATH) / 1024:.0f} KB)...")
    with open(REALM_JSON_PATH, "rb") as f:
        realm_data = f.read()

    status, body = kc_post("/realms", realm_data)
    if status == 201:
        print("  Realm imported successfully.")
    elif status == 409:
        print("  Realm already exists (409 Conflict), skipping import.")
    else:
        raise RuntimeError(f"Realm import failed: {status} {body[:500]}")


def create_realm_minimal():
    """Create a minimal realm with just roles (fallback if no JSON export)."""
    realm_config = {
        "realm": KC_REALM,
        "enabled": True,
        "sslRequired": "none",
        "registrationAllowed": False,
        "loginWithEmailAllowed": True,
        "duplicateEmailsAllowed": False,
        "resetPasswordAllowed": False,
        "editUsernameAllowed": False,
        "bruteForceProtected": True,
    }
    status, body = kc_post("/realms", realm_config)
    if status == 201:
        print("  Realm created.")
    elif status == 409:
        print("  Realm already exists.")
    else:
        raise RuntimeError(f"Failed to create realm: {status} {body[:500]}")

    # Create roles
    print("  Creating business roles...")
    for role_name in CORE_BUSINESS_ROLES:
        status, body = kc_post(
            f"/realms/{KC_REALM}/roles",
            {"name": role_name, "description": f"DSD business role: {role_name}"},
        )
        if status == 201:
            print(f"    Created: {role_name}")
        elif status == 409:
            pass  # already exists
        else:
            print(f"    WARN: {role_name} -> {status}")


# ---------------------------------------------------------------------------
# Verify realm configuration
# ---------------------------------------------------------------------------
def verify_realm():
    """Verify realm has expected roles, resources, policies."""
    print("  Verifying realm configuration...")

    # Check roles
    status, body = kc_get(f"/realms/{KC_REALM}/roles")
    if status != 200:
        raise RuntimeError(f"Failed to get roles: {status}")
    roles = json.loads(body)
    role_names = {r["name"] for r in roles}

    missing_roles = []
    for expected_role in CORE_BUSINESS_ROLES:
        if expected_role not in role_names:
            missing_roles.append(expected_role)

    if missing_roles:
        print(f"    WARNING: {len(missing_roles)} missing roles: {missing_roles[:5]}...")
        # Create missing roles
        for role_name in missing_roles:
            status, _ = kc_post(
                f"/realms/{KC_REALM}/roles",
                {"name": role_name, "description": f"DSD business role: {role_name}"},
            )
            if status in (201, 409):
                print(f"    Created: {role_name}")
    else:
        business_count = len([r for r in role_names if r not in ("default-roles-cmips", "offline_access", "uma_authorization")])
        print(f"    Roles: {business_count} business roles present (expected 52)")

    # Check authorization resources on cmips-backend client
    status, body = kc_get(f"/realms/{KC_REALM}/clients")
    if status != 200:
        raise RuntimeError(f"Failed to get clients: {status}")
    clients = json.loads(body)
    backend_client = None
    for c in clients:
        if c.get("clientId") == KC_CLIENT_ID:
            backend_client = c
            break

    if not backend_client:
        print(f"    WARNING: Client '{KC_CLIENT_ID}' not found!")
        return

    client_uuid = backend_client["id"]

    # Check resources
    status, body = kc_get(f"/realms/{KC_REALM}/clients/{client_uuid}/authz/resource-server/resource")
    if status == 200:
        resources = json.loads(body)
        print(f"    Resources: {len(resources)} (expected 52)")
    else:
        print(f"    WARNING: Could not verify resources: {status}")

    # Check policies (role-based)
    status, body = kc_get(f"/realms/{KC_REALM}/clients/{client_uuid}/authz/resource-server/policy?type=role&max=100")
    if status == 200:
        policies = json.loads(body)
        print(f"    Role policies: {len(policies)} (expected 52)")
    else:
        print(f"    WARNING: Could not verify policies: {status}")

    # Check scope permissions
    status, body = kc_get(f"/realms/{KC_REALM}/clients/{client_uuid}/authz/resource-server/policy?type=scope&max=5000")
    if status == 200:
        permissions = json.loads(body)
        print(f"    Scope permissions: {len(permissions)} (expected 1496)")
    else:
        print(f"    WARNING: Could not verify permissions: {status}")


# ---------------------------------------------------------------------------
# User setup
# ---------------------------------------------------------------------------
def get_user_by_username(username):
    """Find a user by username. Returns user dict or None."""
    status, body = kc_get(
        f"/realms/{KC_REALM}/users?username={urllib.parse.quote(username)}&exact=true"
    )
    if status != 200:
        return None
    users = json.loads(body)
    for u in users:
        if u["username"] == username:
            return u
    return None


def create_or_update_user(user_spec):
    """Create a user if they don't exist, set password, assign roles."""
    username = user_spec["username"]
    print(f"  User: {username}")

    existing = get_user_by_username(username)
    if existing:
        user_id = existing["id"]
        print(f"    Already exists (id={user_id[:8]}...)")
    else:
        # Create user
        user_data = {
            "username": username,
            "firstName": user_spec.get("firstName", ""),
            "lastName": user_spec.get("lastName", ""),
            "email": user_spec.get("email", ""),
            "enabled": user_spec.get("enabled", True),
            "emailVerified": True,
        }
        status, body = kc_post(f"/realms/{KC_REALM}/users", user_data)
        if status not in (201,):
            raise RuntimeError(f"Failed to create user {username}: {status} {body[:300]}")

        # Get the created user's ID
        existing = get_user_by_username(username)
        if not existing:
            raise RuntimeError(f"User {username} created but not found")
        user_id = existing["id"]
        print(f"    Created (id={user_id[:8]}...)")

    # Set password (non-temporary)
    pw_data = {
        "type": "password",
        "value": user_spec["password"],
        "temporary": False,
    }
    status, _ = kc_put(
        f"/realms/{KC_REALM}/users/{user_id}/reset-password", pw_data
    )
    if status == 204:
        print(f"    Password set")
    else:
        print(f"    WARN: Password set returned {status}")

    # Remove any requiredActions (so user can login immediately)
    status, body = kc_get(f"/realms/{KC_REALM}/users/{user_id}")
    if status == 200:
        user_obj = json.loads(body)
        if user_obj.get("requiredActions"):
            user_obj["requiredActions"] = []
            kc_put(f"/realms/{KC_REALM}/users/{user_id}", user_obj)
            print(f"    Cleared requiredActions")

    # Assign roles
    if user_spec.get("roles"):
        assign_roles(user_id, username, user_spec["roles"])


def assign_roles(user_id, username, role_names):
    """Assign realm roles to a user."""
    # Get all realm roles to map name -> id
    status, body = kc_get(f"/realms/{KC_REALM}/roles")
    if status != 200:
        print(f"    WARN: Could not get roles for assignment")
        return
    all_roles = json.loads(body)
    role_map = {r["name"]: r for r in all_roles}

    # Get currently assigned roles
    status, body = kc_get(f"/realms/{KC_REALM}/users/{user_id}/role-mappings/realm")
    current_roles = set()
    if status == 200:
        current_roles = {r["name"] for r in json.loads(body)}

    # Determine which roles to add
    roles_to_add = []
    for rn in role_names:
        if rn not in current_roles and rn in role_map:
            roles_to_add.append(
                {"id": role_map[rn]["id"], "name": rn}
            )

    if roles_to_add:
        status, body = kc_post(
            f"/realms/{KC_REALM}/users/{user_id}/role-mappings/realm",
            roles_to_add,
        )
        if status == 204:
            print(f"    Assigned {len(roles_to_add)} roles")
        else:
            print(f"    WARN: Role assignment returned {status}: {body[:200]}")
    else:
        assigned = len([r for r in role_names if r in current_roles])
        print(f"    All {assigned} roles already assigned")


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
def main():
    parser = argparse.ArgumentParser(description="CMIPS Keycloak Authorization Setup")
    parser.add_argument("--wait", action="store_true", help="Wait for Keycloak to be ready")
    args = parser.parse_args()

    print("=" * 60)
    print("CMIPS Keycloak Authorization Setup")
    print("=" * 60)
    print(f"  KC_URL:       {KC_URL}")
    print(f"  KC_REALM:     {KC_REALM}")
    print(f"  KC_CLIENT_ID: {KC_CLIENT_ID}")
    print(f"  REALM_JSON:   {REALM_JSON_PATH}")
    print()

    # Step 1: Wait for Keycloak
    if args.wait:
        wait_for_keycloak(timeout_seconds=120)
    else:
        # Quick check
        try:
            status, _ = _request(f"{KC_URL}/health/ready")
            if status != 200:
                raise ConnectionError()
        except Exception:
            print("ERROR: Keycloak is not ready. Use --wait to wait for it.")
            sys.exit(1)

    # Step 2: Get admin token
    print("\n[1/4] Authenticating to Keycloak master realm...")
    get_admin_token()
    print("  Authenticated successfully.")

    # Step 3: Import/create realm
    print(f"\n[2/4] Setting up realm '{KC_REALM}'...")
    if realm_exists():
        print(f"  Realm '{KC_REALM}' already exists — skipping import.")
    else:
        import_realm()

    # Step 4: Verify configuration
    print(f"\n[3/4] Verifying authorization configuration...")
    verify_realm()

    # Step 5: Create users
    print(f"\n[4/4] Setting up users...")
    for user_spec in USERS:
        create_or_update_user(user_spec)

    print("\n" + "=" * 60)
    print("Setup complete!")
    print("=" * 60)
    print(f"\nLogin credentials:")
    for u in USERS:
        print(f"  {u['username']:20s} / {u['password']}")
    print(f"\nKeycloak Admin Console: {KC_URL}")
    print(f"  admin / {KC_ADMIN_PASS}")
    print()


if __name__ == "__main__":
    main()
