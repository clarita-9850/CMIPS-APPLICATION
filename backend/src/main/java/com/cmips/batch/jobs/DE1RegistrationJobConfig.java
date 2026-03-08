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
 * Spring Batch Job Configuration for DE-1 EDD Registration.
 *
 * DSD Section 18: Weekly new provider EDD registration and quarterly
 * response processing. Each county IHSS program must register as an employer
 * with EDD. New providers need SEIN (State Employer Identification Number)
 * assignment for tax reporting.
 *
 * Data Flow (Outbound - Registration Request):
 * ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
 * │  Our Database   │───▶│  This Job       │───▶│  Integration    │───▶│      EDD        │
 * │  (Providers     │    │  (Find unreg.   │    │  Hub (Format &  │    │  (Assign SEIN)  │
 * │   w/o SEIN)     │    │   & Generate)   │    │   Send SFTP)    │    │                 │
 * └─────────────────┘    └─────────────────┘    └─────────────────┘    └─────────────────┘
 *
 * Data Flow (Inbound - Registration Response):
 * ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
 * │      EDD        │───▶│  Integration    │───▶│  This Job       │
 * │  (SEIN assigned)│    │  Hub (Fetch)    │    │  (Update DB)    │
 * └─────────────────┘    └─────────────────┘    └─────────────────┘
 *
 * Schedule: Weekly outbound, quarterly inbound (replaces AutoSys schedule)
 * Legacy Reference: EDD DE-1 Registration
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DE1RegistrationJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final ProviderRepository providerRepository;

    private static final String DESTINATION_SYSTEM = "EDD";
    private static final String FILE_TYPE = "DE1_REGISTRATION";

    // ==========================================
    // JOB DEFINITION
    // ==========================================

    @Bean
    public Job de1RegistrationJob() {
        return new JobBuilder("DE1_REGISTRATION_JOB", jobRepository)
            .incrementer(new RunIdIncrementer())
            .listener(jobListener)
            .start(findUnregisteredProvidersStep())
            .next(generateDE1RequestStep())
            .next(processDE1ResponseStep())
            .next(de1LogSummaryStep())
            .build();
    }

    // ==========================================
    // STEP 1: Find Unregistered Providers (no EDD SEIN)
    // ==========================================

    @Bean
    public Step findUnregisteredProvidersStep() {
        return new StepBuilder("findUnregisteredProvidersStep", jobRepository)
            .listener(stepListener)
            .tasklet(findUnregisteredProvidersTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet findUnregisteredProvidersTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 1: Finding Unregistered Providers ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            executionContext.put("totalSteps", 4);

            // Find active providers who do not yet have an EDD SEIN
            // The taxpayerId field is used to store the SEIN once assigned
            List<ProviderEntity> allProviders = providerRepository.findAll();
            List<Long> unregisteredIds = new ArrayList<>();

            for (ProviderEntity provider : allProviders) {
                if (provider.getProviderStatus() == ProviderEntity.ProviderStatus.ACTIVE
                        && (provider.getTaxpayerId() == null || provider.getTaxpayerId().isBlank())
                        && provider.getSsn() != null && !provider.getSsn().isBlank()) {
                    unregisteredIds.add(provider.getId());
                }
            }

            if (unregisteredIds.isEmpty()) {
                log.info("No unregistered providers found requiring EDD SEIN");
                executionContext.put("skipProcessing", true);
                executionContext.put("skipReason", "All active providers already have EDD SEIN");
                return RepeatStatus.FINISHED;
            }

            log.info("Found {} active providers without EDD SEIN", unregisteredIds.size());

            executionContext.put("skipProcessing", false);
            executionContext.put("unregisteredIds", new ArrayList<>(unregisteredIds));
            executionContext.put("unregisteredCount", unregisteredIds.size());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 2: Generate DE-1 Request File
    // ==========================================

    @Bean
    public Step generateDE1RequestStep() {
        return new StepBuilder("generateDE1RequestStep", jobRepository)
            .listener(stepListener)
            .tasklet(generateDE1RequestTasklet(), transactionManager)
            .build();
    }

    @Bean
    @SuppressWarnings("unchecked")
    public Tasklet generateDE1RequestTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 2: Generating DE-1 Registration Request File ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                log.info("Skipping DE-1 generation - no unregistered providers");
                return RepeatStatus.FINISHED;
            }

            List<Long> unregisteredIds = (List<Long>) executionContext.get("unregisteredIds");
            AtomicInteger recordsGenerated = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);

            StringBuilder de1Content = new StringBuilder();
            // DE-1 header
            de1Content.append(String.format("HDR|DE1_REG|%s|IHSS_PROGRAM%n",
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))));

            for (Long providerId : unregisteredIds) {
                try {
                    ProviderEntity provider = providerRepository.findById(providerId).orElse(null);
                    if (provider == null) continue;

                    // In production: would use Integration Hub to format as EDD DE-1
                    // DE-1 requires: business name, FEIN, address, nature of business,
                    // date first wages paid, number of employees
                    String ssn = provider.getSsn() != null ? provider.getSsn().replace("-", "") : "";
                    String county = provider.getDojCountyCode() != null ? provider.getDojCountyCode() : "00";

                    de1Content.append(String.format("REG|%s|%s|%s|%s|%s|%s|%s%n",
                        provider.getProviderNumber(),
                        ssn,
                        provider.getLastName(),
                        provider.getFirstName(),
                        county,
                        provider.getOriginalHireDate() != null
                            ? provider.getOriginalHireDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                            : "",
                        "IHSS_PROVIDER"));

                    recordsGenerated.incrementAndGet();
                } catch (Exception e) {
                    log.error("Error generating DE-1 record for provider {}: {}", providerId, e.getMessage());
                    errorCount.incrementAndGet();
                }
            }

            de1Content.append(String.format("TRL|%d%n", recordsGenerated.get()));

            log.info("Generated DE-1 request file with {} records ({} errors)",
                recordsGenerated.get(), errorCount.get());

            // In production: would use Integration Hub SFTP to send to EDD
            // bawFileService.sendOutboundFile("EDD", "DE1_REGISTRATION", records);
            String fileReference = "DE1-REQ-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + System.currentTimeMillis();
            log.info("MOCK: Sending DE-1 request file {} to EDD SFTP", fileReference);

            executionContext.put("requestFileReference", fileReference);
            executionContext.put("requestRecordsGenerated", recordsGenerated.get());
            executionContext.put("requestErrorCount", errorCount.get());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 3: Process DE-1 Response File (Mock)
    // ==========================================

    @Bean
    public Step processDE1ResponseStep() {
        return new StepBuilder("processDE1ResponseStep", jobRepository)
            .listener(stepListener)
            .tasklet(processDE1ResponseTasklet(), transactionManager)
            .build();
    }

    @Bean
    @SuppressWarnings("unchecked")
    public Tasklet processDE1ResponseTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 3: Processing DE-1 Response File (Mock) ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                log.info("Skipping DE-1 response processing");
                return RepeatStatus.FINISHED;
            }

            // In production: would use Integration Hub to fetch inbound DE-1 response from EDD
            // Response contains assigned SEIN numbers for each registration request
            // bawFileService.fetchInboundFile("EDD", "DE1_RESPONSE", DE1Response.class);

            List<Long> unregisteredIds = (List<Long>) executionContext.get("unregisteredIds");
            AtomicInteger seinAssigned = new AtomicInteger(0);
            AtomicInteger seinPending = new AtomicInteger(0);

            // Mock: simulate response processing by assigning mock SEIN numbers
            // In production, this would parse the actual EDD response file
            for (Long providerId : unregisteredIds) {
                try {
                    ProviderEntity provider = providerRepository.findById(providerId).orElse(null);
                    if (provider == null) continue;

                    // Mock: assign a generated SEIN based on county + sequence
                    // In production: SEIN comes from EDD response file
                    String mockSein = String.format("%s-%07d",
                        provider.getDojCountyCode() != null ? provider.getDojCountyCode() : "99",
                        providerId);

                    // Simulate that ~80% get immediate SEIN assignment, rest are pending
                    if (providerId % 5 != 0) {
                        provider.setTaxpayerId(mockSein);
                        providerRepository.save(provider);
                        seinAssigned.incrementAndGet();
                        log.debug("Assigned SEIN {} to provider {}", mockSein, provider.getProviderNumber());
                    } else {
                        seinPending.incrementAndGet();
                        log.debug("SEIN pending for provider {} - EDD review required",
                            provider.getProviderNumber());
                    }
                } catch (Exception e) {
                    log.error("Error processing DE-1 response for provider {}: {}", providerId, e.getMessage());
                    seinPending.incrementAndGet();
                }
            }

            log.info("DE-1 response processing: {} SEIN assigned, {} pending",
                seinAssigned.get(), seinPending.get());

            executionContext.put("seinAssigned", seinAssigned.get());
            executionContext.put("seinPending", seinPending.get());

            return RepeatStatus.FINISHED;
        };
    }

    // ==========================================
    // STEP 4: Log Summary
    // ==========================================

    @Bean
    public Step de1LogSummaryStep() {
        return new StepBuilder("de1LogSummaryStep", jobRepository)
            .listener(stepListener)
            .tasklet(de1LogSummaryTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet de1LogSummaryTasklet() {
        return (contribution, chunkContext) -> {
            log.info("=== STEP 4: DE-1 Job Summary ===");

            var executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            Boolean skipProcessing = (Boolean) executionContext.get("skipProcessing");
            if (Boolean.TRUE.equals(skipProcessing)) {
                String skipReason = (String) executionContext.get("skipReason");
                log.info("================================================");
                log.info("  DE1_REGISTRATION JOB SKIPPED");
                log.info("  Reason: {}", skipReason);
                log.info("================================================");
                return RepeatStatus.FINISHED;
            }

            String requestFileReference = (String) executionContext.get("requestFileReference");
            int unregisteredCount = (int) executionContext.get("unregisteredCount");
            int requestRecordsGenerated = (int) executionContext.get("requestRecordsGenerated");
            int requestErrorCount = (int) executionContext.get("requestErrorCount");
            int seinAssigned = (int) executionContext.get("seinAssigned");
            int seinPending = (int) executionContext.get("seinPending");

            log.info("================================================");
            log.info("  DE1_REGISTRATION JOB COMPLETED");
            log.info("================================================");
            log.info("  Request File: {}", requestFileReference);
            log.info("  Destination: {} ({})", DESTINATION_SYSTEM, FILE_TYPE);
            log.info("------------------------------------------------");
            log.info("  Registration Request:");
            log.info("    Unregistered Providers Found: {}", unregisteredCount);
            log.info("    DE-1 Records Generated: {}", requestRecordsGenerated);
            log.info("    Generation Errors: {}", requestErrorCount);
            log.info("------------------------------------------------");
            log.info("  Response Processing:");
            log.info("    SEIN Numbers Assigned: {}", seinAssigned);
            log.info("    SEIN Pending (EDD Review): {}", seinPending);
            log.info("================================================");

            return RepeatStatus.FINISHED;
        };
    }
}
