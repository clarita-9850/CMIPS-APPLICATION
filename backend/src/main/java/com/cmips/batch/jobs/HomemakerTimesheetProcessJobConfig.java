package com.cmips.batch.jobs;

import com.cmips.batch.JobExecutionNotificationListener;
import com.cmips.batch.StepProgressListener;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Spring Batch Job Configuration for HOMEMAKER_TIMESHEET_PROCESS_JOB.
 *
 * DSD Section 25: Runs nightly to process county homemaker timesheets.
 * Validates submitted hours against authorized hours and posts to cases.
 *
 * Job Flow:
 * ┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
 * │ Find Submitted│───▶│  Validate   │───▶│ Post Hours   │───▶│   Summary    │
 * │  Homemaker   │    │ Hours vs.    │    │  to Case     │    │    Step      │
 * │  Timesheets  │    │ Authorized   │    │   (mock)     │    │              │
 * └──────────────┘    └──────────────┘    └──────────────┘    └──────────────┘
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class HomemakerTimesheetProcessJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;

    /**
     * Define the HOMEMAKER_TIMESHEET_PROCESS_JOB.
     * The bean name "homemakerTimesheetProcessJob" is used by the trigger service.
     */
    @Bean
    public Job homemakerTimesheetProcessJob() {
        return new JobBuilder("HOMEMAKER_TIMESHEET_PROCESS_JOB", jobRepository)
            .incrementer(new RunIdIncrementer())
            .listener(jobListener)
            .start(findSubmittedHomemakerTimesheetsStep())
            .next(validateHoursAgainstAuthorizedStep())
            .next(postHoursToCaseStep())
            .next(homemakerTimesheetSummaryStep())
            .build();
    }

    // ── Step 1: Find submitted homemaker timesheets ────────────────────────

    @Bean
    public Step findSubmittedHomemakerTimesheetsStep() {
        return new StepBuilder("findSubmittedHomemakerTimesheetsStep", jobRepository)
            .listener(stepListener)
            .tasklet(findSubmittedHomemakerTimesheetsTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet findSubmittedHomemakerTimesheetsTasklet() {
        return (contribution, chunkContext) -> {
            log.info("Finding submitted homemaker timesheets on thread: {}", Thread.currentThread());
            log.info("Is Virtual Thread: {}", Thread.currentThread().isVirtual());

            var execContext = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

            execContext.put("totalSteps", 4);
            execContext.putString("runDate",
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            // Mock: query for homemaker timesheets with status = SUBMITTED
            // In production: SELECT * FROM homemaker_timesheets WHERE status = 'SUBMITTED' AND submit_date = CURRENT_DATE
            int submittedTimesheets = 42;

            // Mock: break down by county
            int laCounty = 15;
            int sfCounty = 8;
            int sdCounty = 7;
            int otherCounties = submittedTimesheets - laCounty - sfCounty - sdCounty;

            execContext.put("submittedTimesheets", submittedTimesheets);
            execContext.put("laCountyTimesheets", laCounty);
            execContext.put("sfCountyTimesheets", sfCounty);
            execContext.put("sdCountyTimesheets", sdCounty);
            execContext.put("otherCountyTimesheets", otherCounties);

            log.info("Found {} submitted homemaker timesheets (LA: {}, SF: {}, SD: {}, Other: {})",
                submittedTimesheets, laCounty, sfCounty, sdCounty, otherCounties);

            return RepeatStatus.FINISHED;
        };
    }

    // ── Step 2: Validate hours against authorized ──────────────────────────

    @Bean
    public Step validateHoursAgainstAuthorizedStep() {
        return new StepBuilder("validateHoursAgainstAuthorizedStep", jobRepository)
            .listener(stepListener)
            .tasklet(validateHoursAgainstAuthorizedTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet validateHoursAgainstAuthorizedTasklet() {
        return (contribution, chunkContext) -> {
            log.info("Validating homemaker hours against authorized hours");

            var execContext = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

            Integer submittedTimesheets = (Integer) execContext.get("submittedTimesheets");

            AtomicInteger validTimesheets = new AtomicInteger(0);
            AtomicInteger overHoursTimesheets = new AtomicInteger(0);
            AtomicInteger invalidTimesheets = new AtomicInteger(0);

            for (int i = 0; i < submittedTimesheets; i++) {
                // Mock: each timesheet has submitted hours and authorized hours
                double submittedHours = 20.0 + (i % 30) * 1.5;  // Range: 20-63.5 hours
                double authorizedHours = 40.0 + (i % 10) * 2.0;  // Range: 40-58 hours

                if (submittedHours <= authorizedHours) {
                    validTimesheets.incrementAndGet();
                } else if (submittedHours <= authorizedHours * 1.1) {
                    // Within 10% tolerance - flag but allow
                    overHoursTimesheets.incrementAndGet();
                    log.debug("Timesheet {} over authorized by {}% - flagged for review",
                        i + 1, String.format("%.1f", ((submittedHours - authorizedHours) / authorizedHours) * 100));
                } else {
                    // More than 10% over - reject
                    invalidTimesheets.incrementAndGet();
                    log.debug("Timesheet {} rejected: submitted {}h exceeds authorized {}h by >{}%",
                        i + 1, String.format("%.1f", submittedHours), String.format("%.1f", authorizedHours), 10);
                }
            }

            execContext.put("validTimesheets", validTimesheets.get());
            execContext.put("overHoursTimesheets", overHoursTimesheets.get());
            execContext.put("invalidTimesheets", invalidTimesheets.get());

            log.info("Validation complete. Valid: {}, Over-hours (flagged): {}, Invalid (rejected): {}",
                validTimesheets.get(), overHoursTimesheets.get(), invalidTimesheets.get());

            return RepeatStatus.FINISHED;
        };
    }

    // ── Step 3: Post hours to case ─────────────────────────────────────────

    @Bean
    public Step postHoursToCaseStep() {
        return new StepBuilder("postHoursToCaseStep", jobRepository)
            .listener(stepListener)
            .tasklet(postHoursToCaseTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet postHoursToCaseTasklet() {
        return (contribution, chunkContext) -> {
            log.info("Posting validated homemaker hours to cases");

            var execContext = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

            Integer validTimesheets = (Integer) execContext.get("validTimesheets");
            Integer overHoursTimesheets = (Integer) execContext.get("overHoursTimesheets");
            int toProcess = validTimesheets + overHoursTimesheets; // Both valid and flagged get posted

            AtomicInteger postedSuccessfully = new AtomicInteger(0);
            AtomicInteger postErrors = new AtomicInteger(0);

            for (int i = 0; i < toProcess; i++) {
                // Mock: post hours to case management system
                // In production: UPDATE case_hours SET homemaker_hours = ?, status = 'POSTED' WHERE case_id = ?
                boolean success = (i % 25 != 0); // Mock: occasional posting error
                if (success) {
                    postedSuccessfully.incrementAndGet();
                } else {
                    postErrors.incrementAndGet();
                }

                if ((i + 1) % 10 == 0) {
                    log.debug("Posted {} of {} timesheets to cases", i + 1, toProcess);
                }
            }

            execContext.put("postedSuccessfully", postedSuccessfully.get());
            execContext.put("postErrors", postErrors.get());

            log.info("Hours posted to cases. Successful: {}, Errors: {}",
                postedSuccessfully.get(), postErrors.get());

            return RepeatStatus.FINISHED;
        };
    }

    // ── Step 4: Summary ────────────────────────────────────────────────────

    @Bean
    public Step homemakerTimesheetSummaryStep() {
        return new StepBuilder("homemakerTimesheetSummaryStep", jobRepository)
            .listener(stepListener)
            .tasklet(homemakerTimesheetSummaryTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Tasklet homemakerTimesheetSummaryTasklet() {
        return (contribution, chunkContext) -> {
            var execContext = chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext();

            String runDate = execContext.getString("runDate");
            Integer submittedTimesheets = (Integer) execContext.get("submittedTimesheets");
            Integer validTimesheets = (Integer) execContext.get("validTimesheets");
            Integer overHoursTimesheets = (Integer) execContext.get("overHoursTimesheets");
            Integer invalidTimesheets = (Integer) execContext.get("invalidTimesheets");
            Integer postedSuccessfully = (Integer) execContext.get("postedSuccessfully");
            Integer postErrors = (Integer) execContext.get("postErrors");

            log.info("=================================================");
            log.info("  HOMEMAKER_TIMESHEET_PROCESS_JOB COMPLETED");
            log.info("  Run Date: {}", runDate);
            log.info("  Submitted Timesheets: {}", submittedTimesheets);
            log.info("  Valid (within authorized): {}", validTimesheets);
            log.info("  Over-hours (flagged for review): {}", overHoursTimesheets);
            log.info("  Invalid (rejected): {}", invalidTimesheets);
            log.info("  Posted to Cases: {}", postedSuccessfully);
            log.info("  Post Errors: {}", postErrors);
            log.info("  Executed on Virtual Thread: {}", Thread.currentThread().isVirtual());
            log.info("=================================================");

            return RepeatStatus.FINISHED;
        };
    }
}
