# Batch Job Scheduler - Complete Implementation Plan

> **Purpose**: This document serves as the master implementation guide. It contains all context needed to build the Scheduler App and modify the CMIPS Backend. If context is lost, read this document first.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Architecture Summary](#2-architecture-summary)
3. [Technology Stack](#3-technology-stack)
4. [Project Structure](#4-project-structure)
5. [Database Schema](#5-database-schema)
6. [Phase 1: Scheduler Backend](#6-phase-1-scheduler-backend)
7. [Phase 2: Scheduler Frontend](#7-phase-2-scheduler-frontend)
8. [Phase 3: CMIPS Backend Modifications](#8-phase-3-trial-app-modifications)
9. [Phase 4: Integration & Testing](#9-phase-4-integration--testing)
10. [API Contracts](#10-api-contracts)
11. [Implementation Checklist](#11-implementation-checklist)

---

## 1. Project Overview

### 1.1 What We're Building

| Component | Description | Port |
|-----------|-------------|------|
| **Scheduler Backend** | Spring Boot app that manages job definitions, scheduling (Quartz), dependencies (DAG), and triggers jobs in CMIPS | 8084 |
| **Scheduler Frontend** | Next.js app providing admin UI for job management, monitoring, and visualization | 3002 |
| **CMIPS Backend (Modified)** | Existing app modified to use Spring Batch for execution, exposing REST APIs for Scheduler to call | 8081 |

### 1.2 Core Principle

```
SCHEDULER APP = BRAIN (decides WHEN to run, manages dependencies)
CMIPS BACKEND = MUSCLE (executes jobs using Spring Batch)
```

### 1.3 Key Directories

```
/Users/mythreya/Desktop/
├── batch-scheduler-app/          # NEW - Scheduler application
│   ├── scheduler-backend/        # Spring Boot backend
│   ├── scheduler-frontend/       # Next.js frontend
│   ├── ARCHITECTURE_DESIGN.html  # Architecture docs
│   ├── ARCHITECTURE_TRADEOFFS.html
│   └── IMPLEMENTATION_PLAN.md    # THIS FILE
│
└── cmips-backend/                        # EXISTING - CMIPS application (to be modified)
    ├── src/main/java/...         # Backend code
    └── timesheet-frontend/       # Frontend code
```

---

## 2. Architecture Summary

### 2.1 System Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         SCHEDULER APP                                   │
│  ┌─────────────────────┐      ┌─────────────────────────────────────┐  │
│  │  Frontend (3002)    │      │  Backend (8084)                     │  │
│  │  - Dashboard        │      │  - Quartz Scheduler                 │  │
│  │  - Job Management   │─────►│  - Dependency Manager (DAG)         │  │
│  │  - Dependency Graph │      │  - Job Config CRUD                  │  │
│  │  - Monitoring       │      │  - Trigger Service                  │  │
│  └─────────────────────┘      └──────────────┬──────────────────────┘  │
└──────────────────────────────────────────────┼──────────────────────────┘
                                               │
                    ┌──────────────────────────┼──────────────────────────┐
                    │                          │                          │
                    ▼                          ▼                          ▼
            ┌──────────────┐          ┌──────────────┐          ┌──────────────┐
            │  PostgreSQL  │          │  REST API    │          │    Redis     │
            │  - job_*     │          │  (to CMIPS)  │          │  - Events    │
            │    tables    │          │              │          │  - Pub/Sub   │
            └──────────────┘          └──────┬───────┘          └──────────────┘
                                             │
                                             ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         CMIPS BACKEND (Spring Batch)                        │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │  REST API Layer                                                  │   │
│  │  - POST /api/batch/trigger                                       │   │
│  │  - GET  /api/batch/jobs/{id}/status                              │   │
│  │  - POST /api/batch/jobs/{id}/stop                                │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │  Spring Batch Core                                               │   │
│  │  - JobLauncher, JobRepository, JobExplorer                       │   │
│  │  - Job definitions with Steps                                    │   │
│  │  - Redis event publishing via listeners                          │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

### 2.2 Integration Points

| Point | Type | Direction | Purpose |
|-------|------|-----------|---------|
| PostgreSQL | Shared DB | Both apps | Scheduler writes config, reads Spring Batch status |
| REST API | HTTP | Scheduler → CMIPS | Trigger, stop, restart, status |
| Redis Pub/Sub | Events | CMIPS → Scheduler | Real-time job updates |
| Keycloak | Auth | Both apps | Shared authentication |

---

## 3. Technology Stack

### 3.1 Scheduler Backend

| Component | Technology | Version |
|-----------|------------|---------|
| Runtime | Java | 21 |
| Framework | Spring Boot | 3.2.x |
| Scheduler | Quartz | 2.3.x |
| Database | PostgreSQL | 15 |
| ORM | Spring Data JPA | 3.2.x |
| Security | Spring Security + OAuth2 | 6.x |
| Redis | Spring Data Redis | 3.2.x |
| API Docs | SpringDoc OpenAPI | 2.x |
| Build | Maven | 3.9.x |

### 3.2 Scheduler Frontend

| Component | Technology | Version |
|-----------|------------|---------|
| Framework | Next.js (App Router) | 14.x |
| Language | TypeScript | 5.x |
| UI | React | 18.x |
| State | TanStack Query | 5.x |
| HTTP | Axios | 1.7.x |
| Styling | CSS Modules | - |
| Auth | Keycloak JS | 24.x |
| Charts | Recharts | 2.x |
| Graph | React Flow | 11.x |

### 3.3 CMIPS Backend Additions

| Component | Technology | Version |
|-----------|------------|---------|
| Batch Framework | Spring Batch | 5.1.x |
| Event Publishing | Spring Data Redis | 3.2.x |

---

## 4. Project Structure

### 4.1 Scheduler Backend Structure

```
scheduler-backend/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/ihss/scheduler/
│   │   │   ├── SchedulerApplication.java
│   │   │   │
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── QuartzConfig.java
│   │   │   │   ├── RedisConfig.java
│   │   │   │   ├── RestTemplateConfig.java
│   │   │   │   └── OpenApiConfig.java
│   │   │   │
│   │   │   ├── entity/
│   │   │   │   ├── JobDefinition.java
│   │   │   │   ├── JobDependency.java
│   │   │   │   ├── JobCalendar.java
│   │   │   │   ├── CalendarDate.java
│   │   │   │   ├── JobExecutionMapping.java
│   │   │   │   └── SchedulerAuditLog.java
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── JobDefinitionRepository.java
│   │   │   │   ├── JobDependencyRepository.java
│   │   │   │   ├── JobCalendarRepository.java
│   │   │   │   ├── JobExecutionMappingRepository.java
│   │   │   │   └── SchedulerAuditLogRepository.java
│   │   │   │
│   │   │   ├── service/
│   │   │   │   ├── JobDefinitionService.java
│   │   │   │   ├── JobDependencyService.java
│   │   │   │   ├── JobCalendarService.java
│   │   │   │   ├── JobTriggerService.java
│   │   │   │   ├── JobMonitorService.java
│   │   │   │   ├── DependencyGraphService.java
│   │   │   │   ├── CronEvaluatorService.java
│   │   │   │   └── AuditService.java
│   │   │   │
│   │   │   ├── scheduler/
│   │   │   │   ├── QuartzJobScheduler.java
│   │   │   │   ├── CronTriggerJob.java
│   │   │   │   └── DependencyTriggerJob.java
│   │   │   │
│   │   │   ├── client/
│   │   │   │   └── CMIPSBatchClient.java
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── JobDefinitionController.java
│   │   │   │   ├── JobDependencyController.java
│   │   │   │   ├── JobCalendarController.java
│   │   │   │   ├── JobTriggerController.java
│   │   │   │   ├── JobMonitorController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   └── HealthController.java
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateJobRequest.java
│   │   │   │   │   ├── UpdateJobRequest.java
│   │   │   │   │   ├── TriggerJobRequest.java
│   │   │   │   │   └── AddDependencyRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── JobDefinitionResponse.java
│   │   │   │       ├── JobStatusResponse.java
│   │   │   │       ├── DependencyGraphResponse.java
│   │   │   │       ├── DashboardStatsResponse.java
│   │   │   │       └── TriggerResponse.java
│   │   │   │
│   │   │   ├── listener/
│   │   │   │   └── RedisEventListener.java
│   │   │   │
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── JobNotFoundException.java
│   │   │   │   ├── CyclicDependencyException.java
│   │   │   │   └── CMIPSAppConnectionException.java
│   │   │   │
│   │   │   └── util/
│   │   │       ├── CronExpressionValidator.java
│   │   │       └── DependencyGraphValidator.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-docker.yml
│   │       └── db/migration/
│   │           ├── V1__create_job_definition.sql
│   │           ├── V2__create_job_dependency.sql
│   │           ├── V3__create_job_calendar.sql
│   │           ├── V4__create_execution_mapping.sql
│   │           └── V5__create_audit_log.sql
│   │
│   └── test/
│       └── java/com/ihss/scheduler/
│           ├── service/
│           ├── controller/
│           └── integration/
│
├── Dockerfile
└── docker-compose.yml
```

### 4.2 Scheduler Frontend Structure

```
scheduler-frontend/
├── package.json
├── tsconfig.json
├── next.config.js
├── .env.local
│
├── app/
│   ├── layout.tsx
│   ├── page.tsx                    # Redirect to dashboard
│   ├── providers.tsx               # React Query, Auth providers
│   │
│   ├── login/
│   │   └── page.tsx
│   │
│   ├── dashboard/
│   │   ├── page.tsx                # Main dashboard
│   │   └── dashboard.module.css
│   │
│   ├── jobs/
│   │   ├── page.tsx                # Job list
│   │   ├── jobs.module.css
│   │   ├── create/
│   │   │   └── page.tsx            # Create job form
│   │   └── [id]/
│   │       ├── page.tsx            # Job details
│   │       └── edit/
│   │           └── page.tsx        # Edit job form
│   │
│   ├── dependencies/
│   │   ├── page.tsx                # Dependency graph
│   │   └── dependencies.module.css
│   │
│   ├── calendars/
│   │   ├── page.tsx                # Calendar list
│   │   ├── create/
│   │   │   └── page.tsx
│   │   └── [id]/
│   │       └── page.tsx
│   │
│   ├── monitoring/
│   │   ├── running/
│   │   │   └── page.tsx            # Running jobs
│   │   ├── history/
│   │   │   └── page.tsx            # Execution history
│   │   └── failed/
│   │       └── page.tsx            # Failed jobs
│   │
│   └── settings/
│       └── page.tsx
│
├── components/
│   ├── layout/
│   │   ├── Sidebar.tsx
│   │   ├── Header.tsx
│   │   ├── PageHeader.tsx
│   │   └── Footer.tsx
│   │
│   ├── common/
│   │   ├── Button.tsx
│   │   ├── Input.tsx
│   │   ├── Select.tsx
│   │   ├── Modal.tsx
│   │   ├── Badge.tsx
│   │   ├── Card.tsx
│   │   ├── Table.tsx
│   │   ├── Spinner.tsx
│   │   ├── EmptyState.tsx
│   │   ├── Toast.tsx
│   │   └── ConfirmDialog.tsx
│   │
│   ├── jobs/
│   │   ├── JobTable.tsx
│   │   ├── JobForm.tsx
│   │   ├── JobCard.tsx
│   │   ├── JobStatusBadge.tsx
│   │   ├── JobActions.tsx
│   │   ├── CronBuilder.tsx
│   │   └── JobParametersEditor.tsx
│   │
│   ├── dependencies/
│   │   ├── DependencyGraph.tsx
│   │   ├── GraphNode.tsx
│   │   ├── GraphEdge.tsx
│   │   └── AddDependencyModal.tsx
│   │
│   ├── monitoring/
│   │   ├── RunningJobsTable.tsx
│   │   ├── ExecutionHistoryTable.tsx
│   │   ├── JobProgressBar.tsx
│   │   └── FailedJobsTable.tsx
│   │
│   └── dashboard/
│       ├── StatCard.tsx
│       ├── JobTypeChart.tsx
│       ├── RecentExecutions.tsx
│       └── DependencyPreview.tsx
│
├── lib/
│   ├── services/
│   │   ├── api.ts                  # Axios instance
│   │   ├── auth.service.ts
│   │   ├── job.service.ts
│   │   ├── dependency.service.ts
│   │   ├── calendar.service.ts
│   │   ├── monitor.service.ts
│   │   └── dashboard.service.ts
│   │
│   ├── hooks/
│   │   ├── useJobs.ts
│   │   ├── useDependencies.ts
│   │   ├── useJobEvents.ts
│   │   └── useAuth.ts
│   │
│   ├── types/
│   │   ├── job.types.ts
│   │   ├── dependency.types.ts
│   │   ├── calendar.types.ts
│   │   └── api.types.ts
│   │
│   └── utils/
│       ├── cron.utils.ts
│       ├── date.utils.ts
│       └── graph.utils.ts
│
├── styles/
│   └── globals.css
│
├── public/
│   └── ...
│
├── Dockerfile
└── docker-compose.yml
```

---

## 5. Database Schema

### 5.1 Scheduler App Tables

```sql
-- =====================================================
-- TABLE: job_definition
-- Purpose: Stores job configurations and schedules
-- =====================================================
CREATE TABLE job_definition (
    id                  BIGSERIAL PRIMARY KEY,
    job_name            VARCHAR(255) NOT NULL UNIQUE,
    job_type            VARCHAR(100) NOT NULL,
    description         TEXT,
    cron_expression     VARCHAR(100),
    timezone            VARCHAR(50) DEFAULT 'America/Los_Angeles',
    status              VARCHAR(20) DEFAULT 'ACTIVE',
    enabled             BOOLEAN DEFAULT true,
    priority            INTEGER DEFAULT 5 CHECK (priority BETWEEN 1 AND 10),
    max_retries         INTEGER DEFAULT 3,
    timeout_seconds     INTEGER DEFAULT 3600,
    job_parameters      JSONB DEFAULT '{}',
    target_roles        TEXT[] DEFAULT '{}',
    target_counties     TEXT[] DEFAULT '{}',
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100),
    deleted_at          TIMESTAMP
);

-- Status values: ACTIVE, INACTIVE, ON_HOLD, ON_ICE
CREATE INDEX idx_job_definition_status ON job_definition(status) WHERE deleted_at IS NULL;
CREATE INDEX idx_job_definition_job_type ON job_definition(job_type);

-- =====================================================
-- TABLE: job_dependency
-- Purpose: Defines parent-child job relationships (DAG)
-- =====================================================
CREATE TABLE job_dependency (
    id                  BIGSERIAL PRIMARY KEY,
    parent_job_id       BIGINT NOT NULL REFERENCES job_definition(id) ON DELETE CASCADE,
    child_job_id        BIGINT NOT NULL REFERENCES job_definition(id) ON DELETE CASCADE,
    dependency_condition VARCHAR(20) DEFAULT 'ON_SUCCESS',
    enabled             BOOLEAN DEFAULT true,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(100),
    UNIQUE(parent_job_id, child_job_id),
    CHECK (parent_job_id != child_job_id)
);

-- Condition values: ON_SUCCESS, ON_FAILURE, ON_COMPLETION
CREATE INDEX idx_job_dependency_parent ON job_dependency(parent_job_id);
CREATE INDEX idx_job_dependency_child ON job_dependency(child_job_id);

-- =====================================================
-- TABLE: job_calendar
-- Purpose: Defines run/exclude calendars
-- =====================================================
CREATE TABLE job_calendar (
    id                  BIGSERIAL PRIMARY KEY,
    calendar_name       VARCHAR(255) NOT NULL UNIQUE,
    description         TEXT,
    calendar_type       VARCHAR(20) NOT NULL,
    timezone            VARCHAR(50) DEFAULT 'America/Los_Angeles',
    enabled             BOOLEAN DEFAULT true,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(100)
);

-- Calendar type values: RUN_ONLY, EXCLUDE

-- =====================================================
-- TABLE: calendar_date
-- Purpose: Specific dates in a calendar
-- =====================================================
CREATE TABLE calendar_date (
    id                  BIGSERIAL PRIMARY KEY,
    calendar_id         BIGINT NOT NULL REFERENCES job_calendar(id) ON DELETE CASCADE,
    calendar_date       DATE NOT NULL,
    description         VARCHAR(255),
    UNIQUE(calendar_id, calendar_date)
);

CREATE INDEX idx_calendar_date_calendar ON calendar_date(calendar_id);
CREATE INDEX idx_calendar_date_date ON calendar_date(calendar_date);

-- =====================================================
-- TABLE: job_calendar_link
-- Purpose: Links jobs to calendars
-- =====================================================
CREATE TABLE job_calendar_link (
    id                  BIGSERIAL PRIMARY KEY,
    job_id              BIGINT NOT NULL REFERENCES job_definition(id) ON DELETE CASCADE,
    calendar_id         BIGINT NOT NULL REFERENCES job_calendar(id) ON DELETE CASCADE,
    UNIQUE(job_id, calendar_id)
);

-- =====================================================
-- TABLE: job_execution_mapping
-- Purpose: Links job definitions to Spring Batch execution IDs
-- =====================================================
CREATE TABLE job_execution_mapping (
    id                      BIGSERIAL PRIMARY KEY,
    job_definition_id       BIGINT NOT NULL REFERENCES job_definition(id),
    spring_batch_execution_id BIGINT NOT NULL,
    trigger_type            VARCHAR(20) NOT NULL,
    triggered_by            VARCHAR(100),
    triggered_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    correlation_id          VARCHAR(36),
    status                  VARCHAR(20),
    completed_at            TIMESTAMP
);

-- Trigger type values: SCHEDULED, MANUAL, DEPENDENCY
CREATE INDEX idx_execution_mapping_job ON job_execution_mapping(job_definition_id);
CREATE INDEX idx_execution_mapping_sb ON job_execution_mapping(spring_batch_execution_id);
CREATE INDEX idx_execution_mapping_triggered ON job_execution_mapping(triggered_at DESC);

-- =====================================================
-- TABLE: scheduler_audit_log
-- Purpose: Audit trail for configuration changes
-- =====================================================
CREATE TABLE scheduler_audit_log (
    id                  BIGSERIAL PRIMARY KEY,
    action              VARCHAR(50) NOT NULL,
    entity_type         VARCHAR(50) NOT NULL,
    entity_id           BIGINT,
    entity_name         VARCHAR(255),
    old_value           JSONB,
    new_value           JSONB,
    performed_by        VARCHAR(100) NOT NULL,
    performed_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address          VARCHAR(45),
    user_agent          TEXT
);

-- Action values: CREATE, UPDATE, DELETE, HOLD, ICE, RESUME, TRIGGER, STOP, RESTART
CREATE INDEX idx_audit_log_entity ON scheduler_audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_performed ON scheduler_audit_log(performed_at DESC);
CREATE INDEX idx_audit_log_action ON scheduler_audit_log(action);
```

### 5.2 Spring Batch Tables (Auto-created by Spring Batch)

```
BATCH_JOB_INSTANCE
BATCH_JOB_EXECUTION
BATCH_JOB_EXECUTION_PARAMS
BATCH_JOB_EXECUTION_CONTEXT
BATCH_STEP_EXECUTION
BATCH_STEP_EXECUTION_CONTEXT
```

---

## 6. Phase 1: Scheduler Backend

### 6.1 Setup Tasks

| # | Task | Description | Files |
|---|------|-------------|-------|
| 1.1 | Create Maven project | Initialize Spring Boot project with dependencies | `pom.xml` |
| 1.2 | Configure application properties | Database, Redis, Keycloak settings | `application.yml` |
| 1.3 | Setup security config | OAuth2 with Keycloak | `SecurityConfig.java` |
| 1.4 | Create Flyway migrations | Database schema creation | `V1__*.sql` to `V5__*.sql` |
| 1.5 | Configure Quartz | Scheduler configuration | `QuartzConfig.java` |
| 1.6 | Configure Redis | Pub/Sub listener setup | `RedisConfig.java` |

### 6.2 Entity & Repository Tasks

| # | Task | Description | Files |
|---|------|-------------|-------|
| 1.7 | Create JobDefinition entity | JPA entity with all fields | `JobDefinition.java` |
| 1.8 | Create JobDependency entity | With parent/child relationships | `JobDependency.java` |
| 1.9 | Create JobCalendar entities | Calendar and CalendarDate | `JobCalendar.java`, `CalendarDate.java` |
| 1.10 | Create JobExecutionMapping entity | Links to Spring Batch | `JobExecutionMapping.java` |
| 1.11 | Create SchedulerAuditLog entity | Audit trail | `SchedulerAuditLog.java` |
| 1.12 | Create all repositories | JPA repositories with custom queries | `*Repository.java` |

### 6.3 Service Layer Tasks

| # | Task | Description | Files |
|---|------|-------------|-------|
| 1.13 | JobDefinitionService | CRUD, status control, validation | `JobDefinitionService.java` |
| 1.14 | JobDependencyService | Add/remove deps, cycle detection | `JobDependencyService.java` |
| 1.15 | DependencyGraphService | Build graph, topological sort | `DependencyGraphService.java` |
| 1.16 | JobCalendarService | Calendar CRUD, date management | `JobCalendarService.java` |
| 1.17 | CronEvaluatorService | Parse and evaluate cron expressions | `CronEvaluatorService.java` |
| 1.18 | CMIPSBatchClient | REST client to call CMIPS app | `CMIPSBatchClient.java` |
| 1.19 | JobTriggerService | Trigger jobs, handle retries | `JobTriggerService.java` |
| 1.20 | JobMonitorService | Get status from CMIPS, aggregate | `JobMonitorService.java` |
| 1.21 | AuditService | Log all configuration changes | `AuditService.java` |

### 6.4 Quartz Scheduler Tasks

| # | Task | Description | Files |
|---|------|-------------|-------|
| 1.22 | QuartzJobScheduler | Register/unregister jobs with Quartz | `QuartzJobScheduler.java` |
| 1.23 | CronTriggerJob | Quartz job that evaluates cron and triggers | `CronTriggerJob.java` |
| 1.24 | DependencyTriggerJob | Triggers dependent jobs after parent completes | `DependencyTriggerJob.java` |

### 6.5 Controller Tasks

| # | Task | Description | Files |
|---|------|-------------|-------|
| 1.25 | JobDefinitionController | CRUD endpoints | `JobDefinitionController.java` |
| 1.26 | JobDependencyController | Dependency endpoints | `JobDependencyController.java` |
| 1.27 | JobCalendarController | Calendar endpoints | `JobCalendarController.java` |
| 1.28 | JobTriggerController | Trigger, stop, restart | `JobTriggerController.java` |
| 1.29 | JobMonitorController | Running jobs, history | `JobMonitorController.java` |
| 1.30 | DashboardController | Aggregated stats | `DashboardController.java` |
| 1.31 | HealthController | Health checks | `HealthController.java` |

### 6.6 Event Listener Tasks

| # | Task | Description | Files |
|---|------|-------------|-------|
| 1.32 | RedisEventListener | Subscribe to CMIPS app events | `RedisEventListener.java` |
| 1.33 | Handle job-started event | Update UI via SSE | In listener |
| 1.34 | Handle job-progress event | Update progress | In listener |
| 1.35 | Handle job-completed event | Trigger dependents | In listener |
| 1.36 | Handle job-failed event | Log, notify | In listener |

### 6.7 DTO Tasks

| # | Task | Description | Files |
|---|------|-------------|-------|
| 1.37 | Create request DTOs | CreateJob, UpdateJob, TriggerJob, etc. | `dto/request/*.java` |
| 1.38 | Create response DTOs | JobDefinition, JobStatus, Graph, etc. | `dto/response/*.java` |

### 6.8 Exception Handling

| # | Task | Description | Files |
|---|------|-------------|-------|
| 1.39 | GlobalExceptionHandler | Handle all exceptions | `GlobalExceptionHandler.java` |
| 1.40 | Custom exceptions | JobNotFound, CyclicDependency, etc. | `exception/*.java` |

---

## 7. Phase 2: Scheduler Frontend

### 7.1 Setup Tasks

| # | Task | Description | Files |
|---|------|-------------|-------|
| 2.1 | Create Next.js project | Initialize with TypeScript | `package.json` |
| 2.2 | Configure environment | API URL, Keycloak settings | `.env.local` |
| 2.3 | Setup providers | React Query, Auth context | `providers.tsx` |
| 2.4 | Configure Axios | Interceptors, token refresh | `lib/services/api.ts` |
| 2.5 | Setup global styles | CSS variables, base styles | `styles/globals.css` |

### 7.2 Layout Components

| # | Task | Description | Files |
|---|------|-------------|-------|
| 2.6 | Sidebar component | Navigation menu | `components/layout/Sidebar.tsx` |
| 2.7 | Header component | User info, logout | `components/layout/Header.tsx` |
| 2.8 | Root layout | Combine sidebar, header | `app/layout.tsx` |
| 2.9 | PageHeader component | Title + actions for pages | `components/layout/PageHeader.tsx` |

### 7.3 Common Components

| # | Task | Description | Files |
|---|------|-------------|-------|
| 2.10 | Button component | Primary, secondary, danger | `components/common/Button.tsx` |
| 2.11 | Input component | Text, textarea, validation | `components/common/Input.tsx` |
| 2.12 | Select component | Dropdown, multi-select | `components/common/Select.tsx` |
| 2.13 | Modal component | Dialog with overlay | `components/common/Modal.tsx` |
| 2.14 | Table component | Sortable, selectable | `components/common/Table.tsx` |
| 2.15 | Badge component | Status indicators | `components/common/Badge.tsx` |
| 2.16 | Card component | Container with header | `components/common/Card.tsx` |
| 2.17 | Spinner component | Loading indicator | `components/common/Spinner.tsx` |
| 2.18 | Toast component | Notifications | `components/common/Toast.tsx` |
| 2.19 | ConfirmDialog | Confirmation modal | `components/common/ConfirmDialog.tsx` |

### 7.4 Service Layer

| # | Task | Description | Files |
|---|------|-------------|-------|
| 2.20 | Auth service | Login, logout, token | `lib/services/auth.service.ts` |
| 2.21 | Job service | CRUD operations | `lib/services/job.service.ts` |
| 2.22 | Dependency service | Graph operations | `lib/services/dependency.service.ts` |
| 2.23 | Calendar service | Calendar CRUD | `lib/services/calendar.service.ts` |
| 2.24 | Monitor service | Status, history | `lib/services/monitor.service.ts` |
| 2.25 | Dashboard service | Aggregated stats | `lib/services/dashboard.service.ts` |

### 7.5 Type Definitions

| # | Task | Description | Files |
|---|------|-------------|-------|
| 2.26 | Job types | JobDefinition, JobStatus | `lib/types/job.types.ts` |
| 2.27 | Dependency types | Dependency, Graph | `lib/types/dependency.types.ts` |
| 2.28 | Calendar types | Calendar, Date | `lib/types/calendar.types.ts` |
| 2.29 | API types | Responses, errors | `lib/types/api.types.ts` |

### 7.6 Dashboard Page

| # | Task | Description | Files |
|---|------|-------------|-------|
| 2.30 | Dashboard page | Main dashboard | `app/dashboard/page.tsx` |
| 2.31 | StatCard component | Metric cards | `components/dashboard/StatCard.tsx` |
| 2.32 | JobTypeChart | Pie chart | `components/dashboard/JobTypeChart.tsx` |
| 2.33 | RecentExecutions | Recent jobs list | `components/dashboard/RecentExecutions.tsx` |
| 2.34 | DependencyPreview | Mini graph | `components/dashboard/DependencyPreview.tsx` |

### 7.7 Jobs Pages

| # | Task | Description | Files |
|---|------|-------------|-------|
| 2.35 | Jobs list page | Table with filters | `app/jobs/page.tsx` |
| 2.36 | JobTable component | Sortable, actions | `components/jobs/JobTable.tsx` |
| 2.37 | JobStatusBadge | Status indicator | `components/jobs/JobStatusBadge.tsx` |
| 2.38 | JobActions | Action buttons | `components/jobs/JobActions.tsx` |
| 2.39 | Create job page | Job form | `app/jobs/create/page.tsx` |
| 2.40 | JobForm component | Reusable form | `components/jobs/JobForm.tsx` |
| 2.41 | CronBuilder | Visual cron editor | `components/jobs/CronBuilder.tsx` |
| 2.42 | Job details page | View job | `app/jobs/[id]/page.tsx` |
| 2.43 | Edit job page | Edit form | `app/jobs/[id]/edit/page.tsx` |

### 7.8 Dependencies Page

| # | Task | Description | Files |
|---|------|-------------|-------|
| 2.44 | Dependencies page | Graph view | `app/dependencies/page.tsx` |
| 2.45 | DependencyGraph | React Flow graph | `components/dependencies/DependencyGraph.tsx` |
| 2.46 | GraphNode | Custom node | `components/dependencies/GraphNode.tsx` |
| 2.47 | AddDependencyModal | Add dep dialog | `components/dependencies/AddDependencyModal.tsx` |

### 7.9 Calendars Pages

| # | Task | Description | Files |
|---|------|-------------|-------|
| 2.48 | Calendars list page | Calendar table | `app/calendars/page.tsx` |
| 2.49 | Create calendar page | Calendar form | `app/calendars/create/page.tsx` |
| 2.50 | Calendar details page | View/edit dates | `app/calendars/[id]/page.tsx` |

### 7.10 Monitoring Pages

| # | Task | Description | Files |
|---|------|-------------|-------|
| 2.51 | Running jobs page | Active jobs | `app/monitoring/running/page.tsx` |
| 2.52 | RunningJobsTable | With progress | `components/monitoring/RunningJobsTable.tsx` |
| 2.53 | JobProgressBar | Progress indicator | `components/monitoring/JobProgressBar.tsx` |
| 2.54 | History page | Execution history | `app/monitoring/history/page.tsx` |
| 2.55 | ExecutionHistoryTable | Filterable | `components/monitoring/ExecutionHistoryTable.tsx` |
| 2.56 | Failed jobs page | Failed queue | `app/monitoring/failed/page.tsx` |
| 2.57 | FailedJobsTable | With retry | `components/monitoring/FailedJobsTable.tsx` |

### 7.11 Hooks

| # | Task | Description | Files |
|---|------|-------------|-------|
| 2.58 | useJobs hook | Job data fetching | `lib/hooks/useJobs.ts` |
| 2.59 | useDependencies hook | Graph data | `lib/hooks/useDependencies.ts` |
| 2.60 | useJobEvents hook | SSE subscription | `lib/hooks/useJobEvents.ts` |
| 2.61 | useAuth hook | Auth state | `lib/hooks/useAuth.ts` |

---

## 8. Phase 3: CMIPS Backend Modifications

### 8.1 Spring Batch Setup

| # | Task | Description | Files |
|---|------|-------------|-------|
| 3.1 | Add Spring Batch dependency | Update pom.xml | `pom.xml` |
| 3.2 | Configure Spring Batch | DataSource, JobRepository | `BatchConfig.java` |
| 3.3 | Create batch schema | Initialize BATCH_* tables | `schema-postgresql.sql` |

### 8.2 REST API for Scheduler

| # | Task | Description | Files |
|---|------|-------------|-------|
| 3.4 | BatchTriggerController | New controller | `BatchTriggerController.java` |
| 3.5 | POST /api/batch/trigger | Trigger job endpoint | In controller |
| 3.6 | GET /api/batch/jobs/{id}/status | Status endpoint | In controller |
| 3.7 | POST /api/batch/jobs/{id}/stop | Stop endpoint | In controller |
| 3.8 | POST /api/batch/jobs/{id}/restart | Restart endpoint | In controller |
| 3.9 | GET /api/batch/executions/running | List running | In controller |

### 8.3 Spring Batch Jobs

| # | Task | Description | Files |
|---|------|-------------|-------|
| 3.10 | Convert existing jobs to Spring Batch | Refactor report jobs | `ReportJobConfig.java` |
| 3.11 | Create ItemReader | Read data source | Various readers |
| 3.12 | Create ItemProcessor | Transform data | Various processors |
| 3.13 | Create ItemWriter | Write output | Various writers |
| 3.14 | Configure retry/skip | Fault tolerance | In job config |

### 8.4 Event Publishing

| # | Task | Description | Files |
|---|------|-------------|-------|
| 3.15 | Create JobExecutionListener | Publish job events | `RedisJobExecutionListener.java` |
| 3.16 | Create ChunkListener | Publish progress | `RedisChunkListener.java` |
| 3.17 | Publish job-started | On beforeJob() | In listener |
| 3.18 | Publish job-progress | On afterChunk() | In listener |
| 3.19 | Publish job-completed | On afterJob() success | In listener |
| 3.20 | Publish job-failed | On afterJob() failure | In listener |

### 8.5 Remove Custom Batch Code

| # | Task | Description | Files |
|---|------|-------------|-------|
| 3.21 | Remove custom scheduler | Delete old cron code | Various |
| 3.22 | Remove custom queue | Delete Redis queue code | Various |
| 3.23 | Remove custom executor | Delete job executor | Various |
| 3.24 | Update service layer | Use Spring Batch | Various services |

---

## 9. Phase 4: Integration & Testing

### 9.1 Integration Tasks

| # | Task | Description | Files |
|---|------|-------------|-------|
| 4.1 | Docker Compose setup | All services | `docker-compose.yml` |
| 4.2 | Configure networking | Service discovery | In compose |
| 4.3 | Keycloak client setup | Create scheduler-app client | Keycloak admin |
| 4.4 | Database initialization | Run migrations | Flyway |

### 9.2 Testing Tasks

| # | Task | Description | Files |
|---|------|-------------|-------|
| 4.5 | Unit tests - Backend | Service layer tests | `*ServiceTest.java` |
| 4.6 | Unit tests - Frontend | Component tests | `*.test.tsx` |
| 4.7 | Integration tests | API tests | `*IntegrationTest.java` |
| 4.8 | E2E flow test | Trigger → Execute → Complete | Manual/automated |
| 4.9 | Dependency chain test | Multi-job workflow | Manual/automated |

---

## 10. API Contracts

### 10.1 Scheduler Backend → CMIPS Backend

#### POST /api/batch/trigger
```json
// Request
{
  "jobName": "dailyReportJob",
  "jobParameters": {
    "reportDate": "2024-12-23",
    "format": "PDF"
  },
  "triggeredBy": "scheduler",
  "correlationId": "uuid-1234"
}

// Response (202 Accepted)
{
  "executionId": 12345,
  "jobName": "dailyReportJob",
  "status": "STARTING",
  "startTime": "2024-12-23T10:00:00Z"
}
```

#### GET /api/batch/jobs/{executionId}/status
```json
// Response (200 OK)
{
  "executionId": 12345,
  "jobName": "dailyReportJob",
  "status": "STARTED",
  "startTime": "2024-12-23T10:00:00Z",
  "endTime": null,
  "exitCode": null,
  "exitDescription": null,
  "steps": [
    {
      "stepName": "extractStep",
      "status": "COMPLETED",
      "readCount": 1000,
      "writeCount": 1000,
      "skipCount": 0
    },
    {
      "stepName": "transformStep",
      "status": "STARTED",
      "readCount": 500,
      "writeCount": 450,
      "skipCount": 2
    }
  ]
}
```

#### POST /api/batch/jobs/{executionId}/stop
```json
// Response (200 OK)
{
  "executionId": 12345,
  "status": "STOPPING",
  "message": "Stop signal sent"
}
```

### 10.2 Redis Event Payloads

#### batch:events:job-started
```json
{
  "eventType": "JOB_STARTED",
  "executionId": 12345,
  "jobName": "dailyReportJob",
  "startTime": "2024-12-23T10:00:00Z",
  "correlationId": "uuid-1234"
}
```

#### batch:events:job-progress
```json
{
  "eventType": "JOB_PROGRESS",
  "executionId": 12345,
  "jobName": "dailyReportJob",
  "stepName": "extractStep",
  "readCount": 5000,
  "writeCount": 4950,
  "skipCount": 5,
  "percentComplete": 50
}
```

#### batch:events:job-completed
```json
{
  "eventType": "JOB_COMPLETED",
  "executionId": 12345,
  "jobName": "dailyReportJob",
  "status": "COMPLETED",
  "exitCode": "COMPLETED",
  "startTime": "2024-12-23T10:00:00Z",
  "endTime": "2024-12-23T10:05:00Z",
  "duration": 300000,
  "correlationId": "uuid-1234"
}
```

---

## 11. Implementation Checklist

### Phase 1: Scheduler Backend
- [ ] 1.1-1.6: Setup (6 tasks)
- [ ] 1.7-1.12: Entities & Repositories (6 tasks)
- [ ] 1.13-1.21: Services (9 tasks)
- [ ] 1.22-1.24: Quartz Scheduler (3 tasks)
- [ ] 1.25-1.31: Controllers (7 tasks)
- [ ] 1.32-1.36: Event Listeners (5 tasks)
- [ ] 1.37-1.38: DTOs (2 tasks)
- [ ] 1.39-1.40: Exception Handling (2 tasks)

**Total Phase 1: 40 tasks**

### Phase 2: Scheduler Frontend
- [ ] 2.1-2.5: Setup (5 tasks)
- [ ] 2.6-2.9: Layout (4 tasks)
- [ ] 2.10-2.19: Common Components (10 tasks)
- [ ] 2.20-2.25: Services (6 tasks)
- [ ] 2.26-2.29: Types (4 tasks)
- [ ] 2.30-2.34: Dashboard (5 tasks)
- [ ] 2.35-2.43: Jobs Pages (9 tasks)
- [ ] 2.44-2.47: Dependencies (4 tasks)
- [ ] 2.48-2.50: Calendars (3 tasks)
- [ ] 2.51-2.57: Monitoring (7 tasks)
- [ ] 2.58-2.61: Hooks (4 tasks)

**Total Phase 2: 61 tasks**

### Phase 3: CMIPS Backend Modifications
- [ ] 3.1-3.3: Spring Batch Setup (3 tasks)
- [ ] 3.4-3.9: REST API (6 tasks)
- [ ] 3.10-3.14: Spring Batch Jobs (5 tasks)
- [ ] 3.15-3.20: Event Publishing (6 tasks)
- [ ] 3.21-3.24: Remove Custom Code (4 tasks)

**Total Phase 3: 24 tasks**

### Phase 4: Integration & Testing
- [ ] 4.1-4.4: Integration (4 tasks)
- [ ] 4.5-4.9: Testing (5 tasks)

**Total Phase 4: 9 tasks**

---

## Quick Reference: Key Files to Create

### Scheduler Backend (Most Important)
1. `SchedulerApplication.java` - Entry point
2. `SecurityConfig.java` - OAuth2 setup
3. `QuartzConfig.java` - Scheduler config
4. `JobDefinition.java` - Main entity
5. `JobDefinitionService.java` - Core business logic
6. `JobTriggerService.java` - Triggers jobs in CMIPS
7. `CMIPSBatchClient.java` - REST client
8. `CronTriggerJob.java` - Quartz job
9. `RedisEventListener.java` - Receives CMIPS events

### Scheduler Frontend (Most Important)
1. `app/layout.tsx` - Root layout
2. `components/layout/Sidebar.tsx` - Navigation
3. `lib/services/api.ts` - Axios setup
4. `lib/services/job.service.ts` - Job API calls
5. `app/dashboard/page.tsx` - Main dashboard
6. `app/jobs/page.tsx` - Job list
7. `components/jobs/JobForm.tsx` - Create/edit form
8. `components/dependencies/DependencyGraph.tsx` - Graph visualization

### CMIPS Backend (Most Important)
1. `BatchConfig.java` - Spring Batch config
2. `BatchTriggerController.java` - API for Scheduler
3. `ReportJobConfig.java` - Job definitions
4. `RedisJobExecutionListener.java` - Event publisher

---

## How to Resume Work

If context is lost, follow these steps:

1. **Read this document** completely
2. **Check the Implementation Checklist** to see what's done
3. **Look at existing code** in the directories
4. **Continue from the next unchecked task**

---

*Document Version: 1.0*
*Created: December 23, 2024*
*Last Updated: December 23, 2024*
