package com.relay.modules.work.api.dto;

import com.relay.modules.work.domain.TaskPriority;
import com.relay.modules.work.domain.TaskStatus;

import java.time.Instant;
import java.util.List;

public record UpdateTaskRequest(
    String title,
    String description,
    TaskStatus status,
    TaskPriority priority,
    Instant startDate,
    Instant dueDate,
    String estimate,
    Integer storyPoints,
    List<String> assigneePublicIds,
    List<String> labelNames
) {}
