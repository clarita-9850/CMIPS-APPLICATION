package com.cmips.service;

import com.cmips.entity.CaseEntity;
import com.cmips.repository.CaseNoteRepository;
import com.cmips.repository.CaseRepository;
import com.cmips.repository.NotificationRepository;
import com.cmips.repository.PersonNoteRepository;
import com.cmips.repository.TimesheetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Retention and Purge Service
 *
 * Implements California IHSS records retention policy:
 *  - Case notes:        purge CANCELLED records older than 5 years
 *  - Processed timesheets: purge records older than 7 years
 *  - Notifications:     purge read notifications older than 90 days
 *  - Closed cases:      flag cases closed more than 7 years ago for archival
 *
 * Schedules (all run at 02:00 AM):
 *  - Daily  at 02:00: notifications cleanup
 *  - Weekly on Sunday 02:00: cancelled case-notes cleanup, old timesheet purge
 *  - Monthly on 1st   02:00: closed case archival flag
 *
 * All jobs are idempotent — safe to re-run if interrupted.
 */
@Service
public class DataRetentionService {

    private static final Logger log = LoggerFactory.getLogger(DataRetentionService.class);

    // Retention thresholds (configurable via constants)
    private static final int NOTIFICATION_RETENTION_DAYS  = 90;
    private static final int CASE_NOTE_RETENTION_YEARS    = 5;
    private static final int TIMESHEET_RETENTION_YEARS    = 7;
    private static final int CLOSED_CASE_ARCHIVE_YEARS    = 7;

    private final CaseNoteRepository     caseNoteRepository;
    private final PersonNoteRepository   personNoteRepository;
    private final TimesheetRepository    timesheetRepository;
    private final NotificationRepository notificationRepository;
    private final CaseRepository         caseRepository;

    @PersistenceContext
    private EntityManager em;

    public DataRetentionService(CaseNoteRepository caseNoteRepository,
                                PersonNoteRepository personNoteRepository,
                                TimesheetRepository timesheetRepository,
                                NotificationRepository notificationRepository,
                                CaseRepository caseRepository) {
        this.caseNoteRepository    = caseNoteRepository;
        this.personNoteRepository  = personNoteRepository;
        this.timesheetRepository   = timesheetRepository;
        this.notificationRepository = notificationRepository;
        this.caseRepository        = caseRepository;
    }

    // ────────────────────────────────────────────────────────────────────────
    // Job 1 — Notifications cleanup (daily at 02:00)
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Purge old read notifications.
     * Runs every day at 02:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void purgeOldNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(NOTIFICATION_RETENTION_DAYS);
        log.info("[DataRetention] purgeOldNotifications: deleting read notifications before {}", cutoff);
        try {
            int deleted = em.createQuery(
                "DELETE FROM Notification n WHERE n.readStatus = true AND n.createdAt < :cutoff")
                .setParameter("cutoff", cutoff)
                .executeUpdate();
            log.info("[DataRetention] purgeOldNotifications: deleted {} records", deleted);
        } catch (Exception e) {
            log.error("[DataRetention] purgeOldNotifications failed", e);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Job 2 — Cancelled case-notes purge (every Sunday at 02:10)
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Purge CANCELLED case notes older than 5 years.
     * CaseNoteEntity uses status='CANCELLED' and cancelledAt timestamp.
     */
    @Scheduled(cron = "0 10 2 * * SUN")
    @Transactional
    public void purgeCancelledCaseNotes() {
        LocalDateTime cutoff = LocalDateTime.now().minusYears(CASE_NOTE_RETENTION_YEARS);
        log.info("[DataRetention] purgeCancelledCaseNotes: deleting CANCELLED notes before {}", cutoff);
        try {
            int deleted = em.createQuery(
                "DELETE FROM CaseNoteEntity cn WHERE cn.status = 'CANCELLED' AND cn.cancelledAt < :cutoff")
                .setParameter("cutoff", cutoff)
                .executeUpdate();
            log.info("[DataRetention] purgeCancelledCaseNotes: deleted {} records", deleted);
        } catch (Exception e) {
            log.error("[DataRetention] purgeCancelledCaseNotes failed", e);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Job 3 — Processed timesheets purge (every Sunday at 02:20)
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Purge PROCESSED timesheets older than 7 years.
     * Timesheets use TimesheetStatus.PROCESSED and createdAt timestamp.
     */
    @Scheduled(cron = "0 20 2 * * SUN")
    @Transactional
    public void purgeOldTimesheets() {
        LocalDateTime cutoff = LocalDateTime.now().minusYears(TIMESHEET_RETENTION_YEARS);
        log.info("[DataRetention] purgeOldTimesheets: deleting PROCESSED timesheets before {}", cutoff);
        try {
            int deleted = em.createQuery(
                "DELETE FROM Timesheet t WHERE t.status = com.cmips.entity.TimesheetStatus.PROCESSED AND t.createdAt < :cutoff")
                .setParameter("cutoff", cutoff)
                .executeUpdate();
            log.info("[DataRetention] purgeOldTimesheets: deleted {} records", deleted);
        } catch (Exception e) {
            log.error("[DataRetention] purgeOldTimesheets failed", e);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Job 4 — Closed-case archival flag (1st of every month at 02:30)
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Identifies cases that have been CLOSED/DENIED/WITHDRAWN for more than
     * 7 years and logs them for archival. Does NOT delete — archival is a
     * manual/downstream step. Runs on the 1st of each month at 02:30 AM.
     *
     * CMIPS data retention policy (California Government Code §12946):
     * IHSS case records must be retained for 7 years after closure.
     */
    @Scheduled(cron = "0 30 2 1 * *")
    @Transactional(readOnly = true)
    public void flagClosedCasesForArchival() {
        LocalDate cutoffDate = LocalDate.now().minusYears(CLOSED_CASE_ARCHIVE_YEARS);
        log.info("[DataRetention] flagClosedCasesForArchival: checking cases closed before {}", cutoffDate);
        try {
            List<CaseEntity> archivable = em.createQuery(
                "SELECT c FROM CaseEntity c WHERE c.caseStatus IN " +
                "('CLOSED', 'DENIED', 'WITHDRAWN', 'TERMINATED') " +
                "AND c.updatedAt < :cutoff",
                CaseEntity.class)
                .setParameter("cutoff", cutoffDate.atStartOfDay())
                .getResultList();

            if (archivable.isEmpty()) {
                log.info("[DataRetention] flagClosedCasesForArchival: no cases ready for archival");
            } else {
                log.warn("[DataRetention] flagClosedCasesForArchival: {} cases exceed {}yr retention threshold — caseIds: {}",
                    archivable.size(),
                    CLOSED_CASE_ARCHIVE_YEARS,
                    archivable.stream().map(c -> c.getId().toString()).collect(java.util.stream.Collectors.joining(", ")));
            }
        } catch (Exception e) {
            log.error("[DataRetention] flagClosedCasesForArchival failed", e);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Job 5 — Inactive person-notes purge (every Sunday at 02:40)
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Purge inactive (active=false) PersonNoteEntity records older than 5 years.
     * PersonNoteEntity uses the 'active' boolean field and 'createdAt' timestamp.
     */
    @Scheduled(cron = "0 40 2 * * SUN")
    @Transactional
    public void purgeInactivePersonNotes() {
        LocalDateTime cutoff = LocalDateTime.now().minusYears(CASE_NOTE_RETENTION_YEARS);
        log.info("[DataRetention] purgeInactivePersonNotes: deleting inactive person notes before {}", cutoff);
        try {
            int deleted = em.createQuery(
                "DELETE FROM PersonNoteEntity pn WHERE pn.active = false AND pn.createdAt < :cutoff")
                .setParameter("cutoff", cutoff)
                .executeUpdate();
            log.info("[DataRetention] purgeInactivePersonNotes: deleted {} records", deleted);
        } catch (Exception e) {
            log.error("[DataRetention] purgeInactivePersonNotes failed", e);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Manual trigger (for admin use / testing)
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Runs all retention jobs immediately.
     * Called by DataRetentionController for admin-triggered runs.
     */
    public RetentionResult runAllJobsNow() {
        log.info("[DataRetention] Manual trigger: running all retention jobs");
        RetentionResult result = new RetentionResult();

        try { purgeOldNotifications();    result.notificationsJob = "OK"; } catch (Exception e) { result.notificationsJob = "ERROR: " + e.getMessage(); }
        try { purgeCancelledCaseNotes();  result.caseNotesJob     = "OK"; } catch (Exception e) { result.caseNotesJob     = "ERROR: " + e.getMessage(); }
        try { purgeOldTimesheets();       result.timesheetsJob    = "OK"; } catch (Exception e) { result.timesheetsJob    = "ERROR: " + e.getMessage(); }
        try { purgeInactivePersonNotes(); result.personNotesJob   = "OK"; } catch (Exception e) { result.personNotesJob   = "ERROR: " + e.getMessage(); }
        try { flagClosedCasesForArchival(); result.archivalFlagJob = "OK"; } catch (Exception e) { result.archivalFlagJob = "ERROR: " + e.getMessage(); }

        log.info("[DataRetention] Manual run complete: {}", result);
        return result;
    }

    /** Simple result DTO for the manual trigger response. */
    public static class RetentionResult {
        public String notificationsJob;
        public String caseNotesJob;
        public String timesheetsJob;
        public String personNotesJob;
        public String archivalFlagJob;
        public String triggeredAt = LocalDateTime.now().toString();

        @Override
        public String toString() {
            return "{notifications=" + notificationsJob +
                ", caseNotes=" + caseNotesJob +
                ", timesheets=" + timesheetsJob +
                ", personNotes=" + personNotesJob +
                ", archivalFlag=" + archivalFlagJob + "}";
        }
    }
}
