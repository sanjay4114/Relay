package com.relay.modules.search.api;

import com.relay.common.dto.ApiResponse;
import com.relay.config.security.AuthenticatedUser;
import com.relay.modules.search.api.dto.UnifiedSearchResponse;
import com.relay.modules.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search API", description = "Endpoints for global platform search")
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "Global search", description = "Search across messages, channels, users, and files with authorization filtering.")
    @GetMapping
    public ApiResponse<UnifiedSearchResponse> search(
            @RequestParam String query,
            @RequestParam(required = false) String workspaceId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUser user) {
            
        if (query == null || query.trim().length() < 2) {
            // Return empty response for too short queries
            return ApiResponse.ok(new UnifiedSearchResponse(java.util.List.of(), page, size, false));
        }
        
        return ApiResponse.ok(searchService.search(
                query.trim(), 
                workspaceId, 
                type, 
                user.userId(), 
                PageRequest.of(page, size)
        ));
    }
}
