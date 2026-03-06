# Transaction Management Analysis & Fixes

## Executive Summary

This document analyzes transaction management across the CMIPS backend service layer and identifies inconsistencies where:
1. Write operations (save, update, delete) are missing `@Transactional`
2. Read operations are missing `@Transactional(readOnly = true)`

## Issues Found

### 1. TaskService - CRITICAL
**File:** `TaskService.java`

**Missing `@Transactional` on write operations:**
- `createTask()` - Line 26
- `updateTaskStatus()` - Line 43
- `updateTask()` - Line 53
- `deleteTask()` - Line 58

**Missing `@Transactional(readOnly = true)` on read operations:**
- `getUserTasks()` - Line 31
- `getUserTasksByStatus()` - Line 35
- `getTaskById()` - Line 39
- `getUserTaskCount()` - Line 62
- `getUserTaskCountByStatus()` - Line 66
- `getTaskStatusByName()` - Line 70
- `getQueueTasks()` - Line 75
- `getQueueTasksByRole()` - Line 80
- `getAvailableQueues()` - Line 85
- `getTasksFromSubscribedQueues()` - Line 104

**Impact:** HIGH - All write operations are not transactional, risking data inconsistency.

---

### 2. ApplicationService - MEDIUM
**File:** `ApplicationService.java`

**Has `@Transactional` on write operations:** ✅ Good

**Missing `@Transactional(readOnly = true)` on read operations:**
- `getApplicationById()` - Line 389
- `getApplicationByNumber()` - Line 394
- `getAllApplications()` - Line 399
- `getApplicationsByCounty()` - Line 403
- `getApplicationsByStatus()` - Line 407
- `getPendingApplications()` - Line 411
- `getPendingApplicationsByCounty()` - Line 415
- `getApplicationsByWorker()` - Line 419
- `getApproachingDeadline()` - Line 423
- `getOverdueApplications()` - Line 427
- `getOverdueApplicationsByCounty()` - Line 431
- `searchApplications()` - Line 435
- `countPendingByCounty()` - Line 449
- `countOverdueByCounty()` - Line 453

**Impact:** MEDIUM - Read operations not optimized with readOnly flag.

---

### 3. ReferralService - MEDIUM
**File:** `ReferralService.java`

**Has `@Transactional` on write operations:** ✅ Good

**Missing `@Transactional(readOnly = true)` on read operations:**
- `getReferralById()` - Line 255
- `getAllReferrals()` - Line 263
- `getReferralsByCounty()` - Line 270
- `getReferralsByStatus()` - Line 277
- `getReferralsByCountyAndStatus()` - Line 284
- `getOpenReferrals()` - Line 291
- `getOpenReferralsByCounty()` - Line 298
- `getReferralsByWorker()` - Line 305
- `getReferralsNeedingFollowUp()` - Line 312
- `getReferralsNeedingFollowUpByCounty()` - Line 319
- `getUrgentReferrals()` - Line 326
- `getUrgentReferralsByCounty()` - Line 333
- `searchReferrals()` - Line 340
- `countOpenReferralsByCounty()` - Line 355
- `countByCountyAndStatus()` - Line 362

**Impact:** MEDIUM - Read operations not optimized with readOnly flag.

---

### 4. CaseManagementService - MEDIUM
**File:** `CaseManagementService.java`

**Has `@Transactional` on write operations:** ✅ Good

**Missing `@Transactional(readOnly = true)` on read operations:**
- `searchCases()` - Line 270
- `getCasesForCaseworker()` - Line 278
- `getActiveCasesByCounty()` - Line 285
- `getCasesDueForReassessment()` - Line 292
- `getCaseNotes()` - Line 368
- `getActiveContacts()` - Line 400
- `findCompanionCases()` - Line 450
- `getCaseStatistics()` - Line 495

**Impact:** MEDIUM - Read operations not optimized with readOnly flag.

---

### 5. PersonService - LOW
**File:** `PersonService.java`

**Has class-level `@Transactional`:** ✅ Good

**Issue:** Read operations should override with `@Transactional(readOnly = true)` for optimization:
- `searchPersons()` - Line 27
- `getPersonById()` - Line 126

**Note:** Class-level `@Transactional` applies to all methods, but read operations should explicitly use `readOnly = true` for better performance.

**Impact:** LOW - Works correctly but not optimized.

---

### 6. ServiceEligibilityService - MEDIUM
**File:** `ServiceEligibilityService.java`

**Has `@Transactional` on write operations:** ✅ Good

**Missing `@Transactional(readOnly = true)` on read operations:**
- All query methods need to be checked

**Impact:** MEDIUM - Read operations not optimized.

---

### 7. ProviderManagementService - MEDIUM
**File:** `ProviderManagementService.java`

**Has `@Transactional` on write operations:** ✅ Good

**Missing `@Transactional(readOnly = true)` on read operations:**
- All query methods need to be checked

**Impact:** MEDIUM - Read operations not optimized.

---

## Transaction Management Best Practices

### 1. Write Operations
- **Always use `@Transactional`** on methods that perform:
  - `save()`, `saveAndFlush()`
  - `delete()`, `deleteById()`
  - `update()` operations
  - Any method that modifies entity state

### 2. Read Operations
- **Always use `@Transactional(readOnly = true)`** on methods that:
  - Only read data (`find*`, `get*`, `search*`, `count*`, `exists*`)
  - Don't modify any entity state
  - Benefits:
    - Optimizes database connection usage
    - Prevents accidental writes
    - Improves performance (database can optimize for read-only)

### 3. Class-Level vs Method-Level
- **Class-level `@Transactional`**: Use when ALL methods need transactions
- **Method-level `@Transactional`**: Use when only specific methods need transactions
- **Override class-level**: Use method-level `@Transactional(readOnly = true)` to override class-level for read operations

### 4. Transaction Propagation
- **Default (`REQUIRED`)**: Join existing transaction or create new one
- **`REQUIRES_NEW`**: Always create new transaction (for audit logging, etc.)
- **`SUPPORTS`**: Use existing transaction if available, otherwise no transaction

---

## Fix Priority

1. **HIGH Priority:**
   - TaskService - Missing `@Transactional` on write operations

2. **MEDIUM Priority:**
   - ApplicationService - Add `@Transactional(readOnly = true)` to read operations
   - ReferralService - Add `@Transactional(readOnly = true)` to read operations
   - CaseManagementService - Add `@Transactional(readOnly = true)` to read operations
   - ServiceEligibilityService - Review and add `@Transactional(readOnly = true)` to read operations
   - ProviderManagementService - Review and add `@Transactional(readOnly = true)` to read operations

3. **LOW Priority:**
   - PersonService - Override class-level with `@Transactional(readOnly = true)` for read operations

---

## Implementation Notes

- All fixes maintain backward compatibility
- No changes to method signatures
- Only adding annotations
- Read operations with `readOnly = true` improve performance and prevent accidental writes
- Write operations with `@Transactional` ensure data consistency
