# Work Queue & Task Backend — Verification Report

This document summarizes the verification of the backend work queue and task implementation against the **DSD business logic** and the **modern Keycloak-based authorization** approach.

---

## 1. DSD Business Logic — What Was Verified

### 1.1 Work Queue Concepts ✅

| DSD Concept | Backend Implementation | Status |
|-------------|------------------------|--------|
| **30 named queues** | `WorkQueueDataSeeder` seeds 30 queues: Case Owner, Case Worker, Case Home, Case Closure, WPCS Case, Health Care Cert, Form Completion; QA, QA Supervisor; Payments Pending Approval, Payroll Supervisor, Special Transaction Requester, Payment Search by Case, Overpayment Recovery; CDSS Payments Pending, CDSS Career Pathways, Submitted By Worker; Provider Management, Timesheet Eligibility, Blind Visually; Travel Claim, Travel Claim Errors, View Travel Claim, Sick Leave Errors, View Sick Leave, IHSS ESP; SW Reserve Assigned, SW User Approvals, SW My Users, Escalated | ✅ |
| **Queue categories** | `WorkQueue.QueueCategory`: CASE_MGMT, QA, PAYROLL, TRAINING, PROVIDER, INTERNAL_OPS, SUPERVISOR | ✅ |
| **Supervisor-only queues** | `WorkQueue.supervisorOnly`; QA_SUPERVISOR, PAYROLL_SUPERVISOR, SW_* and ESCALATED are `supervisorOnly=true` | ✅ |
| **Subscription model** | `WorkQueueSubscription` entity; `WorkQueueSubscriptionService` subscribe/unsubscribe; subscription blocked for supervisor-only queues | ✅ |

### 1.2 Task Lifecycle (Pull Model, Defer, Forward, Close) ✅

| DSD Concept | Backend Implementation | Status |
|-------------|------------------------|--------|
| **Pull model (OPEN → RESERVED)** | `TaskLifecycleService.reserveTask`, `reserveNextTasks`, `reserveSelectedTasks`; OPEN → RESERVED, set reservedBy/reservedDate | ✅ |
| **Defer with restart date** | `TaskLifecycleService.deferTask(taskId, username, restartDate, comment)`; RESERVED/ASSIGNED → DEFERRED; `DeferredTaskRestartJob` runs every hour and moves DEFERRED → OPEN when `restartDate <= now` | ✅ |
| **Forward** | `TaskLifecycleService.forwardTask(taskId, forwardTo, forwardedBy, comments)`; task goes back to OPEN, assignedTo/forwardedTo set; notification to target | ✅ |
| **Close with comments** | `TaskLifecycleService.closeTask(taskId, username, comments)`; sets closedBy, closedDate, closeComments | ✅ |
| **Escalation** | `TaskEscalationJob` every 15 min; overdue OPEN → escalate to TaskType.escalationTargetQueue or ESCALATED; overdue RESERVED with BOTH/NOT_COMPLETED → escalate; auto-close when TaskType.autoCloseEnabled | ✅ |
| **Task types** | 40+ task types in `WorkQueueDataSeeder` (CI-* codes, target queues, deadlines, escalation targets, auto-close for travel/sick leave errors) | ✅ |

### 1.3 History / Audit Trail ✅

| DSD Requirement | Backend Implementation | Status |
|-----------------|------------------------|--------|
| **Full tracking of who did what, when** | `TaskHistory` entity with TaskAction (CREATED, RESERVED, UNRESERVED, ASSIGNED, FORWARDED, DEFERRED, CLOSED, ESCALATED, RESTARTED, REALLOCATED, COMMENT_ADDED, TIME_MODIFIED, STATUS_CHANGED, AUTO_CLOSED); `recordHistory()` called on every lifecycle change | ✅ |

### 1.4 Scheduled Jobs ✅

| DSD Legacy | Modern Implementation | Status |
|------------|------------------------|--------|
| Mainframe/COBOL batch | `TaskEscalationJob` @Scheduled(fixedDelay = 900000) — 15 min | ✅ |
| Deferred restart | `DeferredTaskRestartJob` @Scheduled(fixedDelay = 3600000) — 1 hour | ✅ |

### 1.5 Real-Time Task Creation (Kafka) ✅

| DSD / Modern | Backend Implementation | Status |
|--------------|------------------------|--------|
| Events → tasks | `TaskEventConsumer` on `cmips-case-events` and `cmips-timesheet-events`; creates tasks for case.created, case.address.changed, timesheet.exception.detected, timesheet.overtime.violation | ✅ |
| Queue alignment | Timesheet exception → TIMESHEET_ELIGIBILITY; Overtime violation → SW_RESERVE_ASSIGNED (supervisor queue); Address validation → PROVIDER_MANAGEMENT | ✅ (fixed) |

---

## 2. Keycloak Authorization — What Was Added

Previously, **Task** and **Work Queue** APIs did not use the same Keycloak permission model as other resources (Case, Timesheet, Referral, etc.). The following changes align them with the modern solution.

### 2.1 New Keycloak Resources (Initializer)

In `CaseManagementKeycloakInitializer`:

- **Work Queue Resource**  
  Scopes: `view`, `reserve`, `subscribe`, `manage`  
  Used for: listing queues, queue tasks, reserve from queue, subscribe/unsubscribe, manage users (for subscription UI).

- **Task Resource**  
  Scopes: `view`, `create`, `reserve`, `forward`, `defer`, `close`, `assign`, `reallocate`  
  Used for: task CRUD, lifecycle operations (reserve, forward, defer, close, reallocate).

**Note:** With `cmips.keycloak.init-resources=true`, these resources are created at startup. You must configure **policies and permissions** in Keycloak (or via script) so that roles such as CASEMANAGEMENTROLE, SUPERVISORROLE, etc. get the appropriate scopes on these resources.

### 2.2 Controller Annotations

- **TaskController**  
  All endpoints now have `@RequirePermission(resource = "Task Resource", scope = "...")` (view, create, reserve, forward, defer, close, reallocate as appropriate).

- **WorkQueueController**  
  All endpoints now have `@RequirePermission(resource = "Work Queue Resource", scope = "...")` (view, reserve, subscribe, manage as appropriate).

### 2.3 Supervisor-Only Queue Enforcement

- **Get queue tasks** (`GET /api/work-queues/{id}/tasks`) and **reserve from queue** (`POST /api/work-queues/{id}/reserve`) now check `queue.isSupervisorOnly()`.  
  If the queue is supervisor-only, the current user must have a supervisor role (e.g. `SUPERVISORROLE` or `SUPERVISOR` from JWT). Otherwise the API returns **403** with a clear message.  
  This enforces the DSD rule that certain queues are restricted to supervisors.

---

## 3. Other Fixes Applied

| Issue | Fix |
|-------|-----|
| **TaskEventConsumer queue names** | Timesheet exception task: `assignedTo`/`workQueue` set to `TIMESHEET_ELIGIBILITY` (was `payroll_queue`). Overtime violation task: `assignedTo`/`workQueue` set to `SW_RESERVE_ASSIGNED` (supervisor queue). |
| **TaskRepository searchByKeyword** | JPQL was invalid (`LIKE %:keyword%`). Replaced with `LOWER(...) LIKE LOWER(CONCAT(CONCAT('%', :keyword), '%'))` on `title` and `subject`. |

---

## 4. Summary

- **DSD alignment:** Queue names, categories, supervisor-only flag, subscription model, task lifecycle (reserve, defer, forward, close with comments), escalation and deferred-restart jobs, task types, and full history/audit are implemented and verified.
- **Keycloak alignment:** Work Queue and Task APIs are now protected by `@RequirePermission` using the new **Work Queue Resource** and **Task Resource**, with cache-first policy evaluation (no Keycloak call on hot path). Supervisor-only queues are enforced at the API layer for queue tasks and reserve.
- **Next steps (Keycloak admin):** Create policies and permissions for **Work Queue Resource** and **Task Resource** so that the appropriate business roles (e.g. CASEMANAGEMENTROLE, SUPERVISORROLE) have the correct scopes (view, reserve, subscribe, manage for queues; view, create, reserve, forward, defer, close, assign, reallocate for tasks).
