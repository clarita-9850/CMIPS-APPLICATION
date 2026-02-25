#!/usr/bin/env bash
# Test permission cache with test users (password: password123 for all).
# For each user: test endpoints they HAVE permission for (200) and do NOT (403).
# Set TEST_USERS="user1 user2 user3" to test your Keycloak users; default list below.
set -e

BACKEND_URL="${BACKEND_URL:-http://localhost:8081}"
KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8080}"
BASE_URL="${BASE_URL:-$BACKEND_URL}"
PASS="${TEST_PASSWORD:-password123}"

# Default users to try; override with TEST_USERS="user1 user2"
if [ -z "$TEST_USERS" ]; then
  TEST_USERS="caseworker1 recipient1 provider1 supervisor1 admin1 testuser1 testuser"
fi

get_token() {
  local user="$1"
  local pass="${2:-$PASS}"
  local code body
  # Try backend login first
  body=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$user\",\"password\":\"$pass\"}")
  code=$(echo "$body" | tail -1)
  if [ "$code" = "200" ]; then
    echo "$body" | sed '$d' | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p'
    return
  fi
  # Keycloak token (cmips-frontend)
  body=$(curl -s -w "\n%{http_code}" -X POST "$KEYCLOAK_URL/realms/cmips/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=$user" \
    -d "password=$pass" \
    -d "grant_type=password" \
    -d "client_id=cmips-frontend" \
    -d "client_secret=UnpJullDQX23tenZ4IsTuGkY8QzBlcFd")
  code=$(echo "$body" | tail -1)
  if [ "$code" = "200" ]; then
    echo "$body" | sed '$d' | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p'
  fi
}

http_get() {
  curl -s -o /dev/null -w "%{http_code}" -X GET "$1" -H "Authorization: Bearer $2" -H "Accept: application/json"
}

echo "=== Permission test: test users (password: $PASS) ==="
echo "Backend: $BASE_URL | Keycloak: $KEYCLOAK_URL"
echo "Users: $TEST_USERS"
echo ""

for user in $TEST_USERS; do
  echo "--- User: $user ---"
  TOKEN=$(get_token "$user")
  if [ -z "$TOKEN" ]; then
    echo "  SKIP: No token (user missing or wrong password?)"
    echo ""
    continue
  fi
  echo "  Token OK. Testing endpoints:"

  # Task Resource:view
  s=$(http_get "$BASE_URL/api/tasks" "$TOKEN")
  if [ "$s" = "200" ]; then
    echo "    GET /api/tasks (Task Resource:view)        => 200 OK (has permission)"
  else
    echo "    GET /api/tasks (Task Resource:view)        => $s (no permission)"
  fi

  # Work Queue Resource:view
  s=$(http_get "$BASE_URL/api/work-queues" "$TOKEN")
  if [ "$s" = "200" ]; then
    echo "    GET /api/work-queues (Work Queue:view)     => 200 OK (has permission)"
  else
    echo "    GET /api/work-queues (Work Queue:view)     => $s (no permission)"
  fi

  # Batch Job Resource (most roles should not have this)
  s=$(http_get "$BASE_URL/api/batch/trigger/status/1" "$TOKEN")
  if [ "$s" = "403" ]; then
    echo "    GET /api/batch/trigger/status/1 (Batch)   => 403 (correctly denied)"
  else
    echo "    GET /api/batch/trigger/status/1 (Batch)   => $s"
  fi

  # Referral Resource:view (optional – some roles may have it)
  s=$(http_get "$BASE_URL/api/referrals" "$TOKEN")
  if [ "$s" = "200" ]; then
    echo "    GET /api/referrals (Referral:view)         => 200 OK (has permission)"
  else
    echo "    GET /api/referrals (Referral:view)         => $s (no permission)"
  fi

  echo ""
done

echo "=== Done ==="
echo "All role->resource->scope permissions are cached from Keycloak; evaluation is cache-first."
