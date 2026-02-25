# Scheduler → CMIPS Integration Test Documentation

## Table of Contents
1. [System Overview](#system-overview)
2. [Architecture](#architecture)
3. [Communication Flow](#communication-flow)
4. [Services & Ports](#services--ports)
5. [Integration Test Process](#integration-test-process)
6. [API Endpoints](#api-endpoints)
7. [Redis Pub/Sub Events](#redis-pubsub-events)
8. [Database Schema](#database-schema)
9. [Running the Test](#running-the-test)
10. [Troubleshooting](#troubleshooting)

---

## System Overview

The Batch Scheduler system consists of two main applications working together:

| Component | Role | Responsibility |
|-----------|------|----------------|
| **Scheduler App** | "The Brain" | Job definitions, scheduling, dependencies, triggering |
| **CMIPS App** | "The Muscle" | Spring Batch job execution, report generation |

### Why Two Applications?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          SEPARATION OF CONCERNS                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  SCHEDULER (Brain)                    TRIAL (Muscle)                         │
│  ┌─────────────────────┐              ┌─────────────────────┐               │
│  │ • Job Definitions   │   REST API   │ • Spring Batch Jobs │               │
│  │ • Cron Schedules    │ ──────────►  │ • Data Processing   │               │
│  │ • Dependencies      │              │ • Report Generation │               │
│  │ • Calendar Rules    │  ◄────────── │ • File Output       │               │
│  │ • Execution History │   Redis      │ • Virtual Threads   │               │
│  └─────────────────────┘   Pub/Sub    └─────────────────────┘               │
│                                                                              │
│  Lightweight, always running          Heavy processing, scales independently │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Architecture

### High-Level Architecture Diagram

```
                                    ┌──────────────────┐
                                    │   User/Browser   │
                                    │  localhost:3002  │
                                    └────────┬─────────┘
                                             │
                                             ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              DOCKER COMPOSE NETWORK                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────────────┐         ┌─────────────────────┐                    │
│  │  Scheduler Frontend │         │  Scheduler Backend  │                    │
│  │    (Next.js)        │ ──────► │   (Spring Boot)     │                    │
│  │    Port: 3002       │   API   │    Port: 8084       │                    │
│  └─────────────────────┘         └──────────┬──────────┘                    │
│                                             │                                │
│                                             │ REST API                       │
│                                             │ POST /api/batch/trigger/start  │
│                                             ▼                                │
│                                  ┌─────────────────────┐                    │
│                                  │   CMIPS Backend     │                    │
│                                  │   (Spring Batch)    │                    │
│                                  │    Port: 8081       │                    │
│                                  └──────────┬──────────┘                    │
│                                             │                                │
│                         Redis Pub/Sub       │                                │
│                    ┌────────────────────────┼────────────────────────┐      │
│                    │                        ▼                        │      │
│           ┌────────┴────────┐      ┌─────────────────┐              │      │
│           │     Redis       │      │   PostgreSQL    │              │      │
│           │   Port: 6380    │      │   Port: 5432    │              │      │
│           │  (Event Bus)    │      │  (Shared DB)    │              │      │
│           └─────────────────┘      └─────────────────┘              │      │
│                    │                                                 │      │
│                    └─────────────────────────────────────────────────┘      │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Technology Stack

| Layer | Technology | Version |
|-------|------------|---------|
| Frontend | Next.js | 14.x |
| Backend (Scheduler) | Spring Boot | 3.2.0 |
| Backend (CMIPS) | Spring Boot + Spring Batch | 3.2.0 |
| Database | PostgreSQL | 15 |
| Message Broker | Redis | 7 |
| Runtime | Java | 21 (Virtual Threads) |
| Container | Docker Compose | 3.8 |

---

## Communication Flow

### Job Trigger Flow (Step by Step)

```
┌──────────┐    ┌───────────┐    ┌─────────────┐    ┌───────────┐    ┌─────────┐
│  User    │    │ Scheduler │    │  Scheduler  │    │   CMIPS   │    │  Redis  │
│ Frontend │    │  Backend  │    │   Database  │    │  Backend  │    │         │
└────┬─────┘    └─────┬─────┘    └──────┬──────┘    └─────┬─────┘    └────┬────┘
     │                │                 │                 │               │
     │ 1. Click       │                 │                 │               │
     │   "Trigger"    │                 │                 │               │
     │───────────────►│                 │                 │               │
     │                │                 │                 │               │
     │                │ 2. Create       │                 │               │
     │                │   ExecutionMapping                │               │
     │                │────────────────►│                 │               │
     │                │                 │                 │               │
     │                │ 3. POST /api/batch/trigger/start  │               │
     │                │────────────────────────────────────►               │
     │                │                 │                 │               │
     │                │                 │    4. Start     │               │
     │                │                 │    Spring Batch │               │
     │                │                 │    Job          │               │
     │                │                 │                 │               │
     │                │                 │                 │ 5. PUBLISH    │
     │                │                 │                 │   job-started │
     │                │                 │                 │──────────────►│
     │                │                 │                 │               │
     │                │                 │                 │ 6. PUBLISH    │
     │                │                 │                 │   job-progress│
     │                │                 │                 │──────────────►│
     │                │ 7. SUBSCRIBE    │                 │               │
     │                │◄────────────────────────────────────────────────────
     │                │                 │                 │               │
     │                │ 8. Update       │                 │               │
     │                │   ExecutionMapping                │               │
     │                │────────────────►│                 │               │
     │                │                 │                 │               │
     │                │                 │                 │ 9. PUBLISH    │
     │                │                 │                 │   job-completed│
     │                │                 │                 │──────────────►│
     │                │                 │                 │               │
     │                │ 10. Final Update│                 │               │
     │                │────────────────►│                 │               │
     │                │                 │                 │               │
     │ 11. Poll for   │                 │                 │               │
     │    status      │                 │                 │               │
     │───────────────►│                 │                 │               │
     │                │                 │                 │               │
     │◄───────────────│                 │                 │               │
     │  COMPLETED     │                 │                 │               │
     │                │                 │                 │               │
```

### Detailed Flow Description

| Step | Action | Description |
|------|--------|-------------|
| 1 | User triggers job | User clicks "Trigger" button on frontend |
| 2 | Create execution record | Scheduler creates `ExecutionMapping` with status `TRIGGERED` |
| 3 | Call CMIPS API | Scheduler sends POST request to CMIPS's `/api/batch/trigger/start` |
| 4 | Start batch job | CMIPS starts Spring Batch job on virtual thread |
| 5 | Publish started event | CMIPS publishes to `batch:events:job-started` Redis channel |
| 6 | Publish progress events | CMIPS publishes step completion to `batch:events:job-progress` |
| 7 | Subscribe to events | Scheduler receives Redis events via subscription |
| 8 | Update execution | Scheduler updates `ExecutionMapping` with progress |
| 9 | Publish completed event | CMIPS publishes to `batch:events:job-completed` |
| 10 | Final update | Scheduler marks execution as `COMPLETED` |
| 11 | Frontend polls | Frontend polls for status and displays result |

---

## Services & Ports

### Docker Compose Services

| Service | Container Name | Internal Port | External Port | Health Check |
|---------|---------------|---------------|---------------|--------------|
| PostgreSQL | scheduler-postgres | 5432 | 5432 | `pg_isready` |
| Redis | scheduler-redis | 6379 | 6380 | `redis-cli ping` |
| Scheduler Backend | scheduler-backend | 8084 | 8084 | `/actuator/health` |
| CMIPS Backend | cmips-backend | 8081 | 8081 | `/actuator/health` |
| Scheduler Frontend | scheduler-frontend | 3002 | 3002 | HTTP 200 |

### Environment Variables

#### Scheduler Backend
```yaml
SPRING_PROFILES_ACTIVE: docker
DB_HOST: postgres
DB_PORT: 5432
DB_NAME: scheduler_db
REDIS_HOST: redis
REDIS_PORT: 6379
CMIPS_BACKEND_URL: http://cmips-backend:8081
```

#### CMIPS Backend
```yaml
SPRING_PROFILES_ACTIVE: docker
DB_HOST: postgres
DB_PORT: 5432
DB_NAME: scheduler_db
REDIS_HOST: redis
REDIS_PORT: 6379
SCHEDULER_APP_URL: http://scheduler-backend:8084
SCHEDULER_CHANNEL_JOB_STARTED: batch:events:job-started
SCHEDULER_CHANNEL_JOB_PROGRESS: batch:events:job-progress
SCHEDULER_CHANNEL_JOB_COMPLETED: batch:events:job-completed
SCHEDULER_CHANNEL_JOB_FAILED: batch:events:job-failed
```

---

## Integration Test Process

### Test Script: `test-integration.sh`

The integration test performs the following steps:

#### Step 1: Health Check
```bash
# Check all services are running
curl -s "$SCHEDULER_URL/actuator/health" | grep -q "UP"
curl -s "$TRIAL_URL/actuator/health" | grep -q "UP"
curl -s "$TRIAL_URL/api/batch/trigger/health" | grep -q "UP"
```

#### Step 2: Create Job Definition
```bash
curl -X POST "$SCHEDULER_URL/api/scheduler/jobs" \
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
  }'
```

**Response:**
```json
{
  "id": 1,
  "jobName": "countyDailyReportJob",
  "jobType": "REPORT",
  "status": "ACTIVE",
  "enabled": true,
  "createdBy": "test-user"
}
```

#### Step 3: Trigger Job
```bash
curl -X POST "$SCHEDULER_URL/api/scheduler/trigger/1" \
  -H "Content-Type: application/json" \
  -d '{
    "parameters": {
      "reportDate": "2024-01-15"
    }
  }'
```

**Response:**
```json
{
  "id": 2,
  "triggerId": "f43461c9-e5d8-4060-8e6f-747810d2f873",
  "jobDefinitionId": 1,
  "jobName": "countyDailyReportJob",
  "status": "TRIGGERED",
  "triggerType": "MANUAL",
  "triggeredBy": "test-user"
}
```

#### Step 4: Poll for Completion
```bash
# Poll every 2 seconds until COMPLETED or FAILED
while [ $WAITED -lt $MAX_WAIT ]; do
    STATUS=$(curl -s "$SCHEDULER_URL/api/scheduler/trigger/status/$TRIGGER_ID" \
      | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

    if [ "$STATUS" = "COMPLETED" ]; then
        echo "Job completed successfully!"
        break
    fi
    sleep 2
done
```

#### Step 5: Verify Execution
```bash
# Check CMIPS logs for batch execution
docker logs cmips-backend 2>&1 | grep -i "batch\|trigger\|job"

# Check generated reports
docker exec cmips-backend ls -la /app/reports/county-daily/
```

---

## API Endpoints

### Scheduler Backend (Port 8084)

#### Job Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/scheduler/jobs` | List all jobs (paginated) |
| GET | `/api/scheduler/jobs/{id}` | Get job by ID |
| GET | `/api/scheduler/jobs/name/{jobName}` | Get job by name |
| POST | `/api/scheduler/jobs` | Create new job |
| PUT | `/api/scheduler/jobs/{id}` | Update job |
| DELETE | `/api/scheduler/jobs/{id}` | Delete job |

#### Job Control
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/scheduler/jobs/{id}/hold` | Put job on hold |
| POST | `/api/scheduler/jobs/{id}/ice` | Ice job (skip but allow dependents) |
| POST | `/api/scheduler/jobs/{id}/resume` | Resume held/iced job |
| POST | `/api/scheduler/jobs/{id}/enable` | Enable job |
| POST | `/api/scheduler/jobs/{id}/disable` | Disable job |

#### Triggering
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/scheduler/trigger/{jobId}` | Trigger job execution |
| POST | `/api/scheduler/trigger/stop/{triggerId}` | Stop running execution |
| GET | `/api/scheduler/trigger/status/{triggerId}` | Get execution status |
| GET | `/api/scheduler/trigger/running` | Get all running executions |

### CMIPS Backend (Port 8081)

#### Batch Trigger
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/batch/trigger/health` | Health check for trigger endpoint |
| POST | `/api/batch/trigger/start` | Start a batch job |
| POST | `/api/batch/trigger/stop/{executionId}` | Stop a running job |
| GET | `/api/batch/trigger/status/{executionId}` | Get job execution status |

#### Trigger Request Body
```json
{
  "jobName": "countyDailyReportJob",
  "triggerId": "uuid-from-scheduler",
  "schedulerExecutionId": 123,
  "parameters": {
    "reportDate": "2024-01-15",
    "countyCode": "LA"
  }
}
```

#### Trigger Response Body
```json
{
  "success": true,
  "executionId": 1,
  "jobName": "countyDailyReportJob",
  "triggerId": "uuid-from-scheduler",
  "schedulerExecutionId": 123,
  "status": "STARTED",
  "message": "Job started successfully"
}
```

---

## Redis Pub/Sub Events

### Channel Names
| Channel | Publisher | Subscriber | Purpose |
|---------|-----------|------------|---------|
| `batch:events:job-started` | CMIPS | Scheduler | Job execution started |
| `batch:events:job-progress` | CMIPS | Scheduler | Step completed, progress update |
| `batch:events:job-completed` | CMIPS | Scheduler | Job finished successfully |
| `batch:events:job-failed` | CMIPS | Scheduler | Job failed with error |

### Event Payloads

#### Job Started Event
```json
{
  "jobName": "countyDailyReportJob",
  "triggerId": "f43461c9-e5d8-4060-8e6f-747810d2f873",
  "executionId": 1,
  "eventType": "JOB_STARTED",
  "status": "STARTED",
  "startTime": "2025-12-23T12:11:50.916Z",
  "timestamp": "2025-12-23T12:11:50.920Z"
}
```

#### Job Progress Event
```json
{
  "jobName": "countyDailyReportJob",
  "triggerId": "f43461c9-e5d8-4060-8e6f-747810d2f873",
  "executionId": 1,
  "eventType": "STEP_COMPLETED",
  "stepName": "generateDataStep",
  "stepCount": 2,
  "progress": 50,
  "readCount": 0,
  "writeCount": 0,
  "skipCount": 0,
  "status": "STARTED",
  "timestamp": "2025-12-23T12:11:52.981Z"
}
```

#### Job Completed Event
```json
{
  "jobName": "countyDailyReportJob",
  "triggerId": "f43461c9-e5d8-4060-8e6f-747810d2f873",
  "executionId": 1,
  "eventType": "JOB_COMPLETED",
  "status": "COMPLETED",
  "exitCode": "COMPLETED",
  "exitDescription": "",
  "startTime": "2025-12-23T12:11:50.916Z",
  "endTime": "2025-12-23T12:11:53.015Z",
  "duration": 2099,
  "timestamp": "2025-12-23T12:11:53.016Z"
}
```

#### Job Failed Event
```json
{
  "jobName": "countyDailyReportJob",
  "triggerId": "f43461c9-e5d8-4060-8e6f-747810d2f873",
  "executionId": 1,
  "eventType": "JOB_FAILED",
  "status": "FAILED",
  "exitCode": "FAILED",
  "exitDescription": "java.lang.RuntimeException: Database connection failed",
  "errorMessage": "Database connection failed",
  "timestamp": "2025-12-23T12:11:53.016Z"
}
```

---

## Database Schema

### Scheduler Database Tables

#### job_definition
```sql
CREATE TABLE job_definition (
    id BIGSERIAL PRIMARY KEY,
    job_name VARCHAR(255) UNIQUE NOT NULL,
    job_type VARCHAR(50) NOT NULL,
    description TEXT,
    cron_expression VARCHAR(100),
    timezone VARCHAR(50) DEFAULT 'America/Los_Angeles',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    enabled BOOLEAN DEFAULT TRUE,
    priority INT DEFAULT 5,
    max_retries INT DEFAULT 3,
    timeout_seconds INT DEFAULT 3600,
    parameters JSONB,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);
```

#### execution_mapping
```sql
CREATE TABLE execution_mapping (
    id BIGSERIAL PRIMARY KEY,
    trigger_id UUID UNIQUE NOT NULL,
    job_definition_id BIGINT REFERENCES job_definition(id),
    cmips_execution_id BIGINT,
    spring_batch_execution_id BIGINT,
    status VARCHAR(20) NOT NULL,
    trigger_type VARCHAR(20) NOT NULL,
    triggered_at TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    triggered_by VARCHAR(100),
    progress_percentage INT DEFAULT 0,
    progress_message TEXT,
    error_message TEXT,
    retry_count INT DEFAULT 0
);
```

### Spring Batch Metadata Tables (CMIPS)

```sql
-- Created automatically by Spring Batch
CREATE TABLE batch_job_instance (...);
CREATE TABLE batch_job_execution (...);
CREATE TABLE batch_job_execution_params (...);
CREATE TABLE batch_job_execution_context (...);
CREATE TABLE batch_step_execution (...);
CREATE TABLE batch_step_execution_context (...);
```

---

## Running the Test

### Prerequisites
1. Docker and Docker Compose installed
2. Ports 3002, 5432, 6380, 8081, 8084 available

### Start Services
```bash
cd /Users/mythreya/Desktop/batch-scheduler-app
docker-compose up --build -d
```

### Verify All Services Running
```bash
docker-compose ps
```

Expected output:
```
NAME                 STATUS
scheduler-backend    Up (port 8084)
cmips-backend        Up (port 8081)
scheduler-frontend   Up (port 3002)
scheduler-postgres   Up (healthy)
scheduler-redis      Up (healthy)
```

### Run Integration Test
```bash
chmod +x test-integration.sh
./test-integration.sh
```

### Expected Output
```
==============================================
  Scheduler → CMIPS Integration Test
==============================================

Step 1: Checking service health...
  Scheduler Backend: UP
  CMIPS Backend: UP
  CMIPS Batch Trigger: UP

Step 2: Creating job definition in Scheduler...
  ✓ Job created with ID: 1

Step 3: Triggering job...
  ✓ Job triggered with ID: f43461c9-e5d8-4060-8e6f-747810d2f873

Step 4: Waiting for job completion...
  [0 s] Status: TRIGGERED
  [2 s] Status: TRIGGERED
  [4 s] Status: COMPLETED
  ✓ Job completed successfully!

Step 5: Checking CMIPS app for job execution...
  Recent batch-related logs from CMIPS:
    [batch job execution logs...]

Step 6: Checking for generated reports...
  ✓ Reports directory contents:
    county_daily_report_2024-01-15_1766491913001.csv

==============================================
  Test Summary
==============================================
  Job ID:      1
  Trigger ID:  f43461c9-e5d8-4060-8e6f-747810d2f873
  Final Status: COMPLETED

✓ Integration test PASSED!
```

### Access Frontend
Open browser: http://localhost:3002

---

## Troubleshooting

### Common Issues

#### 1. "Access Denied" Error
**Cause:** Security is enabled but no JWT token provided.

**Fix:** Ensure `@EnableMethodSecurity(prePostEnabled = false)` in `SecurityConfig.java`

#### 2. "BATCH_JOB_INSTANCE table not found"
**Cause:** Spring Batch metadata tables not created.

**Fix:** Add `DataSourceInitializer` bean in `SpringBatchConfig.java`:
```java
@Bean
public DataSourceInitializer batchDataSourceInitializer() {
    ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
    populator.addScript(new ClassPathResource("org/springframework/batch/core/schema-postgresql.sql"));
    populator.setContinueOnError(true);

    DataSourceInitializer initializer = new DataSourceInitializer();
    initializer.setDataSource(dataSource);
    initializer.setDatabasePopulator(populator);
    return initializer;
}
```

#### 3. "Connection refused" to CMIPS
**Cause:** CMIPS backend not running or wrong URL.

**Fix:**
```bash
docker-compose logs cmips-backend
docker-compose restart cmips-backend
```

#### 4. Redis events not received
**Cause:** Redis channels not matching between apps.

**Fix:** Verify channel names match in both `application.yml` files:
```yaml
# CMIPS (publisher)
scheduler-app.events.job-completed: batch:events:job-completed

# Scheduler (subscriber)
scheduler.redis.channels.job-completed: batch:events:job-completed
```

### Viewing Logs
```bash
# All logs
docker-compose logs -f

# Specific service
docker-compose logs -f scheduler-backend
docker-compose logs -f cmips-backend

# Filter for errors
docker logs scheduler-backend 2>&1 | grep -i error
```

### Checking Database
```bash
# Connect to PostgreSQL
docker exec -it scheduler-postgres psql -U postgres -d scheduler_db

# View tables
\dt

# Check job definitions
SELECT * FROM job_definition;

# Check executions
SELECT * FROM execution_mapping ORDER BY triggered_at DESC;

# Check Spring Batch executions
SELECT * FROM batch_job_execution ORDER BY start_time DESC;
```

### Checking Redis
```bash
# Connect to Redis
docker exec -it scheduler-redis redis-cli

# Subscribe to events (for debugging)
SUBSCRIBE batch:events:job-started batch:events:job-completed

# Check pub/sub channels
PUBSUB CHANNELS
```

---

## Security Note

**Current State:** Security is DISABLED for testing purposes.

To re-enable security:

1. **Scheduler Backend** (`SecurityConfig.java`):
   - Change `@EnableMethodSecurity(prePostEnabled = false)` to `true`
   - Uncomment the OAuth2 resource server configuration

2. **CMIPS Backend** (`SecurityConfig.java`):
   - Enable JWT validation

3. **Docker Compose**:
   - Uncomment Keycloak service
   - Configure proper Keycloak realm and clients

---

## Files Modified for Testing

| File | Change |
|------|--------|
| `scheduler-backend/.../SecurityConfig.java` | Disabled method security |
| `scheduler-backend/.../SecurityUtils.java` | Added null-safe JWT handling |
| `scheduler-backend/.../JobController.java` | Use SecurityUtils for username |
| `scheduler-backend/.../TriggerController.java` | Use SecurityUtils for username |
| `scheduler-backend/.../CalendarController.java` | Use SecurityUtils for username |
| `scheduler-backend/.../application-docker.yml` | Excluded OAuth2 auto-config |
| `cmips-backend/.../SecurityConfig.java` | Disabled security |
| `cmips-backend/.../SpringBatchConfig.java` | Added schema initializer |
| `docker-compose.yml` | Commented out Keycloak |

---

*Documentation generated: December 23, 2025*
*Integration test status: PASSING*
