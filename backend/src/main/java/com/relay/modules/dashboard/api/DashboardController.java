package com.relay.modules.dashboard.api;

import com.relay.common.dto.ApiResponse;
import com.relay.config.security.AuthenticatedUser;
import com.relay.modules.dashboard.api.dto.ActivityItemDto;
import com.relay.modules.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard API", description = "Endpoints for aggregated dashboard data")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get unified timeline", description = "Get recent activity timeline across all entities")
    @GetMapping("/timeline")
    public ApiResponse<List<ActivityItemDto>> getTimeline(
            @PathVariable String workspaceId,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(dashboardService.getUnifiedTimeline(workspaceId, user.userId(), limit));
    }

    @Operation(summary = "Get dashboard stats", description = "Get aggregated statistics for the workspace")
    @GetMapping("/stats")
    public ApiResponse<com.relay.modules.dashboard.api.dto.DashboardStatsDto> getStats(
            @PathVariable String workspaceId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(dashboardService.getStats(workspaceId, user.userId()));
    }
}
