package com.relay.modules.dashboard.api.dto;

public record DashboardStatsDto(
    int activeTasks,
    int overdueTasks,
    int dueTodayTasks,
    int unreadMessages,
    int activeChannels,
    int onlineMembers
) {}
