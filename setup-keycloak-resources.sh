#!/bin/bash
# Setup all Keycloak resources, scopes, and permissions for CMIPS

KC_URL="http://localhost:8080"
KC_REALM="cmips"
KC_CLIENT_ID="cmips-backend"
KC_ADMIN_USER="admin"
KC_ADMIN_PASS="admin123"

# Get admin token
TOKEN=$(curl -s -X POST "$KC_URL/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=$KC_ADMIN_USER" \
  -d "password=$KC_ADMIN_PASS" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])")

if [ -z "$TOKEN" ]; then
  echo "ERROR: Failed to get admin token"
  exit 1
fi
echo "Got admin token"

# Get client internal ID
CLIENT_UUID=$(curl -s "$KC_URL/admin/realms/$KC_REALM/clients" \
  -H "Authorization: Bearer $TOKEN" | python3 -c "
import sys,json
clients = json.load(sys.stdin)
for c in clients:
    if c['clientId'] == '$KC_CLIENT_ID':
        print(c['id'])
        break
")

if [ -z "$CLIENT_UUID" ]; then
  echo "ERROR: Client $KC_CLIENT_ID not found"
  exit 1
fi
echo "Client UUID: $CLIENT_UUID"

AUTH_URL="$KC_URL/admin/realms/$KC_REALM/clients/$CLIENT_UUID/authz/resource-server"

# Function to create a scope
create_scope() {
  local name="$1"
  curl -s -o /dev/null -w "%{http_code}" -X POST "$AUTH_URL/scope" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"name\": \"$name\"}"
}

# Function to create a resource with scopes
create_resource() {
  local name="$1"
  shift
  local scopes_json="["
  local first=true
  for scope in "$@"; do
    if [ "$first" = true ]; then first=false; else scopes_json+=","; fi
    scopes_json+="{\"name\":\"$scope\"}"
  done
  scopes_json+="]"
  
  local status=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$AUTH_URL/resource" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"name\": \"$name\", \"type\": \"$name\", \"scopes\": $scopes_json}")
  echo "  Resource '$name': $status"
}

echo ""
echo "=== Creating Resources ==="

create_resource "Provider Resource" view create edit approve reinstate enroll
create_resource "Provider Assignment Resource" view create edit terminate
create_resource "Provider CORI Resource" view create edit
create_resource "Overtime Violation Resource" view create edit review supervisor-review
create_resource "Task Resource" view create close defer forward reallocate reserve
create_resource "Case Resource" view create edit approve deny assign transfer terminate reactivate rescind
create_resource "Case Notes Resource" view create edit delete
create_resource "Case Contacts Resource" view create delete
create_resource "Case Management Resource" approve supervise
create_resource "Referral Resource" view create edit assign close convert reopen
create_resource "Recipient Resource" view create edit
create_resource "Recipient Waiver Resource" view create edit approve assign submit revoke
create_resource "Service Eligibility Resource" view create edit approve
create_resource "Health Care Certification Resource" view create edit approve
create_resource "Application Resource" view create edit approve deny
create_resource "Batch Job Resource" view trigger stop status
create_resource "Work Queue Resource" view manage reserve subscribe
create_resource "Quality Assurance Resource" view
create_resource "Warrant Resource" view create
create_resource "Normal Login Resource" view create delete

echo ""
echo "=== Creating Case Worker Policy ==="

# Check if Case Worker Policy already exists
EXISTING_POLICY=$(curl -s "$AUTH_URL/policy?name=Case+Worker+Policy" \
  -H "Authorization: Bearer $TOKEN" | python3 -c "
import sys,json
policies = json.load(sys.stdin)
for p in policies:
    if p['name'] == 'Case Worker Policy':
        print(p['id'])
        break
" 2>/dev/null)

if [ -z "$EXISTING_POLICY" ]; then
  # Get CASE_WORKER role ID
  ROLE_ID=$(curl -s "$KC_URL/admin/realms/$KC_REALM/roles" \
    -H "Authorization: Bearer $TOKEN" | python3 -c "
import sys,json
roles = json.load(sys.stdin)
for r in roles:
    if r['name'] == 'CASE_WORKER':
        print(r['id'])
        break
")
  
  curl -s -o /dev/null -w "  Policy: %{http_code}\n" -X POST "$AUTH_URL/policy/role" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"name\": \"CaseWorker Full Access Policy\", \"type\": \"role\", \"logic\": \"POSITIVE\", \"roles\": [{\"id\": \"$ROLE_ID\", \"required\": false}]}"
  POLICY_NAME="CaseWorker Full Access Policy"
else
  echo "  Policy already exists: $EXISTING_POLICY"
  POLICY_NAME="Case Worker Policy"
fi

echo ""
echo "=== Creating Permissions ==="

# Get all resources
RESOURCES=$(curl -s "$AUTH_URL/resource" \
  -H "Authorization: Bearer $TOKEN")

# Create a permission for each resource granting CASE_WORKER access to all scopes
python3 - "$AUTH_URL" "$TOKEN" "$POLICY_NAME" <<'PYEOF'
import sys, json, urllib.request, urllib.error

auth_url = sys.argv[1]
token = sys.argv[2]
policy_name = sys.argv[3]

headers = {
    "Authorization": f"Bearer {token}",
    "Content-Type": "application/json"
}

# Get all resources
req = urllib.request.Request(f"{auth_url}/resource", headers=headers)
resources = json.loads(urllib.request.urlopen(req).read())

# Get policy ID
req = urllib.request.Request(f"{auth_url}/policy", headers=headers)
policies = json.loads(urllib.request.urlopen(req).read())
policy_id = None
for p in policies:
    if p["name"] == policy_name:
        policy_id = p["id"]
        break

if not policy_id:
    # Try the other name
    for p in policies:
        if "Case Worker" in p["name"] or "CaseWorker" in p["name"]:
            policy_id = p["id"]
            policy_name = p["name"]
            break

if not policy_id:
    print(f"ERROR: Could not find policy '{policy_name}'")
    sys.exit(1)

print(f"  Using policy: {policy_name} ({policy_id})")

# Skip already-existing resources (Timesheet, EVV, Provider-Recipient already have permissions)
skip_resources = {"Timesheet Resource", "EVV Resource", "Provider-Recipient Resource"}

for resource in resources:
    rname = resource["name"]
    rid = resource["_id"]
    
    if rname in skip_resources:
        print(f"  Skipping {rname} (already configured)")
        continue
    
    # Get scopes for this resource
    req = urllib.request.Request(f"{auth_url}/resource/{rid}", headers=headers)
    resource_detail = json.loads(urllib.request.urlopen(req).read())
    scopes = resource_detail.get("scopes", [])
    scope_ids = [s["id"] for s in scopes]
    
    if not scope_ids:
        print(f"  Skipping {rname} (no scopes)")
        continue
    
    perm_data = {
        "name": f"{rname} - CaseWorker Full Access",
        "type": "resource",
        "logic": "POSITIVE",
        "decisionStrategy": "AFFIRMATIVE",
        "resources": [rid],
        "policies": [policy_id],
        "scopes": scope_ids
    }
    
    data = json.dumps(perm_data).encode()
    req = urllib.request.Request(f"{auth_url}/permission/resource", data=data, headers=headers, method="POST")
    try:
        resp = urllib.request.urlopen(req)
        print(f"  Permission created: {rname} - CaseWorker Full Access ({resp.status})")
    except urllib.error.HTTPError as e:
        body = e.read().decode()
        if "already exists" in body.lower():
            print(f"  Permission already exists: {rname}")
        else:
            print(f"  ERROR creating permission for {rname}: {e.code} {body}")

print("")
print("=== Done! ==="  )
PYEOF

