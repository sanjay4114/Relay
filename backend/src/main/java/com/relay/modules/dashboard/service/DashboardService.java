package com.relay.modules.dashboard.service;

import com.relay.modules.dashboard.api.dto.ActivityItemDto;
import com.relay.modules.dashboard.repository.DashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public List<ActivityItemDto> getUnifiedTimeline(String workspacePublicId, Long userId, int limit) {
        return dashboardRepository.getUnifiedTimeline(workspacePublicId, userId, limit);
    }

    public com.relay.modules.dashboard.api.dto.DashboardStatsDto getStats(String workspacePublicId, Long userId) {
        return dashboardRepository.getDashboardStats(workspacePublicId, userId);
    }
}
