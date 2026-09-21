package com.relay.modules.dashboard.repository;

import com.relay.modules.dashboard.api.dto.ActivityItemDto;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class DashboardRepository {

    private final JdbcClient jdbcClient;

    public DashboardRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<ActivityItemDto> getUnifiedTimeline(String workspacePublicId, Long userId, int limit) {
        String sql = """
            (
                SELECT m.public_id as id, 
                       'MESSAGE' as type, 
                       'New Message' as title, 
                       m.content as description, 
                       u.display_name as actorName, 
                       u.avatar_url as actorAvatar, 
                       m.created_at as timestamp,
                       c.public_id as meta1, 
                       c.name as meta2
                FROM messages m
                JOIN users u ON m.sender_id = u.id
                JOIN channels c ON m.channel_id = c.id
                JOIN channel_members cm ON c.id = cm.channel_id
                JOIN workspaces w ON c.workspace_id = w.id
                WHERE cm.user_id = :userId AND w.public_id = :workspaceId AND m.deleted_at IS NULL
                ORDER BY m.created_at DESC LIMIT :limit
            )
            UNION ALL
            (
                SELECT CAST(ta.id AS CHAR) as id,
                       'TASK_UPDATE' as type,
                       CONCAT('Task ', ta.activity_type) as title,
                       t.title as description,
                       u.display_name as actorName,
                       u.avatar_url as actorAvatar,
                       ta.created_at as timestamp,
                       t.public_id as meta1,
                       '' as meta2
                FROM task_activity ta
                JOIN tasks t ON ta.task_id = t.id
                JOIN users u ON ta.actor_id = u.id
                JOIN workspaces w ON t.workspace_id = w.id
                WHERE w.public_id = :workspaceId
                ORDER BY ta.created_at DESC LIMIT :limit
            )
            UNION ALL
            (
                SELECT f.public_id as id,
                       'FILE_UPLOAD' as type,
                       'File Uploaded' as title,
                       f.original_name as description,
                       u.display_name as actorName,
                       u.avatar_url as actorAvatar,
                       f.uploaded_at as timestamp,
                       f.public_id as meta1,
                       m.public_id as meta2
                FROM file_attachments f
                JOIN messages m ON f.message_id = m.id
                JOIN users u ON m.sender_id = u.id
                JOIN channels c ON m.channel_id = c.id
                JOIN channel_members cm ON c.id = cm.channel_id
                JOIN workspaces w ON c.workspace_id = w.id
                WHERE cm.user_id = :userId AND w.public_id = :workspaceId
                ORDER BY f.uploaded_at DESC LIMIT :limit
            )
            ORDER BY timestamp DESC
            LIMIT :limit
        """;

        return jdbcClient.sql(sql)
            .param("userId", userId)
            .param("workspaceId", workspacePublicId)
            .param("limit", limit)
            .query((rs, rowNum) -> new ActivityItemDto(
                rs.getString("id"),
                rs.getString("type"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("actorName"),
                rs.getString("actorAvatar"),
                rs.getTimestamp("timestamp").toInstant(),
                Map.of("meta1", rs.getString("meta1") != null ? rs.getString("meta1") : "",
                       "meta2", rs.getString("meta2") != null ? rs.getString("meta2") : "")
            )).list();
    }

    public com.relay.modules.dashboard.api.dto.DashboardStatsDto getDashboardStats(String workspacePublicId, Long userId) {
        String sql = """
            SELECT 
                (SELECT COUNT(*) FROM tasks t 
                 JOIN workspaces w ON t.workspace_id = w.id 
                 JOIN task_assignees ta ON t.id = ta.task_id 
                 WHERE w.public_id = :workspaceId AND ta.user_id = :userId AND t.status != 'DONE' AND t.deleted_at IS NULL) as activeTasks,
                 
                (SELECT COUNT(*) FROM tasks t 
                 JOIN workspaces w ON t.workspace_id = w.id 
                 JOIN task_assignees ta ON t.id = ta.task_id 
                 WHERE w.public_id = :workspaceId AND ta.user_id = :userId AND t.status != 'DONE' AND t.due_date < NOW() AND t.deleted_at IS NULL) as overdueTasks,
                 
                (SELECT COUNT(*) FROM tasks t 
                 JOIN workspaces w ON t.workspace_id = w.id 
                 JOIN task_assignees ta ON t.id = ta.task_id 
                 WHERE w.public_id = :workspaceId AND ta.user_id = :userId AND t.status != 'DONE' AND DATE(t.due_date) = CURDATE() AND t.deleted_at IS NULL) as dueTodayTasks,
                 
                (SELECT COUNT(m.id) FROM messages m 
                 JOIN channels c ON m.channel_id = c.id
                 JOIN channel_members cm ON c.id = cm.channel_id
                 JOIN workspaces w ON c.workspace_id = w.id
                 WHERE w.public_id = :workspaceId AND cm.user_id = :userId AND (cm.last_read_at IS NULL OR m.created_at > cm.last_read_at) AND m.deleted_at IS NULL) as unreadMessages,
                 
                (SELECT COUNT(*) FROM channels c 
                 JOIN workspaces w ON c.workspace_id = w.id 
                 JOIN channel_members cm ON c.id = cm.channel_id 
                 WHERE w.public_id = :workspaceId AND cm.user_id = :userId AND c.deleted_at IS NULL) as activeChannels,
                 
                (SELECT COUNT(DISTINCT u.id) FROM users u
                 JOIN workspace_members wm ON u.id = wm.user_id
                 JOIN workspaces w ON wm.workspace_id = w.id
                 WHERE w.public_id = :workspaceId AND u.deleted_at IS NULL AND u.last_seen_at >= DATE_SUB(NOW(), INTERVAL 5 MINUTE)) as onlineMembers
        """;

        return jdbcClient.sql(sql)
            .param("userId", userId)
            .param("workspaceId", workspacePublicId)
            .query((rs, rowNum) -> new com.relay.modules.dashboard.api.dto.DashboardStatsDto(
                rs.getInt("activeTasks"),
                rs.getInt("overdueTasks"),
                rs.getInt("dueTodayTasks"),
                rs.getInt("unreadMessages"),
                rs.getInt("activeChannels"),
                rs.getInt("onlineMembers")
            )).single();
    }
}
