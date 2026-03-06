package com.ihss.scheduler.dto;

import com.ihss.scheduler.entity.DependencyType;
import jakarta.validation.constraints.NotNull;

public record AddDependencyRequest(
    @NotNull(message = "Depends on job ID is required")
    Long dependsOnJobId,

    DependencyType dependencyType
) {
    public AddDependencyRequest {
        if (dependencyType == null) dependencyType = DependencyType.SUCCESS;
    }
}
