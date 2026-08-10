package com.relay.modules.work.api.dto;

import java.time.Instant;
import java.util.List;

public record TaskDto(
    String publicId,
    String title,
    String description,
    String status,
    String priority,
    Instant startDate,
    Instant dueDate,
    Instant completedAt,
    String estimate,
    Integer storyPoints,
    String creatorPublicId,
    String linkedMessagePublicId,
    List<String> assigneePublicIds,
    List<LabelDto> labels,
    Instant createdAt,
    Instant updatedAt
) {}
