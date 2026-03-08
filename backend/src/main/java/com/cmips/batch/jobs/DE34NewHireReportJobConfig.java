package com.cmips.batch.jobs;

import com.cmips.batch.JobExecutionNotificationListener;
import com.cmips.batch.StepProgressListener;
import com.cmips.entity.ProviderEntity;
import com.cmips.repository.ProviderRepository;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spring Batch Job Configuration for DE-34 New Hire Report.
 *
 * DSD Section 18: Bi-weekly report of newly hired/rehired IHSS providers to EDD.
 * California employers must report new hires within 20 days of start date using
 * EDD Form DE-34 (Report of New Employee(s)).
 *
 * Data Flow:
 * ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
 * │  Our Database   │───▶│  This Job       │───▶│  Integration    │───▶│      EDD        │
 * │  (Providers     │    │  (Extract &     │    │  Hub (Format &  │    │  (Process       │
 * │   New Hires)    │    │   Generate)     │    │   Send SFTP)    │    │   New Hires)    │
 * └─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘
 *
 * Job Flow:
 * ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
 * │   Query New     │───▶│   Generate      │───▶│   Send to EDD   │───▶│   Log           │
 * │   Hires Since   │    │   DE-34 File    │    │   via SFTP      │    │   Summary       │
 * │   Last Run      │    │   (mock BAW)    │    │   (mock)        │    │                 │
 * └─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘
 *
 * Schedule: Bi-weekly (replaces AutoSys schedule)
 * Legacy Reference: EDD DE-34 New Hire Reporting
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DE34NewHireReportJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final ProviderRepository providerRepository;

    private static final String DESTINATION_SYSTEM = "EDD";
    private static final String FILE_TYPE = "DE34_NEW_HIRE";
    // California SEIN for IHSS (mock)
    private static final String EMPLOYER_SEIN = "999-9999-9";
    private static final String EMPLOYER_FEIN = "94-6000134";

    // ==========================================
    // JOB DEFINITION
    // ==========================================

    @Bean
    public Job de34NewHireReportJob() {
        return new JobBuilder("DE34_NEW_HIRE_REPORT_JOB", jobRepository)
            .incrementer(new RunIdIncrementer())
            .listener(jobListener)
            .start(queryNewHiresStep())
            .next(generateDE34FileStep())
            .next(sendToEddStep())
            .next(de34LogSummaryStep())
            .build();
    }

    // ==========================================
    // STEP 1: Query New Hires Since Last Run
    // ==========================================

    @Bean
    public Step queryNewHiresStep() {
        return new StepBuilder("queryNewHiresStep", jobRepository)
            .listener(stepListener)
            .tasklet(queryNewHiresTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet queryNewHiresTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 1: Querying New Hires Since Last Run ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            executionContext.put("totalSteps", 4);

            // Look back 14 days for bi-weekly run (covers any missed runs)
            LocalDate cutoffDate = LocalDate.now().minusDays(14);

            // Query providers whose originalHireDate is after the cutoff
            // and who have ACTIVE status (newly enrolled providers)
            List<ProviderEntity> allProviders = providerRepository.findAll();
            List<Long> newHireIds = new ArrayList<>();

            AtomicInteger newHireCount = new AtomicInteger(0);
            AtomicInteger rehireCount = new AtomicInteger(0);

            for (ProviderEntity provider : allProviders) {
                if (provider.getOriginalHireDate() != null
                        && !provider.getOriginalHireDate().isBefore(cutoffDate)
                        && provider.getProviderStatus() == ProviderEntity.ProviderStatus.ACTIVE) {
                    newHireIds.add(provider.getId());
                    newHireCount.incrementAndGet();
                }
                // Rehires: providers reactivated within the window
                // (effectiveDate within window and they had a prior termination)
                if (provider.getEffectiveDate() != null
                        && !provider.getEffectiveDate().isBefore(cutoffDate)
                        && provider.getLeaveTerminationEffectiveDate() != null
                        && provider.getProviderStatus() == ProviderEntity.ProviderStatus.ACTIVE) {
                    if (!newHireIds.contains(provider.getId())) {
                        newHireIds.add(provider.getId());
                        rehireCount.incrementAndGet();
                    }
                }
            }

            if (newHireIds.isEmpty()) {
                log.info("No new hires or rehires found since {}", cutoffDate);
                executionContext.put("skipProcessing", true);
                executionContext.put("skipReason", "No new hires or rehires since " + cutoffDate);
                return RepeatStatus.FINISHED;
            }

            log.info("Found {} new hires and {} rehires since {}",
                newHireCount.get(), rehireCount.get(), cutoffDate);

            executionContext.put("skipProcessing", false);
            executionContext.put("newHireIds", new ArrayList<>(newHireIds));
            executionContext.put("newHireCount", newHireCount.get());
            executionContext.put("rehireCount", rehireCount.get());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 2: Generate DE-34 File
    // ==========================================

    @Bean
    public Step generateDE34FileStep() {
        return new StepBuilder("generateDE34FileStep", jobRepository)
            .listener(stepListener)
            .tasklet(generateDE34FileTasklet(), transactionManager)
            .build();
    }

    @Bean
    @SuppressWarnings("unchecked")
    public Tasklet generateDE34FileTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 2: Generating DE-34 File ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                log.info("Skipping DE-34 generation - no new hires to report");
                return RepeatStatus.FINISHED;
            }

            List<Long> newHireIds = (List<Long>) executionContext.get("newHireIds");
            AtomicInteger recordsGenerated = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);

            StringBuilder de34Content = new StringBuilder();
            // DE-34 header: SEIN, FEIN, employer name, report date
            de34Content.append(String.format("HDR|%s|%s|IHSS PROGRAM|%s%n",
                EMPLOYER_SEIN, EMPLOYER_FEIN,
                LocalDate.now().format(DateTimeFormatter.ofPattern("MMddyyyy"))));

            for (Long providerId : newHireIds) {
                try {
                    ProviderEntity provider = providerRepository.findById(providerId).orElse(null);
                    if (provider == null) continue;

                    // In production: would use Integration Hub to format as EDD DE-34 fixed-width
                    // DE-34 record: SSN, last name, first name, address, hire date, DOB
                    String ssn = provider.getSsn() != null ? provider.getSsn().replace("-", "") : "000000000";
                    String hireDate = provider.getOriginalHireDate() != null
                        ? provider.getOriginalHireDate().format(DateTimeFormatter.ofPattern("MMddyyyy"))
                        : "";

                    de34Content.append(String.format("DTL|%s|%s|%s|%s|%s|%s|%s|%s%n",
                        ssn,
                        provider.getLastName(),
                        provider.getFirstName(),
                        provider.getStreetAddress() != null ? provider.getStreetAddress() : "",
                        provider.getCity() != null ? provider.getCity() : "",
                        provider.getState() != null ? provider.getState() : "CA",
                        provider.getZipCode() != null ? provider.getZipCode() : "",
                        hireDate));

                    recordsGenerated.incrementAndGet();
                } catch (Exception e) {
                    log.error("Error generating DE-34 record for provider {}: {}", providerId, e.getMessage());
                    errorCount.incrementAndGet();
                }
            }

            // DE-34 trailer
            de34Content.append(String.format("TRL|%d%n", recordsGenerated.get()));

            log.info("Generated DE-34 file with {} records ({} errors)",
                recordsGenerated.get(), errorCount.get());

            // In production: would write to Integration Hub staging area
            String fileReference = "DE34-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + System.currentTimeMillis();
            executionContext.put("fileReference", fileReference);
            executionContext.put("recordsGenerated", recordsGenerated.get());
            executionContext.put("generateErrorCount", errorCount.get());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 3: Send to EDD via SFTP (Mock)
    // ==========================================

    @Bean
    public Step sendToEddStep() {
        return new StepBuilder("sendToEddStep", jobRepository)
            .listener(stepListener)
            .tasklet(sendToEddTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet sendToEddTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 3: Sending DE-34 to EDD via SFTP ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                log.info("Skipping send - no file to transmit");
                return RepeatStatus.FINISHED;
            }

            String fileReference = (String) executionContext.get("fileReference");
            int recordsGenerated = (int) executionContext.get("recordsGenerated");

            // In production: would use Integration Hub SFTP to send to EDD
            // bawFileService.sendOutboundFile("EDD", "DE34_NEW_HIRE", records);
            log.info("MOCK: Sending DE-34 file {} with {} records to EDD SFTP endpoint",
                fileReference, recordsGenerated);
            log.info("MOCK: EDD SFTP transfer complete");

            executionContext.put("sentToEdd", true);

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 4: Log Summary
    // ==========================================

    @Bean
    public Step de34LogSummaryStep() {
        return new StepBuilder("de34LogSummaryStep", jobRepository)
            .listener(stepListener)
            .tasklet(de34LogSummaryTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet de34LogSummaryTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 4: DE-34 Job Summary ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                String skipReason = (String) executionContext.get("skipReason");
                log.info("================================================");
                log.info("  DE34_NEW_HIRE_REPORT JOB SKIPPED");
                log.info("  Reason: {}", skipReason);
                log.info("================================================");
                return RepeatStatus.FINISHED;
            }

            String fileReference = (String) executionContext.get("fileReference");
            int newHireCount = (int) executionContext.get("newHireCount");
            int rehireCount = (int) executionContext.get("rehireCount");
            int recordsGenerated = (int) executionContext.get("recordsGenerated");
            int generateErrorCount = (int) executionContext.get("generateErrorCount");

            log.info("================================================");
            log.info("  DE34_NEW_HIRE_REPORT JOB COMPLETED");
            log.info("================================================");
            log.info("  File Reference: {}", fileReference);
            log.info("  Destination: {} ({})", DESTINATION_SYSTEM, FILE_TYPE);
            log.info("  Employer SEIN: {}", EMPLOYER_SEIN);
            log.info("------------------------------------------------");
            log.info("  Statistics:");
            log.info("    New Hires Found: {}", newHireCount);
            log.info("    Rehires Found: {}", rehireCount);
            log.info("    DE-34 Records Generated: {}", recordsGenerated);
            log.info("    Generation Errors: {}", generateErrorCount);
            log.info("================================================");

            return RepeatStatus.FINISHED;
        };
    }
}
