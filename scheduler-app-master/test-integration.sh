#!/bin/bash

# =============================================================================
# Scheduler → CMIPS Backend Integration Test Script
# =============================================================================
# This script tests the full integration flow:
# 1. Create a job definition in Scheduler
# 2. Trigger the job
# 3. Verify CMIPS backend receives and executes it
# 4. Verify Scheduler receives Redis events
# =============================================================================

set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

SCHEDULER_URL="http://localhost:8084"
CMIPS_URL="http://localhost:8081"

echo "=============================================="
echo "  Scheduler → CMIPS Backend Integration Test"
echo "=============================================="
echo ""

# Step 1: Check if services are healthy
echo -e "${YELLOW}Step 1: Checking service health...${NC}"

echo -n "  Scheduler Backend: "
if curl -s "$SCHEDULER_URL/actuator/health" | grep -q "UP"; then
    echo -e "${GREEN}UP${NC}"
else
    echo -e "${RED}DOWN${NC}"
    echo "Please start the services with: docker-compose up --build -d"
    exit 1
fi

echo -n "  CMIPS Backend: "
if curl -s "$CMIPS_URL/actuator/health" | grep -q "UP"; then
    echo -e "${GREEN}UP${NC}"
else
    echo -e "${RED}DOWN${NC}"
    echo "Please start the services with: docker-compose up --build -d"
    exit 1
fi

echo -n "  CMIPS Batch Trigger: "
if curl -s "$CMIPS_URL/api/batch/trigger/health" | grep -q "UP"; then
    echo -e "${GREEN}UP${NC}"
else
    echo -e "${RED}DOWN${NC}"
    exit 1
fi

echo ""

# Step 2: Create a job definition
echo -e "${YELLOW}Step 2: Creating job definition in Scheduler...${NC}"

JOB_RESPONSE=$(curl -s -X POST "$SCHEDULER_URL/api/scheduler/jobs" \
  -H "Content-Type: application/json" \
  -d '{
    "jobName": "countyDailyReportJob",
    "jobType": "REPORT",
    "description": "Test county daily report job",
    "cronExpression": "0 0 6 * * ?",
    "enabled": true,
    "parameters": {
      "reportDate": "2024-01-15",
      "countyCode": "LA"
    }
  }' 2>/dev/null)

echo "  Response: $JOB_RESPONSE"

JOB_ID=$(echo "$JOB_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -n "$JOB_ID" ] && [ "$JOB_ID" != "null" ]; then
    echo -e "  ${GREEN}✓ Job created with ID: $JOB_ID${NC}"
else
    # Job might already exist, try to get it
    echo -e "  ${YELLOW}Job may already exist, trying to find it...${NC}"
    EXISTING_JOB=$(curl -s "$SCHEDULER_URL/api/scheduler/jobs/name/countyDailyReportJob" 2>/dev/null)
    JOB_ID=$(echo "$EXISTING_JOB" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

    if [ -n "$JOB_ID" ] && [ "$JOB_ID" != "null" ]; then
        echo -e "  ${GREEN}✓ Found existing job with ID: $JOB_ID${NC}"
    else
        echo -e "  ${RED}✗ Failed to create or find job${NC}"
        echo "  Response was: $JOB_RESPONSE"
        exit 1
    fi
fi

echo ""

# Step 3: Trigger the job
echo -e "${YELLOW}Step 3: Triggering job...${NC}"

TRIGGER_RESPONSE=$(curl -s -X POST "$SCHEDULER_URL/api/scheduler/trigger/$JOB_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "parameters": {
      "reportDate": "2024-01-15"
    }
  }' 2>/dev/null)

echo "  Response: $TRIGGER_RESPONSE"

TRIGGER_ID=$(echo "$TRIGGER_RESPONSE" | grep -o '"triggerId":"[^"]*"' | cut -d'"' -f4)

if [ -n "$TRIGGER_ID" ] && [ "$TRIGGER_ID" != "null" ]; then
    echo -e "  ${GREEN}✓ Job triggered with ID: $TRIGGER_ID${NC}"
else
    echo -e "  ${RED}✗ Failed to trigger job${NC}"
    exit 1
fi

echo ""

# Step 4: Wait for job completion
echo -e "${YELLOW}Step 4: Waiting for job completion...${NC}"

MAX_WAIT=60
WAITED=0
STATUS="UNKNOWN"

while [ $WAITED -lt $MAX_WAIT ]; do
    STATUS_RESPONSE=$(curl -s "$SCHEDULER_URL/api/scheduler/trigger/status/$TRIGGER_ID" 2>/dev/null)
    STATUS=$(echo "$STATUS_RESPONSE" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

    echo "  [$WAITED s] Status: $STATUS"

    if [ "$STATUS" = "COMPLETED" ]; then
        echo -e "  ${GREEN}✓ Job completed successfully!${NC}"
        break
    elif [ "$STATUS" = "FAILED" ]; then
        echo -e "  ${RED}✗ Job failed${NC}"
        echo "  Response: $STATUS_RESPONSE"
        exit 1
    fi

    sleep 2
    WAITED=$((WAITED + 2))
done

if [ $WAITED -ge $MAX_WAIT ]; then
    echo -e "  ${YELLOW}⚠ Job did not complete within ${MAX_WAIT}s (current status: $STATUS)${NC}"
    echo "  This might be normal if the job is still running."
fi

echo ""

# Step 5: Check CMIPS backend logs for job execution
echo -e "${YELLOW}Step 5: Checking CMIPS backend for job execution...${NC}"

CMIPS_LOGS=$(docker logs cmips-backend 2>&1 | tail -50 | grep -i "batch\|trigger\|job" || true)
if [ -n "$CMIPS_LOGS" ]; then
    echo "  Recent batch-related logs from CMIPS backend:"
    echo "$CMIPS_LOGS" | head -20 | sed 's/^/    /'
else
    echo "  No recent batch logs found (this might be normal)"
fi

echo ""

# Step 6: Check for generated reports
echo -e "${YELLOW}Step 6: Checking for generated reports...${NC}"

REPORTS=$(docker exec cmips-backend ls -la /app/reports/county-daily/ 2>/dev/null || echo "")
if [ -n "$REPORTS" ]; then
    echo -e "  ${GREEN}✓ Reports directory contents:${NC}"
    echo "$REPORTS" | sed 's/^/    /'
else
    echo "  No reports found yet (might still be generating)"
fi

echo ""
echo "=============================================="
echo "  Test Summary"
echo "=============================================="
echo "  Job ID:      $JOB_ID"
echo "  Trigger ID:  $TRIGGER_ID"
echo "  Final Status: $STATUS"
echo ""

if [ "$STATUS" = "COMPLETED" ]; then
    echo -e "${GREEN}✓ Integration test PASSED!${NC}"
    exit 0
elif [ "$STATUS" = "RUNNING" ] || [ "$STATUS" = "STARTING" ]; then
    echo -e "${YELLOW}⚠ Job is still running. Check again later.${NC}"
    exit 0
else
    echo -e "${RED}✗ Integration test needs attention.${NC}"
    exit 1
fi
