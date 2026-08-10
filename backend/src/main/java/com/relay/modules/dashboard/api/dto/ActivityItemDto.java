package com.relay.modules.dashboard.api.dto;

import java.time.Instant;
import java.util.Map;

public record ActivityItemDto(
    String id,
    String type, // MESSAGE, TASK_UPDATE, FILE_UPLOAD, CHANNEL_CREATED
    String title,
    String description,
    String actorName,
    String actorAvatar,
    Instant timestamp,
    Map<String, Object> metadata
) {}
