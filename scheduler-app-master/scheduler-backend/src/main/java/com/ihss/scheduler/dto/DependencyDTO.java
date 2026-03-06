package com.ihss.scheduler.dto;

import com.ihss.scheduler.entity.DependencyType;

public record DependencyDTO(
    Long id,
    Long jobId,
    String jobName,
    Long dependsOnJobId,
    String dependsOnJobName,
    DependencyType dependencyType,
    Boolean isActive
) {
    public static DependencyDTO fromEntity(com.ihss.scheduler.entity.JobDependency entity) {
        return new DependencyDTO(
            entity.getId(),
            entity.getJob().getId(),
            entity.getJob().getJobName(),
            entity.getDependsOnJob().getId(),
            entity.getDependsOnJob().getJobName(),
            entity.getDependencyType(),
            entity.getIsActive()
        );
    }
}
