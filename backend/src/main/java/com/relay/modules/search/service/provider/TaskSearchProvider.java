package com.relay.modules.search.service.provider;

import com.relay.modules.search.api.dto.SearchResultDto;
import com.relay.modules.search.repository.SearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskSearchProvider implements SearchProvider {

    private final SearchRepository searchRepository;

    @Override
    public String getEntityType() {
        return "TASK";
    }

    @Override
    public List<SearchResultDto> search(String query, String workspacePublicId, Long userId, Pageable pageable) {
        return searchRepository.searchTasks(query, workspacePublicId, userId, pageable.getPageSize() + 1, (int) pageable.getOffset());
    }
}
