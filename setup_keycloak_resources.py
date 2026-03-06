#!/usr/bin/env python3
import urllib.request, urllib.error, json

KC_URL = "http://keycloak:8080"
KC_REALM = "cmips"

def api_call(method, url, data=None, token=None):
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    if data and isinstance(data, dict):
        data = json.dumps(data).encode()
    elif data and isinstance(data, str):
        headers["Content-Type"] = "application/x-www-form-urlencoded"
        data = data.encode()
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(req)
        body = resp.read().decode()
        return resp.status, json.loads(body) if body else {}
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()

# 1. Get admin token
_, token_resp = api_call("POST", f"{KC_URL}/realms/master/protocol/openid-connect/token",
    "username=admin&password=admin123&grant_type=password&client_id=admin-cli")
TOKEN = token_resp["access_token"]
print("Got admin token")

# 2. Get client UUID
_, clients = api_call("GET", f"{KC_URL}/admin/realms/{KC_REALM}/clients", token=TOKEN)
CLIENT_UUID = next(c["id"] for c in clients if c["clientId"] == "cmips-backend")
print(f"Client UUID: {CLIENT_UUID}")

AUTH_URL = f"{KC_URL}/admin/realms/{KC_REALM}/clients/{CLIENT_UUID}/authz/resource-server"

# 3. Get CM_WORKER role ID
_, roles = api_call("GET", f"{KC_URL}/admin/realms/{KC_REALM}/roles", token=TOKEN)
CW_ROLE_ID = next(r["id"] for r in roles if r["name"] == "CM_WORKER")
print(f"CM_WORKER role ID: {CW_ROLE_ID}")

# 4. All resources and scopes needed by controllers
RESOURCES = {
    "Provider Resource": ["view", "create", "edit", "approve", "reinstate", "enroll"],
    "Provider Assignment Resource": ["view", "create", "edit", "terminate"],
    "Provider CORI Resource": ["view", "create", "edit"],
    "Overtime Violation Resource": ["view", "create", "edit", "review", "supervisor-review"],
    "Task Resource": ["view", "create", "close", "defer", "forward", "reallocate", "reserve"],
    "Case Resource": ["view", "create", "edit", "approve", "deny", "assign", "transfer", "terminate", "reactivate", "rescind"],
    "Case Notes Resource": ["view", "create", "edit", "delete"],
    "Case Contacts Resource": ["view", "create", "delete"],
    "Case Management Resource": ["approve", "supervise"],
    "Referral Resource": ["view", "create", "edit", "assign", "close", "convert", "reopen"],
    "Recipient Resource": ["view", "create", "edit"],
    "Recipient Waiver Resource": ["view", "create", "edit", "approve", "assign", "submit", "revoke"],
    "Service Eligibility Resource": ["view", "create", "edit", "approve"],
    "Health Care Certification Resource": ["view", "create", "edit", "approve"],
    "Application Resource": ["view", "create", "edit", "approve", "deny"],
    "Batch Job Resource": ["view", "trigger", "stop", "status"],
    "Work Queue Resource": ["view", "manage", "reserve", "subscribe"],
    "Quality Assurance Resource": ["view"],
    "Warrant Resource": ["view", "create"],
    "Normal Login Resource": ["view", "create", "delete"],
}

# 5. Create resources
print("\n=== Creating Resources ===")
resource_ids = {}
for rname, scopes in RESOURCES.items():
    scope_list = [{"name": s} for s in scopes]
    status, resp = api_call("POST", f"{AUTH_URL}/resource",
        {"name": rname, "type": rname, "scopes": scope_list}, token=TOKEN)
    if status == 201:
        rid = resp["_id"]
        resource_ids[rname] = rid
        print(f"  Created: {rname} ({rid})")
    elif status == 409:
        print(f"  Exists: {rname}")
        _, existing = api_call("GET", f"{AUTH_URL}/resource?name={urllib.request.quote(rname)}", token=TOKEN)
        if existing:
            resource_ids[rname] = existing[0]["_id"]
    else:
        print(f"  ERROR {status}: {rname} - {resp}")

# 6. Create CaseWorker Full Access policy
print("\n=== Creating Policy ===")
status, resp = api_call("POST", f"{AUTH_URL}/policy/role",
    {"name": "CM Worker Full Access", "type": "role", "logic": "POSITIVE",
     "roles": [{"id": CW_ROLE_ID, "required": False}]}, token=TOKEN)
if status == 201:
    policy_id = resp["id"]
    print(f"  Created policy: {policy_id}")
else:
    print(f"  Policy create returned {status}, fetching existing...")
    _, policies = api_call("GET", f"{AUTH_URL}/policy", token=TOKEN)
    policy_id = None
    for p in policies:
        if "CM Worker" in p.get("name", "") or "CaseWorker" in p.get("name", "") or "Case Worker" in p.get("name", ""):
            policy_id = p["id"]
            print(f"  Using existing policy: {p['name']} ({policy_id})")
            break
    if not policy_id:
        print("  ERROR: Could not find or create policy")

# 7. Create permissions
if policy_id:
    print("\n=== Creating Permissions ===")
    for rname, rid in resource_ids.items():
        _, resource_detail = api_call("GET", f"{AUTH_URL}/resource/{rid}", token=TOKEN)
        scope_ids = [s["id"] for s in resource_detail.get("scopes", [])]

        perm_data = {
            "name": f"{rname} - Full Access",
            "type": "resource",
            "logic": "POSITIVE",
            "decisionStrategy": "AFFIRMATIVE",
            "resources": [rid],
            "policies": [policy_id],
            "scopes": scope_ids
        }
        status, resp = api_call("POST", f"{AUTH_URL}/permission/resource", perm_data, token=TOKEN)
        if status == 201:
            print(f"  Created: {rname} - Full Access")
        elif status == 409:
            print(f"  Exists: {rname} - Full Access")
        else:
            print(f"  ERROR {status}: {rname} - {resp}")

print("\n=== DONE ===")
