package com.relay.modules.search.service;

import com.relay.modules.search.api.dto.SearchResultDto;
import com.relay.modules.search.api.dto.UnifiedSearchResponse;
import com.relay.modules.search.service.provider.SearchProvider;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final Map<String, SearchProvider> providers;

    public SearchService(List<SearchProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(SearchProvider::getEntityType, Function.identity()));
    }

    public UnifiedSearchResponse search(String query, String workspacePublicId, String type, Long userId, Pageable pageable) {
        List<SearchResultDto> results = new ArrayList<>();
        boolean hasNext = false;
        
        if (type != null && !type.isBlank()) {
            SearchProvider provider = providers.get(type.toUpperCase());
            if (provider != null) {
                List<SearchResultDto> providerResults = provider.search(query, workspacePublicId, userId, pageable);
                if (providerResults.size() > pageable.getPageSize()) {
                    hasNext = true;
                    providerResults = providerResults.subList(0, pageable.getPageSize());
                }
                results.addAll(providerResults);
            }
        } else {
            // Unified search across all providers. 
            // Give each provider a smaller page size, e.g., max 3 items each for overview
            int itemsPerType = 3;
            Pageable subPageable = org.springframework.data.domain.PageRequest.of(0, itemsPerType);
            
            for (SearchProvider provider : providers.values()) {
                List<SearchResultDto> providerResults = provider.search(query, workspacePublicId, userId, subPageable);
                if (providerResults.size() > itemsPerType) {
                    providerResults = providerResults.subList(0, itemsPerType);
                }
                results.addAll(providerResults);
            }
            // For unified search, we typically rely on client to drill down for more, so hasNext is false
            hasNext = false; 
        }

        return new UnifiedSearchResponse(results, pageable.getPageNumber(), pageable.getPageSize(), hasNext);
    }
}
