package com.cmips.batch.jobs;

import com.cmips.batch.JobExecutionNotificationListener;
import com.cmips.batch.StepProgressListener;
import com.cmips.entity.ProviderEntity;
import com.cmips.entity.SickLeaveClaimEntity;
import com.cmips.repository.ProviderRepository;
import com.cmips.repository.SickLeaveClaimRepository;
import com.cmips.service.ProviderManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spring Batch Job — Sick Leave Accrual (DSD Section 23 / SB-3 / CI-117630)
 *
 * California SB-3 sick leave accrual rules:
 * - FY 7/1/2018–6/30/2020: 8 hours per fiscal year
 * - FY 7/1/2020–6/30/2022: 16 hours per fiscal year
 * - FY 7/1/2022+:           24 hours per fiscal year
 *
 * Eligibility requirements (DSD SB-3):
 * - Provider must be ACTIVE
 * - Must have worked at least 100 total hours in the FY (totalServiceHoursWorked >= 100)
 * - IHSS providers: effective 7/1/2018; enrollment date must be on or before FY start
 *
 * Scheduled: Daily at midnight via @Scheduled cron.
 * On July 1  each year : credit new FY accrual to all eligible providers.
 * On June 30 each year : zero out unused hours (no-carryover rule).
 * On July 31 each year : final claim submission window closes — forfeit all unissued ACTIVE
 *                        claims entered before July 1 (prior FY claims not yet paid).
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SickLeaveAccrualBatchJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final ProviderRepository providerRepository;
    private final SickLeaveClaimRepository sickLeaveClaimRepository;
    private final ProviderManagementService providerManagementService;

    /** FY accrual hours by fiscal year start — per SB-3 schedule */
    private static double getAccrualHoursForFiscalYear(int fiscalYearStart) {
        if (fiscalYearStart >= 2022) return 24.0;
        if (fiscalYearStart >= 2020) return 16.0;
        if (fiscalYearStart >= 2018) return 8.0;
        return 0.0; // Prior to SB-3 effective date
    }

    @Bean(name = "sickLeaveAccrualJob")
    public Job sickLeaveAccrualJob() {
        return new JobBuilder("SICK_LEAVE_ACCRUAL_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(sickLeaveAccrualStep())
                .next(sickLeaveCarryoverResetStep())
                .next(sickLeaveJuly31ForfeitureStep())
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // Step 1 — July 1: Credit new FY accrual hours
    // ─────────────────────────────────────────────────────────────

    @Bean
    public Step sickLeaveAccrualStep() {
        return new StepBuilder("SICK_LEAVE_ACCRUAL_STEP", jobRepository)
                .tasklet(accrualTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet accrualTasklet() {
        return (contribution, chunkContext) -> {
            LocalDate today = LocalDate.now();

            // Only run on July 1
            if (today.getMonthValue() != 7 || today.getDayOfMonth() != 1) {
                log.info("[SICK_LEAVE_ACCRUAL] Not July 1 — skipping FY accrual credit. Today: {}", today);
                return RepeatStatus.FINISHED;
            }

            int fiscalYearStart = today.getYear();
            double accrualHours = getAccrualHoursForFiscalYear(fiscalYearStart);

            log.info("[SICK_LEAVE_ACCRUAL] FY {} starting — crediting {} hours to eligible providers",
                    fiscalYearStart, accrualHours);

            if (accrualHours <= 0) {
                log.info("[SICK_LEAVE_ACCRUAL] No accrual for FY {} (pre-SB3)", fiscalYearStart);
                return RepeatStatus.FINISHED;
            }

            AtomicInteger credited = new AtomicInteger(0);
            AtomicInteger skipped = new AtomicInteger(0);

            LocalDate fiscalStart = LocalDate.of(fiscalYearStart, 7, 1);

            List<ProviderEntity> activeProviders = providerRepository.findAll().stream()
                    .filter(p -> ProviderEntity.ProviderStatus.ACTIVE.equals(p.getProviderStatus()))
                    .toList();

            for (ProviderEntity provider : activeProviders) {
                try {
                    // Must have a valid enrollment effective date
                    if (provider.getEffectiveDate() == null) {
                        log.debug("[SICK_LEAVE_ACCRUAL] Provider {} skipped — no effective date", provider.getId());
                        skipped.incrementAndGet();
                        continue;
                    }

                    // Enrollment must be on or before this FY start (IHSS effective 7/1/2018)
                    if (provider.getEffectiveDate().isAfter(fiscalStart)) {
                        log.debug("[SICK_LEAVE_ACCRUAL] Provider {} skipped — enrolled after FY start ({})",
                                provider.getId(), provider.getEffectiveDate());
                        skipped.incrementAndGet();
                        continue;
                    }

                    // DSD SB-3: Must have worked at least 100 total hours in the fiscal year
                    Double hoursWorked = provider.getTotalServiceHoursWorked();
                    if (hoursWorked == null || hoursWorked < 100.0) {
                        log.debug("[SICK_LEAVE_ACCRUAL] Provider {} skipped — only {:.1f} hours worked (need 100)",
                                provider.getId(), hoursWorked != null ? hoursWorked : 0.0);
                        skipped.incrementAndGet();
                        continue;
                    }

                    // Credit accrual hours
                    double current = provider.getSickLeaveAccruedHours() != null
                            ? provider.getSickLeaveAccruedHours() : 0.0;
                    provider.setSickLeaveAccruedHours(current + accrualHours);
                    provider.setUpdatedBy("SICK_LEAVE_BATCH");
                    providerRepository.save(provider);
                    credited.incrementAndGet();

                    log.debug("[SICK_LEAVE_ACCRUAL] Provider {} credited {} hours (total accrued: {})",
                            provider.getId(), accrualHours, provider.getSickLeaveAccruedHours());

                } catch (Exception e) {
                    log.error("[SICK_LEAVE_ACCRUAL] Error processing provider {}: {}",
                            provider.getId(), e.getMessage());
                }
            }

            log.info("[SICK_LEAVE_ACCRUAL] FY {} accrual complete: {} credited, {} skipped",
                    fiscalYearStart, credited.get(), skipped.get());
            return RepeatStatus.FINISHED;
        };
    }

    // ─────────────────────────────────────────────────────────────
    // Step 2 — June 30: Zero unused hours (no-carryover rule)
    // ─────────────────────────────────────────────────────────────

    @Bean
    public Step sickLeaveCarryoverResetStep() {
        return new StepBuilder("SICK_LEAVE_CARRYOVER_RESET_STEP", jobRepository)
                .tasklet(carryoverResetTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet carryoverResetTasklet() {
        return (contribution, chunkContext) -> {
            LocalDate today = LocalDate.now();

            // Only run on June 30
            if (today.getMonthValue() != 6 || today.getDayOfMonth() != 30) {
                log.info("[SICK_LEAVE_CARRYOVER] Not June 30 — skipping carryover reset. Today: {}", today);
                return RepeatStatus.FINISHED;
            }

            log.info("[SICK_LEAVE_CARRYOVER] June 30 — zeroing unused sick leave hours (no-carryover rule)");
            AtomicInteger reset = new AtomicInteger(0);

            List<ProviderEntity> providers = providerRepository.findAll().stream()
                    .filter(p -> p.getSickLeaveAccruedHours() != null && p.getSickLeaveAccruedHours() > 0)
                    .toList();

            for (ProviderEntity provider : providers) {
                try {
                    log.debug("[SICK_LEAVE_CARRYOVER] Provider {} forfeiting {} unused hours",
                            provider.getId(), provider.getSickLeaveAccruedHours());
                    provider.setSickLeaveAccruedHours(0.0);
                    provider.setUpdatedBy("SICK_LEAVE_BATCH");
                    providerRepository.save(provider);
                    reset.incrementAndGet();
                } catch (Exception e) {
                    log.error("[SICK_LEAVE_CARRYOVER] Error resetting provider {}: {}",
                            provider.getId(), e.getMessage());
                }
            }

            log.info("[SICK_LEAVE_CARRYOVER] FY carryover reset complete: {} provider balances zeroed", reset.get());
            return RepeatStatus.FINISHED;
        };
    }

    // ─────────────────────────────────────────────────────────────
    // Step 3 — July 31: Forfeit unissued prior-FY claims
    // ─────────────────────────────────────────────────────────────

    /**
     * DSD SB-3: July 31 is the final deadline for providers to submit sick leave claims
     * for the fiscal year that ended June 30. Any ACTIVE claim entered before July 1 that
     * has not been issued (paid) by July 31 is forfeited and marked FORFEITED.
     */
    @Bean
    public Step sickLeaveJuly31ForfeitureStep() {
        return new StepBuilder("SICK_LEAVE_JULY31_FORFEITURE_STEP", jobRepository)
                .tasklet(july31ForfeitureTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet july31ForfeitureTasklet() {
        return (contribution, chunkContext) -> {
            LocalDate today = LocalDate.now();

            // Only run on July 31
            if (today.getMonthValue() != 7 || today.getDayOfMonth() != 31) {
                log.info("[SICK_LEAVE_FORFEITURE] Not July 31 — skipping claim forfeiture. Today: {}", today);
                return RepeatStatus.FINISHED;
            }

            // Claims entered before July 1 of this year belong to the prior fiscal year
            LocalDate priorFyEnd = LocalDate.of(today.getYear(), 7, 1);

            log.info("[SICK_LEAVE_FORFEITURE] July 31 — forfeiting unissued prior-FY claims (entered before {})",
                    priorFyEnd);

            List<SickLeaveClaimEntity> expiredClaims =
                    sickLeaveClaimRepository.findActiveUnissuedClaimsBefore(priorFyEnd);

            AtomicInteger forfeited = new AtomicInteger(0);

            for (SickLeaveClaimEntity claim : expiredClaims) {
                try {
                    log.debug("[SICK_LEAVE_FORFEITURE] Forfeiting claim {} for provider {} (entered {}, never issued)",
                            claim.getClaimNumber(), claim.getProviderId(), claim.getClaimEnteredDate());
                    claim.setStatus("FORFEITED");
                    claim.setUpdatedBy("SICK_LEAVE_BATCH");
                    sickLeaveClaimRepository.save(claim);
                    forfeited.incrementAndGet();
                } catch (Exception e) {
                    log.error("[SICK_LEAVE_FORFEITURE] Error forfeiting claim {}: {}",
                            claim.getClaimNumber(), e.getMessage());
                }
            }

            log.info("[SICK_LEAVE_FORFEITURE] July 31 forfeiture complete: {} claims forfeited", forfeited.get());
            return RepeatStatus.FINISHED;
        };
    }
}
