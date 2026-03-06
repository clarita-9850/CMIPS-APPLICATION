package com.ihss.scheduler.service;

import com.ihss.scheduler.dto.DashboardStatsDTO;
import com.ihss.scheduler.dto.ExecutionSummaryDTO;
import com.ihss.scheduler.entity.ExecutionStatus;
import com.ihss.scheduler.entity.JobStatus;
import com.ihss.scheduler.repository.ExecutionMappingRepository;
import com.ihss.scheduler.repository.JobDefinitionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final JobDefinitionRepository jobRepository;
    private final ExecutionMappingRepository executionRepository;

    public DashboardService(
            JobDefinitionRepository jobRepository,
            ExecutionMappingRepository executionRepository) {
        this.jobRepository = jobRepository;
        this.executionRepository = executionRepository;
    }

    public DashboardStatsDTO getStats() {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        long totalJobs = jobRepository.findAllByDeletedAtIsNull().size();
        long activeJobs = jobRepository.findByStatusAndDeletedAtIsNull(JobStatus.ACTIVE).size();
        long runningExecutions = executionRepository.findAllRunningExecutions().size();

        // Get today's execution counts
        List<Object[]> statusCounts = executionRepository.countByStatusSince(todayStart);
        Map<String, Long> executionsByStatus = new HashMap<>();
        long completedToday = 0;
        long failedToday = 0;

        for (Object[] row : statusCounts) {
            ExecutionStatus status = (ExecutionStatus) row[0];
            Long count = (Long) row[1];
            executionsByStatus.put(status.name(), count);

            if (status == ExecutionStatus.COMPLETED) {
                completedToday = count;
            } else if (status == ExecutionStatus.FAILED) {
                failedToday = count;
            }
        }

        // Get jobs by type
        List<String> jobTypes = jobRepository.findDistinctJobTypes();
        Map<String, Long> jobsByType = new HashMap<>();
        for (String type : jobTypes) {
            long count = jobRepository.findByJobTypeAndDeletedAtIsNull(type).size();
            jobsByType.put(type, count);
        }

        // Get jobs by status
        Map<String, Long> jobsByStatus = new HashMap<>();
        for (JobStatus status : JobStatus.values()) {
            long count = jobRepository.findByStatusAndDeletedAtIsNull(status).size();
            jobsByStatus.put(status.name(), count);
        }

        return new DashboardStatsDTO(
            totalJobs,
            activeJobs,
            runningExecutions,
            completedToday,
            failedToday,
            executionsByStatus,
            jobsByType,
            jobsByStatus
        );
    }

    public List<ExecutionSummaryDTO> getRecentExecutions(int limit) {
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        return executionRepository.findRecentWithJobDetails(since, PageRequest.of(0, limit))
            .getContent()
            .stream()
            .map(ExecutionSummaryDTO::fromEntity)
            .collect(Collectors.toList());
    }

    public List<ExecutionSummaryDTO> getRunningExecutions() {
        return executionRepository.findAllRunningExecutions()
            .stream()
            .map(ExecutionSummaryDTO::fromEntity)
            .collect(Collectors.toList());
    }
}
