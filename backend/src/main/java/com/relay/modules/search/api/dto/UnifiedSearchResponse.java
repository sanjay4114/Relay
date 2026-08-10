package com.relay.modules.search.api.dto;

import java.util.List;

public record UnifiedSearchResponse(
    List<SearchResultDto> results,
    int page,
    int size,
    boolean hasNext
) {}
