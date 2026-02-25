#!/usr/bin/env bash
# Test cache-first permission: login, call protected API, verify "via cache" in logs.
set -e

BACKEND_URL="${BACKEND_URL:-http://localhost:8081}"
GATEWAY_URL="${GATEWAY_URL:-http://localhost:8090}"
# Use backend directly so we only need backend + Keycloak (no gateway required)
BASE_URL="${BASE_URL:-$BACKEND_URL}"

echo "=== 1. Get access token ==="
# Try backend login first (uses cmips-backend client)
LOGIN_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"caseworker1","password":"password123"}')
HTTP_BODY=$(echo "$LOGIN_RESP" | sed '$d')
HTTP_CODE=$(echo "$LOGIN_RESP" | tail -1)

if [ "$HTTP_CODE" = "200" ]; then
  ACCESS_TOKEN=$(echo "$HTTP_BODY" | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p')
fi

# Fallback: get token directly from Keycloak (in case realm has only cmips-frontend)
KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8080}"
if [ -z "$ACCESS_TOKEN" ] || [ "$HTTP_CODE" != "200" ]; then
  echo "Backend login returned HTTP $HTTP_CODE. Trying Keycloak token endpoint (KEYCLOAK_URL=$KEYCLOAK_URL)..."
  TOKEN_RESP=$(curl -s -w "\n%{http_code}" -X POST "$KEYCLOAK_URL/realms/cmips/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=caseworker1" \
    -d "password=password123" \
    -d "grant_type=password" \
    -d "client_id=cmips-frontend" \
    -d "client_secret=UnpJullDQX23tenZ4IsTuGkY8QzBlcFd")
  TOKEN_BODY=$(echo "$TOKEN_RESP" | sed '$d')
  TOKEN_CODE=$(echo "$TOKEN_RESP" | tail -1)
  if [ "$TOKEN_CODE" = "200" ]; then
    ACCESS_TOKEN=$(echo "$TOKEN_BODY" | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p')
    echo "Keycloak token OK."
  fi
fi

if [ -z "$ACCESS_TOKEN" ]; then
  echo "Could not obtain token. Ensure Keycloak is up and realm 'cmips' has user caseworker1/password123 and client cmips-frontend (or cmips-backend) with direct access grants."
  echo "Backend response: $HTTP_BODY"
  exit 1
fi
echo "Got access_token (length ${#ACCESS_TOKEN})."

echo ""
echo "=== 2. Call protected API (GET $BASE_URL/api/tasks) with Bearer token ==="
TASKS_RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/api/tasks" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Accept: application/json")
TASKS_BODY=$(echo "$TASKS_RESP" | sed '$d')
TASKS_CODE=$(echo "$TASKS_RESP" | tail -1)

echo "HTTP $TASKS_CODE"
if [ "$TASKS_CODE" = "200" ]; then
  echo "API returned 200 OK. Body (first 200 chars): ${TASKS_BODY:0:200}..."
elif [ "$TASKS_CODE" = "403" ]; then
  echo "API returned 403 Forbidden (permission denied). Body: $TASKS_BODY"
else
  echo "API response: $TASKS_BODY"
fi

echo ""
echo "=== 3. Check backend logs for cache-first path ==="
if command -v docker &>/dev/null && docker ps --format '{{.Names}}' 2>/dev/null | grep -q cmips-backend; then
  LOGS=$(docker logs cmips-backend 2>&1 | tail -150)
  if echo "$LOGS" | grep -q "via cache"; then
    echo "PASS: Backend logs contain 'via cache' -> permission was evaluated from cache (cache-first path)."
    echo "$LOGS" | grep -E "Evaluating permission|via cache|Permission GRANTED|Permission DENIED" | tail -10
  else
    echo "Recent permission-related log lines:"
    echo "$LOGS" | grep -E "Evaluating permission|Permission cache|via cache|UMA fallback|Permission DENIED|Permission GRANTED" | tail -15
    echo ""
    echo "Note: Cache-first is used: the backend tries the cache first. If you see 'Permission cache is empty' then UMA fallback ran. To get 200 OK and 'via cache', ensure Keycloak has Task Resource (and Work Queue Resource) with permissions for CASE_WORKER (e.g. set cmips.keycloak.init-resources=true and configure policies)."
  fi
else
  echo "Docker not available or cmips-backend not running. Manually check backend logs for 'Permission ... (via cache)'."
fi

echo ""
echo "=== Done ==="
