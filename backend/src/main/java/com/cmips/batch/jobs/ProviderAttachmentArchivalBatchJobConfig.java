package com.cmips.batch.jobs;

import com.cmips.batch.JobExecutionNotificationListener;
import com.cmips.batch.StepProgressListener;
import com.cmips.entity.ProviderAttachmentEntity;
import com.cmips.entity.ProviderAttachmentEntity.AttachmentStatus;
import com.cmips.repository.ProviderAttachmentRepository;
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
 * Spring Batch Job — Nightly Provider Attachment Archival (CI-117642)
 *
 * DSD: Attachments are nightly archived. Users may restore same-day only.
 * Scheduled: Nightly (1:00 AM) via @Scheduled cron in scheduler module.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ProviderAttachmentArchivalBatchJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobExecutionNotificationListener jobListener;
    private final StepProgressListener stepListener;
    private final ProviderAttachmentRepository attachmentRepository;

    @Bean(name = "providerAttachmentArchivalJob")
    public Job providerAttachmentArchivalJob() {
        return new JobBuilder("PROVIDER_ATTACHMENT_ARCHIVAL_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobListener)
                .start(archiveAttachmentsStep())
                .build();
    }

    @Bean
    public Step archiveAttachmentsStep() {
        return new StepBuilder("ARCHIVE_ATTACHMENTS_STEP", jobRepository)
                .tasklet(archiveAttachmentsTasklet(), transactionManager)
                .listener(stepListener)
                .build();
    }

    private Tasklet archiveAttachmentsTasklet() {
        return (contribution, chunkContext) -> {
            LocalDate today = LocalDate.now();
            log.info("[ATTACHMENT_ARCHIVAL] Starting nightly attachment archival for date: {}", today);

            // Archive all ACTIVE attachments not uploaded today
            List<ProviderAttachmentEntity> toArchive = attachmentRepository.findActiveForNightlyArchival(today);

            AtomicInteger archived = new AtomicInteger(0);
            toArchive.forEach(attachment -> {
                try {
                    attachment.setStatus(AttachmentStatus.ARCHIVED);
                    attachment.setArchivedDate(today);
                    attachment.setArchivedBy("NIGHTLY_BATCH");
                    attachment.setUpdatedBy("NIGHTLY_BATCH");
                    attachmentRepository.save(attachment);
                    archived.incrementAndGet();
                } catch (Exception e) {
                    log.error("[ATTACHMENT_ARCHIVAL] Error archiving attachment {}: {}", attachment.getId(), e.getMessage());
                }
            });

            log.info("[ATTACHMENT_ARCHIVAL] Nightly archival complete: {} attachments archived", archived.get());
            return RepeatStatus.FINISHED;
        };
    }
}
