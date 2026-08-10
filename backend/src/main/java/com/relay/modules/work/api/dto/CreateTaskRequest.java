package com.relay.modules.work.api.dto;

import com.relay.modules.work.domain.TaskPriority;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;

public record CreateTaskRequest(
    @NotBlank String title,
    String description,
    TaskPriority priority,
    Instant startDate,
    Instant dueDate,
    String estimate,
    Integer storyPoints,
    List<String> assigneePublicIds,
    List<String> labelNames,
    String linkedMessagePublicId
) {}
