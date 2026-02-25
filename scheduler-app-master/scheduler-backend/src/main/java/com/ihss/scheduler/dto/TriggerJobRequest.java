package com.ihss.scheduler.dto;

import java.util.Map;

public record TriggerJobRequest(
    Map<String, Object> parameters,
    Boolean skipDependencyCheck
) {
    public TriggerJobRequest {
        if (skipDependencyCheck == null) skipDependencyCheck = false;
    }
}
