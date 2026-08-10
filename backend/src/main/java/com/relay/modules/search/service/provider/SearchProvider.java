package com.relay.modules.search.service.provider;

import com.relay.modules.search.api.dto.SearchResultDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SearchProvider {
    String getEntityType();
    List<SearchResultDto> search(String query, String workspacePublicId, Long userId, Pageable pageable);
}
