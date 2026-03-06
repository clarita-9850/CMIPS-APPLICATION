package com.cmips.batch;

import com.cmips.entity.*;
import com.cmips.entity.ProviderAssignmentEntity.AssignmentStatus;
import com.cmips.entity.OvertimeViolationEntity.ViolationStatus;
import com.cmips.repository.*;
import com.cmips.service.NotificationService;
import com.cmips.service.ProviderManagementService;
import com.cmips.service.ProviderNotificationService;
import com.cmips.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Provider Batch Job Service
 * Implements scheduled background processing jobs for provider enrollment management.
 * All jobs use Spring @Scheduled annotations.
 */
@Service
public class ProviderBatchJobService {

    private static final Logger log = LoggerFactory.getLogger(ProviderBatchJobService.class);

    private final ProviderRepository providerRepository;
    private final ProviderAssignmentRepository providerAssignmentRepository;
    private final OvertimeViolationRepository overtimeViolationRepository;
    private final ProviderManagementService providerManagementService;
    private final ProviderNotificationService providerNotificationService;
    private final NotificationService notificationService;
    private final TaskService taskService;

    public ProviderBatchJobService(
            ProviderRepository providerRepository,
            ProviderAssignmentRepository providerAssignmentRepository,
            OvertimeViolationRepository overtimeViolationRepository,
            ProviderManagementService providerManagementService,
            ProviderNotificationService providerNotificationService,
            NotificationService notificationService,
            TaskService taskService) {
        this.providerRepository = providerRepository;
        this.providerAssignmentRepository = providerAssignmentRepository;
        this.overtimeViolationRepository = overtimeViolationRepository;
        this.providerManagementService = providerManagementService;
        this.providerNotificationService = providerNotificationService;
        this.notificationService = notificationService;
        this.taskService = taskService;
    }

    /**
     * SSN Verification Send - Twice weekly (Tuesday and Thursday at 2 AM)
     * Find providers with status NOT_YET_VERIFIED, create outbound file.
     */
    @Scheduled(cron = "0 0 2 ? * TUE,THU")
    @Transactional(readOnly = true)
    public void sendSsnVerificationBatch() {
        log.info("SSN Verification Send batch job started");

        List<ProviderEntity> providers = providerRepository.findProvidersWithPendingSsnVerification();
        log.info("Found {} providers with pending SSN verification", providers.size());

        for (ProviderEntity provider : providers) {
            // Stub: Create outbound record for SSA verification
            log.info("STUB: SSN verification request for provider {} (SSN: {})",
                    provider.getProviderNumber(), maskSsn(provider.getSsn()));
        }

        log.info("SSN Verification Send batch job completed. {} records processed.", providers.size());
    }

    /**
     * SSN Verification Receive - Twice weekly (Wednesday and Friday at 2 AM)
     * Process inbound SSA verification results.
     */
    @Scheduled(cron = "0 0 2 ? * WED,FRI")
    @Transactional
    public void receiveSsnVerificationBatch() {
        log.info("SSN Verification Receive batch job started");

        // Stub: Process inbound file - in production this would read from file/queue
        log.info("STUB: Processing inbound SSN verification results");

        // Example processing logic:
        // For each result:
        //   - Update provider.ssnVerificationStatus based on SSA response
        //   - If DECEASED → create Task to case owner
        //   - If VERIFIED → check if all enrollment requirements now met

        log.info("SSN Verification Receive batch job completed");
    }

    /**
     * Medi-Cal Suspended Match - Weekly (Sunday at 3 AM)
     * Match providers against Medi-Cal Suspended/Ineligible list.
     */
    @Scheduled(cron = "0 0 3 ? * SUN")
    @Transactional
    public void processMediCalSuspendedBatch() {
        log.info("Medi-Cal Suspended batch job started");

        // Stub: In production, this would read the Medi-Cal suspended list file
        // and match providers on 2 of 3 criteria (Name, SSN, DOB)

        List<ProviderEntity> suspendedProviders = providerRepository.findByMediCalSuspendedTrue();
        log.info("Found {} providers currently on Medi-Cal suspended list", suspendedProviders.size());

        // For new matches (stub - in production would compare against incoming file):
        // 1. Set mediCalSuspended = true
        // 2. Set eligible = NO, ineligibleReason = MEDI_CAL_SUSPENDED
        // 3. Terminate all active assignments
        // 4. Send PVM-01/02/03 notifications

        log.info("Medi-Cal Suspended batch job completed");
    }

    /**
     * Inactive Provider Processing - Monthly (1st of month at 4 AM)
     * Find providers with no payroll activity for 12+ months.
     */
    @Scheduled(cron = "0 0 4 1 * ?")
    @Transactional
    public void processInactiveProviderBatch() {
        log.info("Inactive Provider batch job started");

        LocalDate cutoffDate = LocalDate.now().minusMonths(12);
        List<ProviderEntity> inactiveProviders = providerRepository.findInactiveProvidersForOneYear(cutoffDate);
        log.info("Found {} providers inactive for 12+ months", inactiveProviders.size());

        int processedCount = 0;
        for (ProviderEntity provider : inactiveProviders) {
            if ("YES".equals(provider.getEligible())) {
                try {
                    providerManagementService.setProviderIneligible(
                            provider.getId(), "INACTIVE_NO_PAYROLL_1_YEAR", "SYSTEM_BATCH");
                    processedCount++;
                    log.info("Provider {} set ineligible due to 12+ months inactivity",
                            provider.getProviderNumber());
                } catch (Exception e) {
                    log.error("Error processing inactive provider {}: {}",
                            provider.getProviderNumber(), e.getMessage());
                }
            }
        }

        log.info("Inactive Provider batch job completed. {} providers processed.", processedCount);
    }

    /**
     * Enrollment Due Date Check - Daily (at 6 AM)
     * Find providers where enrollmentBeginDate is 75 days past and eligible=PENDING.
     */
    @Scheduled(cron = "0 0 6 * * ?")
    @Transactional
    public void checkEnrollmentDueDates() {
        log.info("Enrollment Due Date batch job started");

        LocalDate targetDate = LocalDate.now().minusDays(75);
        List<ProviderEntity> providers = providerRepository.findByEligible("PENDING");

        int notifiedCount = 0;
        for (ProviderEntity provider : providers) {
            if (provider.getEnrollmentBeginDate() != null &&
                    !provider.getEnrollmentBeginDate().isAfter(targetDate)) {
                // PVM-37: Send enrollment due date notification
                providerNotificationService.notifyEnrollmentDueDate(provider);
                notifiedCount++;
                log.info("PVM-37: Enrollment due date notification sent for provider {}",
                        provider.getProviderNumber());
            }
        }

        log.info("Enrollment Due Date batch job completed. {} notifications sent.", notifiedCount);
    }

    /**
     * Violation Report Generation - Daily (at 5 AM)
     * Generate overtime violation report data.
     */
    @Scheduled(cron = "0 0 5 * * ?")
    public void generateViolationReport() {
        log.info("Violation Report batch job started");

        // Stub: Generate overtime violation report data
        List<ProviderEntity> providersWithViolations = providerRepository.findProvidersWithOvertimeViolations();
        log.info("STUB: Generating violation report for {} providers", providersWithViolations.size());

        log.info("Violation Report batch job completed");
    }

    /**
     * County Pay Rate Refresh - Monthly (15th of month at 3 AM)
     * Refresh county-specific pay rates.
     */
    @Scheduled(cron = "0 0 3 15 * ?")
    public void refreshCountyPayRates() {
        log.info("County Pay Rate Refresh batch job started");

        // Stub: Refresh county pay rates from external source
        log.info("STUB: Refreshing county pay rates");

        log.info("County Pay Rate Refresh batch job completed");
    }

    /**
     * Send Pending Providers to Labor Org - Weekly (Monday at 7 AM)
     * Find eligible=PENDING/PENDING_REINSTATEMENT with enrollmentDueDate not null.
     */
    @Scheduled(cron = "0 0 7 ? * MON")
    @Transactional(readOnly = true)
    public void sendPendingProvidersToLaborOrg() {
        log.info("Labor Org Pending Providers batch job started");

        List<ProviderEntity> pendingProviders = providerRepository.findByEligible("PENDING");
        List<ProviderEntity> pendingReinstatement = providerRepository.findByEligible("PENDING_REINSTATEMENT");

        int count = 0;
        for (ProviderEntity provider : pendingProviders) {
            if (provider.getEnrollmentDueDate() != null) {
                log.info("STUB: Sending pending provider {} to labor org",
                        provider.getProviderNumber());
                count++;
            }
        }
        for (ProviderEntity provider : pendingReinstatement) {
            if (provider.getEnrollmentDueDate() != null) {
                log.info("STUB: Sending pending reinstatement provider {} to labor org",
                        provider.getProviderNumber());
                count++;
            }
        }

        log.info("Labor Org Pending Providers batch job completed. {} providers sent.", count);
    }

    /**
     * OT Violation 90-Day Reinstatement Check - Daily (at 6:30 AM)
     * PVM-32: Find providers whose 90-day OT ineligibility period has expired.
     * Violation #3 = 90 days, Violation #4 = 365 days.
     */
    @Scheduled(cron = "0 30 6 * * ?")
    @Transactional
    public void checkOtViolationReinstatementEligibility() {
        log.info("OT Violation Reinstatement Check batch job started");

        // Find violation #3 providers whose 90-day period has passed
        LocalDate ninetyDaysCutoff = LocalDate.now().minusDays(90);
        List<OvertimeViolationEntity> readyForReinstatement =
                overtimeViolationRepository.findProvidersReadyForReinstatement(ninetyDaysCutoff);

        int notifiedCount = 0;
        for (OvertimeViolationEntity violation : readyForReinstatement) {
            if (violation.getReinstatementDate() != null &&
                    !violation.getReinstatementDate().isAfter(LocalDate.now())) {
                ProviderEntity provider = providerRepository.findById(violation.getProviderId()).orElse(null);
                if (provider != null && "NO".equals(provider.getEligible())) {
                    // PVM-32: Send 90-day period expired notification
                    providerNotificationService.notifyOT90DaysPassed(provider);
                    notifiedCount++;
                    log.info("PVM-32: OT 90-day reinstatement eligible for provider {}",
                            provider.getProviderNumber());
                }
            }
        }

        log.info("OT Violation Reinstatement Check completed. {} providers notified.", notifiedCount);
    }

    /**
     * Provider End Date Check - Daily (at 7 AM)
     * PVM-07: Find active provider assignments with end dates within 30 days.
     */
    @Scheduled(cron = "0 0 7 * * ?")
    @Transactional(readOnly = true)
    public void checkProviderEndDates() {
        log.info("Provider End Date Check batch job started");

        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        List<ProviderAssignmentEntity> assignments = providerAssignmentRepository
                .findByStatusAndEndDateBefore(AssignmentStatus.ACTIVE, thirtyDaysFromNow);

        int notifiedCount = 0;
        for (ProviderAssignmentEntity assignment : assignments) {
            if (assignment.getEndDate() != null &&
                    assignment.getEndDate().isAfter(LocalDate.now()) &&
                    !assignment.getEndDate().isAfter(thirtyDaysFromNow)) {
                providerNotificationService.notifyProviderEndDateWithin30Days(assignment);
                notifiedCount++;
            }
        }

        log.info("Provider End Date Check completed. {} notifications sent.", notifiedCount);
    }

    /**
     * SSN Verification Response Processing - Twice weekly (Wednesday and Friday at 3 AM)
     * Process SSA verification responses and update provider status.
     * DSD Interface: CMRS701E (outbound) / CMRR701D (inbound)
     * SSA Response Codes:
     *   1 = SSN verified (name, DOB, gender match)
     *   2 = SSN verified, name mismatch on DOB or gender
     *   3 = SSN verified, name match, DOB/gender mismatch
     *   4 = SSN not in SSA file
     *   5 = Name/DOB/gender mismatch
     *   6 = SSN belongs to deceased individual
     */
    @Scheduled(cron = "0 0 3 ? * WED,FRI")
    @Transactional
    public void processSsnVerificationResponses() {
        log.info("SSN Verification Response Processing batch job started");

        // Stub: In production, would read inbound file CMRR701D from SSA
        // For each response record, map SSA response code to internal status:
        //   Code 1 → VERIFIED
        //   Code 2 → NAME_MATCH_DOB_GENDER_MISMATCH
        //   Code 3 → NAME_MATCH_DOB_MISMATCH
        //   Code 4 → SSN_NOT_IN_FILE
        //   Code 5 → NAME_DOB_GENDER_MISMATCH
        //   Code 6 → DECEASED (creates Task to case owner)

        List<ProviderEntity> pendingProviders = providerRepository.findProvidersWithPendingSsnVerification();
        log.info("STUB: {} providers awaiting SSN verification response (Interface CMRR701D)", pendingProviders.size());

        // Stub processing - in production would match against inbound file
        for (ProviderEntity provider : pendingProviders) {
            log.info("STUB: Awaiting SSA response for provider {} (Interface CMRR701D)",
                    provider.getProviderNumber());
        }

        log.info("SSN Verification Response Processing completed");
    }

    // ==================== HELPER METHODS ====================

    private String maskSsn(String ssn) {
        if (ssn == null || ssn.length() < 4) return "***";
        return "***-**-" + ssn.substring(ssn.length() - 4);
    }
}
