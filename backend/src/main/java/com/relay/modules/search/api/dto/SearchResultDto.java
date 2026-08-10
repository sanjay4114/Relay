package com.relay.modules.search.api.dto;

import java.time.Instant;
import java.util.Map;

public record SearchResultDto(
    String id,
    String type,
    String title,
    String subtitle,
    String url,
    String imageUrl,
    Instant createdAt,
    Map<String, Object> metadata
) {}
