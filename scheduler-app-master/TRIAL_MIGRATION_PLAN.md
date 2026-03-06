# CMIPS Application - Spring Batch Migration Plan

## Executive Summary

This document outlines the migration plan for converting the CMIPS application's custom batch processing system to Spring Batch framework, while integrating with the new Scheduler application.

**Current State:** Custom batch processing with Redis queues, manual job execution, and custom retry logic
**Target State:** Spring Batch framework with Scheduler integration via REST API and Redis pub/sub

---

## Architecture Overview

### Before Migration
```
┌─────────────────────────────────────────────────────────────────┐
│                      TRIAL APPLICATION                           │
│                                                                  │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────┐  │
│  │ @Scheduled  │───►│ JobQueue    │───►│ BackgroundProcessing│  │
│  │ (5s poll)   │    │ Service     │    │ Service             │  │
│  └─────────────┘    └─────────────┘    └─────────────────────┘  │
│         │                  │                      │              │
│         ▼                  ▼                      ▼              │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────────────┐  │
│  │ ReportJob   │    │ Redis       │    │ ReportGeneration    │  │
│  │ Repository  │    │ Queue       │    │ Service             │  │
│  └─────────────┘    └─────────────┘    └─────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### After Migration
```
┌─────────────────────┐         ┌─────────────────────────────────┐
│   SCHEDULER APP     │         │        TRIAL APPLICATION        │
│   (Port 8084)       │         │        (Port 8081)              │
│                     │         │                                  │
│  ┌───────────────┐  │  REST   │  ┌───────────────────────────┐  │
│  │ Quartz        │──┼────────►│  │ BatchTriggerController    │  │
│  │ CronEvaluator │  │ trigger │  │ POST /api/batch/trigger   │  │
│  └───────────────┘  │         │  └───────────────────────────┘  │
│         │           │         │              │                   │
│         ▼           │         │              ▼                   │
│  ┌───────────────┐  │         │  ┌───────────────────────────┐  │
│  │ Dependency    │  │         │  │ Spring Batch JobLauncher  │  │
│  │ Service       │  │         │  └───────────────────────────┘  │
│  └───────────────┘  │         │              │                   │
│         ▲           │         │              ▼                   │
│         │           │  Redis  │  ┌───────────────────────────┐  │
│  ┌───────────────┐  │  Events │  │ Spring Batch Jobs         │  │
│  │ Execution     │◄─┼─────────│  │ - CountyDailyReportJob    │  │
│  │ Service       │  │         │  │ - WeeklySyncJob           │  │
│  └───────────────┘  │         │  │ - MonthlyAggregationJob   │  │
│                     │         │  └───────────────────────────┘  │
└─────────────────────┘         └─────────────────────────────────┘
```

---

## Migration Phases

## Phase 1: Foundation Setup (No Breaking Changes)

### 1.1 Add Spring Batch Dependencies

**File:** `cmips-backend/pom.xml`

```xml
<!-- Add to dependencies section -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-batch</artifactId>
</dependency>
```

### 1.2 Configure Spring Batch

**File:** `cmips-backend/src/main/resources/application.yml`

```yaml
spring:
  batch:
    job:
      enabled: false  # Disable auto-run, we trigger manually
    jdbc:
      initialize-schema: always  # Create batch tables
      isolation-level-for-create: default

  # Existing datasource config will be used for batch tables
```

### 1.3 Create Spring Batch Configuration

**File:** `cmips-backend/src/main/java/com/.../config/SpringBatchConfig.java`

```java
@Configuration
@EnableBatchProcessing
public class SpringBatchConfig {

    @Bean
    public JobRepository jobRepository(DataSource dataSource,
                                       PlatformTransactionManager transactionManager) {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.setIsolationLevelForCreate("ISOLATION_SERIALIZABLE");
        factory.setTablePrefix("BATCH_");
        return factory.getObject();
    }

    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) {
        TaskExecutorJobLauncher launcher = new TaskExecutorJobLauncher();
        launcher.setJobRepository(jobRepository);
        launcher.setTaskExecutor(new SimpleAsyncTaskExecutor()); // Async execution
        return launcher;
    }

    @Bean
    public JobExplorer jobExplorer(DataSource dataSource) {
        JobExplorerFactoryBean factory = new JobExplorerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTablePrefix("BATCH_");
        return factory.getObject();
    }
}
```

### 1.4 Create Batch Trigger REST Controller

**File:** `cmips-backend/src/main/java/com/.../controller/BatchTriggerController.java`

```java
@RestController
@RequestMapping("/api/batch")
@Slf4j
public class BatchTriggerController {

    private final JobLauncher jobLauncher;
    private final Map<String, Job> jobRegistry;
    private final JobEventPublisher eventPublisher;

    @PostMapping("/trigger")
    public ResponseEntity<TriggerResponse> triggerJob(@RequestBody TriggerRequest request) {
        log.info("Received trigger request: jobName={}, triggerId={}",
                 request.getJobName(), request.getTriggerId());

        Job job = jobRegistry.get(request.getJobName());
        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            JobParameters params = buildJobParameters(request);
            JobExecution execution = jobLauncher.run(job, params);

            return ResponseEntity.ok(new TriggerResponse(
                request.getTriggerId(),
                execution.getId(),
                execution.getStatus().toString()
            ));
        } catch (Exception e) {
            log.error("Failed to trigger job", e);
            return ResponseEntity.status(500).body(
                new TriggerResponse(request.getTriggerId(), null, "FAILED")
            );
        }
    }

    @PostMapping("/stop/{triggerId}")
    public ResponseEntity<Void> stopJob(@PathVariable String triggerId) {
        // Implementation to stop running job
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status/{triggerId}")
    public ResponseEntity<StatusResponse> getStatus(@PathVariable String triggerId) {
        // Query Spring Batch for execution status
        return ResponseEntity.ok(/* status */);
    }

    private JobParameters buildJobParameters(TriggerRequest request) {
        JobParametersBuilder builder = new JobParametersBuilder()
            .addString("triggerId", request.getTriggerId())
            .addLong("timestamp", System.currentTimeMillis());

        if (request.getParameters() != null) {
            request.getParameters().forEach((k, v) ->
                builder.addString(k, String.valueOf(v)));
        }

        return builder.toJobParameters();
    }
}
```

### 1.5 Create Redis Event Publisher

**File:** `cmips-backend/src/main/java/com/.../service/JobEventPublisher.java`

```java
@Service
@Slf4j
public class JobEventPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${redis.channels.job-started:batch:events:job-started}")
    private String jobStartedChannel;

    @Value("${redis.channels.job-progress:batch:events:job-progress}")
    private String jobProgressChannel;

    @Value("${redis.channels.job-completed:batch:events:job-completed}")
    private String jobCompletedChannel;

    @Value("${redis.channels.job-failed:batch:events:job-failed}")
    private String jobFailedChannel;

    public void publishJobStarted(String triggerId, Long executionId, String jobName) {
        JobEvent event = JobEvent.builder()
            .eventType("JOB_STARTED")
            .triggerId(triggerId)
            .springBatchExecutionId(executionId)
            .jobName(jobName)
            .status("RUNNING")
            .timestamp(LocalDateTime.now())
            .build();

        redisTemplate.convertAndSend(jobStartedChannel, event);
        log.info("Published JOB_STARTED event for triggerId={}", triggerId);
    }

    public void publishJobProgress(String triggerId, int percentage, String message) {
        JobEvent event = JobEvent.builder()
            .eventType("JOB_PROGRESS")
            .triggerId(triggerId)
            .progressPercentage(percentage)
            .progressMessage(message)
            .timestamp(LocalDateTime.now())
            .build();

        redisTemplate.convertAndSend(jobProgressChannel, event);
    }

    public void publishJobCompleted(String triggerId, Long executionId) {
        JobEvent event = JobEvent.builder()
            .eventType("JOB_COMPLETED")
            .triggerId(triggerId)
            .springBatchExecutionId(executionId)
            .status("COMPLETED")
            .timestamp(LocalDateTime.now())
            .build();

        redisTemplate.convertAndSend(jobCompletedChannel, event);
        log.info("Published JOB_COMPLETED event for triggerId={}", triggerId);
    }

    public void publishJobFailed(String triggerId, Long executionId, String errorMessage) {
        JobEvent event = JobEvent.builder()
            .eventType("JOB_FAILED")
            .triggerId(triggerId)
            .springBatchExecutionId(executionId)
            .status("FAILED")
            .errorMessage(errorMessage)
            .timestamp(LocalDateTime.now())
            .build();

        redisTemplate.convertAndSend(jobFailedChannel, event);
        log.error("Published JOB_FAILED event for triggerId={}", triggerId);
    }
}
```

### 1.6 Create Job Execution Listener

**File:** `cmips-backend/src/main/java/com/.../batch/listener/JobExecutionNotificationListener.java`

```java
@Component
@Slf4j
public class JobExecutionNotificationListener implements JobExecutionListener {

    private final JobEventPublisher eventPublisher;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String triggerId = jobExecution.getJobParameters().getString("triggerId");
        String jobName = jobExecution.getJobInstance().getJobName();

        log.info("Job {} starting with triggerId={}", jobName, triggerId);

        eventPublisher.publishJobStarted(
            triggerId,
            jobExecution.getId(),
            jobName
        );
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String triggerId = jobExecution.getJobParameters().getString("triggerId");
        BatchStatus status = jobExecution.getStatus();

        if (status == BatchStatus.COMPLETED) {
            eventPublisher.publishJobCompleted(triggerId, jobExecution.getId());
        } else if (status == BatchStatus.FAILED) {
            String errorMsg = jobExecution.getAllFailureExceptions().stream()
                .map(Throwable::getMessage)
                .collect(Collectors.joining("; "));
            eventPublisher.publishJobFailed(triggerId, jobExecution.getId(), errorMsg);
        }
    }
}
```

---

## Phase 2: Create Spring Batch Jobs

### 2.1 Job Registry Service

**File:** `cmips-backend/src/main/java/com/.../batch/JobRegistryService.java`

```java
@Service
public class JobRegistryService {

    private final Map<String, Job> jobs = new ConcurrentHashMap<>();

    @Autowired
    public void registerJobs(List<Job> allJobs) {
        for (Job job : allJobs) {
            jobs.put(job.getName(), job);
            log.info("Registered Spring Batch job: {}", job.getName());
        }
    }

    public Job getJob(String jobName) {
        return jobs.get(jobName);
    }

    public Set<String> getRegisteredJobNames() {
        return jobs.keySet();
    }

    public boolean hasJob(String jobName) {
        return jobs.containsKey(jobName);
    }
}
```

### 2.2 Example: County Daily Report Job

**File:** `cmips-backend/src/main/java/com/.../batch/jobs/CountyDailyReportJobConfig.java`

```java
@Configuration
@RequiredArgsConstructor
public class CountyDailyReportJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final CountyReportItemReader itemReader;
    private final CountyReportItemProcessor itemProcessor;
    private final CountyReportItemWriter itemWriter;
    private final StepProgressListener stepProgressListener;

    @Bean
    public Job countyDailyReportJob() {
        return new JobBuilder("COUNTY_DAILY_REPORT", jobRepository)
            .listener(jobListener)
            .start(extractDataStep())
            .next(generateReportStep())
            .next(deliverReportStep())
            .build();
    }

    @Bean
    public Step extractDataStep() {
        return new StepBuilder("extractData", jobRepository)
            .<CountyRecord, CountyRecord>chunk(1000, transactionManager)
            .reader(itemReader)
            .processor(itemProcessor)
            .writer(itemWriter)
            .listener(stepProgressListener)
            .faultTolerant()
            .retryLimit(3)
            .retry(TransientDataAccessException.class)
            .skipLimit(100)
            .skip(DataValidationException.class)
            .build();
    }

    @Bean
    public Step generateReportStep() {
        return new StepBuilder("generateReport", jobRepository)
            .tasklet(reportGenerationTasklet(), transactionManager)
            .build();
    }

    @Bean
    public Step deliverReportStep() {
        return new StepBuilder("deliverReport", jobRepository)
            .tasklet(reportDeliveryTasklet(), transactionManager)
            .build();
    }
}
```

### 2.3 Item Reader Example

**File:** `cmips-backend/src/main/java/com/.../batch/reader/CountyReportItemReader.java`

```java
@Component
@StepScope
public class CountyReportItemReader implements ItemReader<CountyRecord> {

    private final TimesheetRepository timesheetRepository;
    private Iterator<CountyRecord> dataIterator;

    @Value("#{jobParameters['county']}")
    private String county;

    @Value("#{jobParameters['reportDate']}")
    private String reportDate;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        // Load data for this step
        LocalDate date = LocalDate.parse(reportDate);
        List<CountyRecord> records = timesheetRepository.findByCountyAndDate(county, date);
        this.dataIterator = records.iterator();

        // Store total count for progress tracking
        stepExecution.getExecutionContext().putInt("totalRecords", records.size());
    }

    @Override
    public CountyRecord read() {
        if (dataIterator != null && dataIterator.hasNext()) {
            return dataIterator.next();
        }
        return null; // Signals end of data
    }
}
```

### 2.4 Item Processor Example

**File:** `cmips-backend/src/main/java/com/.../batch/processor/CountyReportItemProcessor.java`

```java
@Component
@StepScope
public class CountyReportItemProcessor implements ItemProcessor<CountyRecord, ProcessedRecord> {

    @Value("#{jobParameters['reportFormat']}")
    private String reportFormat;

    @Override
    public ProcessedRecord process(CountyRecord item) throws Exception {
        // Transform/validate the record
        ProcessedRecord processed = new ProcessedRecord();
        processed.setCountyCode(item.getCountyCode());
        processed.setData(transformData(item));
        processed.setFormat(reportFormat);

        return processed;
    }

    private Map<String, Object> transformData(CountyRecord item) {
        // Business logic transformation
        return Map.of(
            "id", item.getId(),
            "amount", calculateAmount(item),
            "status", determineStatus(item)
        );
    }
}
```

### 2.5 Item Writer Example

**File:** `cmips-backend/src/main/java/com/.../batch/writer/CountyReportItemWriter.java`

```java
@Component
@StepScope
public class CountyReportItemWriter implements ItemWriter<ProcessedRecord> {

    private final ReportOutputRepository outputRepository;
    private final JobEventPublisher eventPublisher;

    @Value("#{jobParameters['triggerId']}")
    private String triggerId;

    private int processedCount = 0;
    private int totalRecords = 0;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.totalRecords = stepExecution.getExecutionContext().getInt("totalRecords", 0);
    }

    @Override
    public void write(Chunk<? extends ProcessedRecord> chunk) throws Exception {
        // Write chunk to output
        for (ProcessedRecord record : chunk) {
            outputRepository.save(record);
            processedCount++;
        }

        // Publish progress
        int percentage = totalRecords > 0 ? (processedCount * 100) / totalRecords : 0;
        eventPublisher.publishJobProgress(
            triggerId,
            percentage,
            String.format("Processed %d of %d records", processedCount, totalRecords)
        );
    }
}
```

### 2.6 Step Progress Listener

**File:** `cmips-backend/src/main/java/com/.../batch/listener/StepProgressListener.java`

```java
@Component
@StepScope
public class StepProgressListener implements StepExecutionListener, ChunkListener {

    private final JobEventPublisher eventPublisher;

    @Value("#{jobParameters['triggerId']}")
    private String triggerId;

    private int totalChunks = 0;
    private int processedChunks = 0;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Starting step: {}", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("Completed step: {} with status: {}",
                 stepExecution.getStepName(),
                 stepExecution.getStatus());

        // Log step metrics
        log.info("Read: {}, Written: {}, Skipped: {}",
                 stepExecution.getReadCount(),
                 stepExecution.getWriteCount(),
                 stepExecution.getSkipCount());

        return stepExecution.getExitStatus();
    }

    @Override
    public void afterChunk(ChunkContext context) {
        processedChunks++;
        StepExecution stepExecution = context.getStepContext().getStepExecution();

        int readCount = stepExecution.getReadCount();
        int writeCount = stepExecution.getWriteCount();

        eventPublisher.publishJobProgress(
            triggerId,
            calculatePercentage(writeCount),
            String.format("Step %s: processed %d records",
                         stepExecution.getStepName(), writeCount)
        );
    }
}
```

---

## Phase 3: Migrate Existing Jobs

### 3.1 Job Mapping Table

| Current Custom Job | Spring Batch Job Name | Priority |
|-------------------|----------------------|----------|
| County Daily Report | `COUNTY_DAILY_REPORT` | High |
| Weekly Sync | `WEEKLY_SYNC` | High |
| Monthly Aggregation | `MONTHLY_AGGREGATION` | Medium |
| Quarterly Summary | `QUARTERLY_SUMMARY` | Medium |
| Annual Report | `ANNUAL_REPORT` | Low |
| Data Cleanup | `DATA_CLEANUP` | Low |
| SFTP Delivery | `SFTP_DELIVERY` | Medium |

### 3.2 Migration Steps per Job

For each job:

1. **Create Job Configuration Class**
   ```
   /batch/jobs/{JobName}JobConfig.java
   ```

2. **Create ItemReader** (if chunk-oriented)
   ```
   /batch/reader/{JobName}ItemReader.java
   ```

3. **Create ItemProcessor** (if needed)
   ```
   /batch/processor/{JobName}ItemProcessor.java
   ```

4. **Create ItemWriter**
   ```
   /batch/writer/{JobName}ItemWriter.java
   ```

5. **Create Tasklets** (for non-chunk steps)
   ```
   /batch/tasklet/{JobName}Tasklet.java
   ```

6. **Register in JobRegistryService**

7. **Add to Scheduler's job_definition table**

8. **Test end-to-end**

### 3.3 Parallel Running Strategy

During migration, both systems can run:

```yaml
# application.yml
batch:
  legacy:
    enabled: true   # Keep custom batch running
  spring-batch:
    enabled: true   # Enable Spring Batch
  migration:
    mode: parallel  # Run both, compare results
```

```java
@Service
public class BatchRoutingService {

    @Value("${batch.migration.mode:parallel}")
    private String migrationMode;

    public void executeJob(String jobName, Map<String, Object> params) {
        if ("spring-batch".equals(migrationMode)) {
            // Only Spring Batch
            springBatchService.execute(jobName, params);
        } else if ("legacy".equals(migrationMode)) {
            // Only legacy
            legacyBatchService.execute(jobName, params);
        } else {
            // Parallel - run both, compare
            CompletableFuture<Result> legacyResult =
                CompletableFuture.supplyAsync(() -> legacyBatchService.execute(jobName, params));
            CompletableFuture<Result> batchResult =
                CompletableFuture.supplyAsync(() -> springBatchService.execute(jobName, params));

            // Compare results for validation
            compareResults(legacyResult.join(), batchResult.join());
        }
    }
}
```

---

## Phase 4: Remove Legacy Code

### 4.1 Files to Remove/Deprecate

```
REMOVE:
├── service/
│   ├── BatchJobScheduler.java          # Replaced by Scheduler App
│   ├── JobQueueService.java            # Replaced by Spring Batch
│   ├── BackgroundProcessingService.java # Replaced by Spring Batch Jobs
│   └── EnhancedBatchQueueService.java  # Replaced by Spring Batch
│
├── config/
│   └── EnhancedBatchConfig.java        # Replaced by SpringBatchConfig
│
└── entity/
    └── ReportJobEntity.java            # Replaced by Spring Batch tables
                                        # (Keep for historical data migration)

KEEP:
├── service/
│   ├── ReportGenerationService.java    # Business logic, used by ItemProcessor
│   ├── EmailReportService.java         # Used by delivery step
│   ├── SFTPDeliveryService.java        # Used by delivery step
│   └── JobDependencyService.java       # Move to Scheduler App
│
├── entity/
│   ├── BatchJobDefinitionEntity.java   # Move to Scheduler App
│   ├── BatchJobDependencyEntity.java   # Move to Scheduler App
│   └── BatchJobCalendarEntity.java     # Move to Scheduler App
│
└── repository/
    └── ReportDataRepository.java       # Business data, keep
```

### 4.2 Configuration Cleanup

**Remove from application.yml:**
```yaml
# REMOVE these sections
batch:
  scheduler:
    enabled: true
    interval-ms: 5000
  queue:
    priority: "batch:job:priority"
    processing-hash: "batch:job:processing:hash"
  enhanced:
    enabled: true
```

**Keep/Modify:**
```yaml
# KEEP these sections (modified)
spring:
  batch:
    job:
      enabled: false
    jdbc:
      initialize-schema: always

redis:
  channels:
    job-started: batch:events:job-started
    job-progress: batch:events:job-progress
    job-completed: batch:events:job-completed
    job-failed: batch:events:job-failed
```

---

## Phase 5: Database Migration

### 5.1 Spring Batch Tables (Auto-created)

Spring Batch will create these tables automatically:

```sql
-- Created by Spring Batch
BATCH_JOB_INSTANCE
BATCH_JOB_EXECUTION
BATCH_JOB_EXECUTION_PARAMS
BATCH_JOB_EXECUTION_CONTEXT
BATCH_STEP_EXECUTION
BATCH_STEP_EXECUTION_CONTEXT
BATCH_JOB_SEQ
BATCH_JOB_EXECUTION_SEQ
BATCH_STEP_EXECUTION_SEQ
```

### 5.2 Data Migration Script

```sql
-- Migrate historical job executions to Spring Batch tables
-- (Optional - for historical reporting)

INSERT INTO BATCH_JOB_INSTANCE (JOB_INSTANCE_ID, VERSION, JOB_NAME, JOB_KEY)
SELECT
    job_id,
    0,
    report_type,
    MD5(CONCAT(report_type, '-', user_role, '-', created_at))
FROM report_jobs
WHERE status IN ('COMPLETED', 'FAILED');

INSERT INTO BATCH_JOB_EXECUTION (
    JOB_EXECUTION_ID, VERSION, JOB_INSTANCE_ID,
    CREATE_TIME, START_TIME, END_TIME, STATUS, EXIT_CODE
)
SELECT
    job_id,
    0,
    job_id,
    created_at,
    started_at,
    completed_at,
    CASE status
        WHEN 'COMPLETED' THEN 'COMPLETED'
        WHEN 'FAILED' THEN 'FAILED'
        ELSE 'UNKNOWN'
    END,
    CASE status
        WHEN 'COMPLETED' THEN 'COMPLETED'
        WHEN 'FAILED' THEN 'FAILED'
        ELSE 'UNKNOWN'
    END
FROM report_jobs;
```

### 5.3 Scheduler Database Setup

The Scheduler app tables already exist. Ensure CMIPS can READ from:
- `job_definition` (for job metadata)

---

## Phase 6: Integration Testing

### 6.1 Test Scenarios

| Test Case | Description | Expected Result |
|-----------|-------------|-----------------|
| TC-01 | Scheduler triggers job via REST | Job starts, events published |
| TC-02 | Job completes successfully | COMPLETED event received |
| TC-03 | Job fails | FAILED event with error message |
| TC-04 | Job progress updates | Progress events at intervals |
| TC-05 | Stop running job | Job stops, STOPPED event |
| TC-06 | Retry failed job | New execution created |
| TC-07 | Concurrent job limit | Jobs queued properly |
| TC-08 | Job with dependencies | Dependent job waits |

### 6.2 Integration Test Example

```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.batch.job.enabled=false"
})
class BatchIntegrationTest {

    @Autowired
    private BatchTriggerController triggerController;

    @Autowired
    private JobExplorer jobExplorer;

    @Test
    void testTriggerJob() {
        TriggerRequest request = new TriggerRequest();
        request.setJobName("COUNTY_DAILY_REPORT");
        request.setTriggerId(UUID.randomUUID().toString());
        request.setParameters(Map.of("county", "LA"));

        ResponseEntity<TriggerResponse> response = triggerController.triggerJob(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getStatus()).isIn("STARTING", "STARTED");

        // Wait for completion
        await().atMost(30, TimeUnit.SECONDS).until(() -> {
            JobExecution execution = jobExplorer.getJobExecution(
                response.getBody().getExecutionId());
            return execution.getStatus() == BatchStatus.COMPLETED;
        });
    }
}
```

---

## Phase 7: Rollback Plan

### 7.1 Feature Flags

```yaml
feature:
  spring-batch:
    enabled: ${SPRING_BATCH_ENABLED:true}
  legacy-batch:
    enabled: ${LEGACY_BATCH_ENABLED:false}
```

### 7.2 Rollback Steps

1. Set `SPRING_BATCH_ENABLED=false`
2. Set `LEGACY_BATCH_ENABLED=true`
3. Restart CMIPS application
4. Verify legacy batch processing works
5. Investigate Spring Batch issues
6. Fix and re-enable

---

## Timeline Estimate

| Phase | Description | Tasks |
|-------|-------------|-------|
| Phase 1 | Foundation Setup | Add deps, config, REST endpoints, event publisher |
| Phase 2 | Create Spring Batch Jobs | Job configs, readers, processors, writers |
| Phase 3 | Migrate Existing Jobs | Convert each job type |
| Phase 4 | Remove Legacy Code | Cleanup old code |
| Phase 5 | Database Migration | Migrate historical data |
| Phase 6 | Integration Testing | End-to-end tests |
| Phase 7 | Production Rollout | Deploy with feature flags |

---

## File Structure After Migration

```
cmips-backend/src/main/java/com/ihss/cmips-backend/
├── batch/
│   ├── config/
│   │   └── SpringBatchConfig.java
│   ├── jobs/
│   │   ├── CountyDailyReportJobConfig.java
│   │   ├── WeeklySyncJobConfig.java
│   │   ├── MonthlyAggregationJobConfig.java
│   │   └── ...
│   ├── reader/
│   │   ├── CountyReportItemReader.java
│   │   └── ...
│   ├── processor/
│   │   ├── CountyReportItemProcessor.java
│   │   └── ...
│   ├── writer/
│   │   ├── CountyReportItemWriter.java
│   │   └── ...
│   ├── tasklet/
│   │   ├── ReportGenerationTasklet.java
│   │   ├── ReportDeliveryTasklet.java
│   │   └── ...
│   ├── listener/
│   │   ├── JobExecutionNotificationListener.java
│   │   └── StepProgressListener.java
│   └── JobRegistryService.java
├── controller/
│   └── BatchTriggerController.java
├── service/
│   ├── JobEventPublisher.java
│   └── ... (existing business services)
└── dto/
    ├── TriggerRequest.java
    ├── TriggerResponse.java
    └── JobEvent.java
```

---

## Summary

This migration plan provides a structured approach to:

1. **Add Spring Batch** without breaking existing functionality
2. **Create integration layer** (REST + Redis events) for Scheduler communication
3. **Migrate jobs incrementally** with parallel running capability
4. **Clean up legacy code** once Spring Batch is validated
5. **Maintain rollback capability** throughout the process

The key principle is **"Scheduler = Brain, CMIPS + Spring Batch = Muscle"** where:
- Scheduler decides WHEN to run (cron, dependencies, calendars)
- CMIPS executes HOW to run (Spring Batch jobs, business logic)
- Redis events bridge the communication gap
