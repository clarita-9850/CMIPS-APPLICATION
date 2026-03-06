package com.cmips.batch.jobs;

import com.cmips.batch.JobExecutionNotificationListener;
import com.cmips.batch.StepProgressListener;
import com.cmips.entity.ProviderEntity;
import com.cmips.repository.ProviderRepository;
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
 * Spring Batch Job — SSN Verification (CMRS701E send / CMRR701D receive)
 *
 * DSD Section 23: Weekly CMIPS ↔ SSA verification interface.
 * - CMRS701E: Send provider SSNs to SSA for verification
 * - CMRR701D: Receive verification results from SSA
 *
 * Scheduled: Weekly (Monday midnight) via @Scheduled cron in scheduler module.
 * This job queries all providers with PENDING or null SSN verification status,
 * simulates the SSA exchange (mock), and updates their verification status.
 *
 * Statuses: VERIFIED, SUSPENDED_INELIGIBLE, NAME_MISMATCH, NOT_FOUND, PENDING
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SsnVerificationBatchJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final ProviderRepository providerRepository;
    private final ProviderManagementService providerManagementService;

    @Bean(name = "ssnVerificationJob")
    public Job ssnVerificationJob() {
        return new JobBuilder("SSN_VERIFICATION_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(ssnSendStep())
                .next(ssnReceiveStep())
                .build();
    }

    /**
     * Step 1 — CMRS701E: Identify providers needing SSN verification and mark as PENDING.
     * In production this would generate the SSA send file via Integration Hub.
     */
    @Bean
    public Step ssnSendStep() {
        return new StepBuilder("SSN_SEND_STEP", jobRepository)
                .tasklet(ssnSendTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet ssnSendTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[SSN_VERIFICATION] Step 1 — CMRS701E: Scanning providers for SSN verification...");
            AtomicInteger count = new AtomicInteger(0);

            // Find providers with no SSN verification or with PENDING status
            List<ProviderEntity> pendingProviders = providerRepository.findAll().stream()
                    .filter(p -> p.getSsnVerificationStatus() == null
                            || "PENDING".equals(p.getSsnVerificationStatus())
                            || "NOT_VERIFIED".equals(p.getSsnVerificationStatus()))
                    .toList();

            pendingProviders.forEach(p -> {
                p.setSsnVerificationStatus("PENDING");
                providerRepository.save(p);
                count.incrementAndGet();
                log.debug("[SSN_VERIFICATION] Queued provider {} (SSN: ****) for SSA verification", p.getId());
            });

            log.info("[SSN_VERIFICATION] CMRS701E: {} providers queued for SSN verification send file", count.get());
            // In production: call Integration Hub SFTP to send file to SSA
            return RepeatStatus.FINISHED;
        };
    }

    /**
     * Step 2 — CMRR701D: Receive and process SSA verification responses (mock).
     * In production this would read the SSA response file via Integration Hub.
     */
    @Bean
    public Step ssnReceiveStep() {
        return new StepBuilder("SSN_RECEIVE_STEP", jobRepository)
                .tasklet(ssnReceiveTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet ssnReceiveTasklet() {
        return (contribution, chunkContext) -> {
            log.info("[SSN_VERIFICATION] Step 2 — CMRR701D: Processing SSA verification responses...");
            AtomicInteger verified = new AtomicInteger(0);
            AtomicInteger failed = new AtomicInteger(0);

            List<ProviderEntity> pendingProviders = providerRepository.findAll().stream()
                    .filter(p -> "PENDING".equals(p.getSsnVerificationStatus()))
                    .toList();

            pendingProviders.forEach(p -> {
                // Mock SSA response: providers with SSN set are VERIFIED; others NOT_FOUND
                String result = (p.getSsn() != null && p.getSsn().length() >= 9)
                        ? "VERIFIED"
                        : "NOT_FOUND";

                try {
                    providerManagementService.updateSsnVerificationResult(p.getId(), result, "SSN_BATCH");
                    if ("VERIFIED".equals(result)) {
                        verified.incrementAndGet();
                    } else {
                        failed.incrementAndGet();
                    }
                } catch (Exception e) {
                    log.error("[SSN_VERIFICATION] Error processing SSA result for provider {}: {}", p.getId(), e.getMessage());
                }
            });

            log.info("[SSN_VERIFICATION] CMRR701D complete: {} verified, {} failed", verified.get(), failed.get());
            return RepeatStatus.FINISHED;
        };
    }
}
