package com.relay.modules.search.repository;

import com.relay.modules.search.api.dto.SearchResultDto;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class SearchRepository {

    private final JdbcClient jdbcClient;

    public SearchRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<SearchResultDto> searchMessages(String query, String workspacePublicId, Long userId, int limit, int offset) {
        String sql = """
            SELECT m.public_id as id, 
                   'MESSAGE' as type, 
                   m.content as title, 
                   u.display_name as subtitle, 
                   m.created_at as createdAt,
                   c.public_id as channelPublicId,
                   c.name as channelName,
                   w.public_id as workspacePublicId
            FROM messages m
            JOIN users u ON m.sender_id = u.id
            JOIN channels c ON m.channel_id = c.id
            JOIN channel_members cm ON c.id = cm.channel_id
            JOIN workspaces w ON c.workspace_id = w.id
            WHERE MATCH(m.content) AGAINST (:query IN BOOLEAN MODE)
              AND cm.user_id = :userId
              AND m.deleted_at IS NULL
              AND (:workspaceId IS NULL OR w.public_id = :workspaceId)
            ORDER BY m.created_at DESC
            LIMIT :limit OFFSET :offset
        """;
        
        return jdbcClient.sql(sql)
            .param("query", query + "*")
            .param("userId", userId)
            .param("workspaceId", workspacePublicId)
            .param("limit", limit)
            .param("offset", offset)
            .query((rs, rowNum) -> new SearchResultDto(
                rs.getString("id"),
                rs.getString("type"),
                rs.getString("title"),
                rs.getString("subtitle"),
                null,
                null,
                rs.getTimestamp("createdAt").toInstant(),
                Map.of(
                    "channelId", rs.getString("channelPublicId"),
                    "channelName", rs.getString("channelName"),
                    "workspaceId", rs.getString("workspacePublicId")
                )
            )).list();
    }

    public List<SearchResultDto> searchChannels(String query, String workspacePublicId, Long userId, int limit, int offset) {
        String sql = """
            SELECT c.public_id as id,
                   'CHANNEL' as type,
                   c.name as title,
                   c.description as subtitle,
                   c.created_at as createdAt,
                   w.public_id as workspacePublicId
            FROM channels c
            JOIN workspaces w ON c.workspace_id = w.id
            JOIN workspace_members wm ON w.id = wm.workspace_id
            LEFT JOIN channel_members cm ON c.id = cm.channel_id AND cm.user_id = :userId
            WHERE MATCH(c.name, c.description) AGAINST (:query IN BOOLEAN MODE)
              AND wm.user_id = :userId
              AND c.deleted_at IS NULL
              AND (c.visibility = 'PUBLIC' OR cm.user_id IS NOT NULL)
              AND (:workspaceId IS NULL OR w.public_id = :workspaceId)
            ORDER BY c.created_at DESC
            LIMIT :limit OFFSET :offset
        """;

        return jdbcClient.sql(sql)
            .param("query", query + "*")
            .param("userId", userId)
            .param("workspaceId", workspacePublicId)
            .param("limit", limit)
            .param("offset", offset)
            .query((rs, rowNum) -> new SearchResultDto(
                rs.getString("id"),
                rs.getString("type"),
                rs.getString("title"),
                rs.getString("subtitle"),
                null,
                null,
                rs.getTimestamp("createdAt").toInstant(),
                Map.of("workspaceId", rs.getString("workspacePublicId"))
            )).list();
    }

    public List<SearchResultDto> searchUsers(String query, String workspacePublicId, Long userId, int limit, int offset) {
        String sql = """
            SELECT DISTINCT u.public_id as id,
                   'USER' as type,
                   u.display_name as title,
                   u.email as subtitle,
                   u.avatar_url as imageUrl,
                   u.created_at as createdAt
            FROM users u
            JOIN workspace_members wm_target ON u.id = wm_target.user_id
            JOIN workspace_members wm_current ON wm_target.workspace_id = wm_current.workspace_id
            JOIN workspaces w ON wm_target.workspace_id = w.id
            WHERE MATCH(u.display_name, u.email) AGAINST (:query IN BOOLEAN MODE)
              AND wm_current.user_id = :userId
              AND u.deleted_at IS NULL
              AND (:workspaceId IS NULL OR w.public_id = :workspaceId)
            ORDER BY u.display_name ASC
            LIMIT :limit OFFSET :offset
        """;

        return jdbcClient.sql(sql)
            .param("query", query + "*")
            .param("userId", userId)
            .param("workspaceId", workspacePublicId)
            .param("limit", limit)
            .param("offset", offset)
            .query((rs, rowNum) -> new SearchResultDto(
                rs.getString("id"),
                rs.getString("type"),
                rs.getString("title"),
                rs.getString("subtitle"),
                null,
                rs.getString("imageUrl"),
                rs.getTimestamp("createdAt").toInstant(),
                Map.of()
            )).list();
    }

    public List<SearchResultDto> searchFiles(String query, String workspacePublicId, Long userId, int limit, int offset) {
        String sql = """
            SELECT f.public_id as id,
                   'FILE' as type,
                   f.original_name as title,
                   f.mime_type as subtitle,
                   f.thumbnail_path as imageUrl,
                   f.created_at as createdAt,
                   m.public_id as messagePublicId,
                   c.public_id as channelPublicId,
                   w.public_id as workspacePublicId
            FROM file_attachments f
            JOIN messages m ON f.message_id = m.id
            JOIN channels c ON m.channel_id = c.id
            JOIN channel_members cm ON c.id = cm.channel_id
            JOIN workspaces w ON c.workspace_id = w.id
            WHERE MATCH(f.original_name) AGAINST (:query IN BOOLEAN MODE)
              AND cm.user_id = :userId
              AND m.deleted_at IS NULL
              AND (:workspaceId IS NULL OR w.public_id = :workspaceId)
            ORDER BY f.created_at DESC
            LIMIT :limit OFFSET :offset
        """;

        return jdbcClient.sql(sql)
            .param("query", query + "*")
            .param("userId", userId)
            .param("workspaceId", workspacePublicId)
            .param("limit", limit)
            .param("offset", offset)
            .query((rs, rowNum) -> new SearchResultDto(
                rs.getString("id"),
                rs.getString("type"),
                rs.getString("title"),
                rs.getString("subtitle"),
                "/api/v1/files/" + rs.getString("id") + "/download",
                rs.getString("imageUrl") != null ? "/api/v1/files/" + rs.getString("id") + "/thumbnail" : null,
                rs.getTimestamp("createdAt").toInstant(),
                Map.of(
                    "messageId", rs.getString("messagePublicId"),
                    "channelId", rs.getString("channelPublicId"),
                    "workspaceId", rs.getString("workspacePublicId")
                )
            )).list();
    }

    public List<SearchResultDto> searchWorkspaces(String query, Long userId, int limit, int offset) {
        String sql = """
            SELECT w.public_id as id,
                   'WORKSPACE' as type,
                   w.name as title,
                   w.description as subtitle,
                   w.created_at as createdAt
            FROM workspaces w
            JOIN workspace_members wm ON w.id = wm.workspace_id
            WHERE MATCH(w.name, w.description) AGAINST (:query IN BOOLEAN MODE)
              AND wm.user_id = :userId
              AND w.deleted_at IS NULL
            ORDER BY w.created_at DESC
            LIMIT :limit OFFSET :offset
        """;

        return jdbcClient.sql(sql)
            .param("query", query + "*")
            .param("userId", userId)
            .param("limit", limit)
            .param("offset", offset)
            .query((rs, rowNum) -> new SearchResultDto(
                rs.getString("id"),
                rs.getString("type"),
                rs.getString("title"),
                rs.getString("subtitle"),
                null,
                null,
                rs.getTimestamp("createdAt").toInstant(),
                Map.of()
            )).list();
    }

    public List<SearchResultDto> searchTasks(String query, String workspacePublicId, Long userId, int limit, int offset) {
        String sql = """
            SELECT t.public_id as id,
                   'TASK' as type,
                   t.title as title,
                   t.description as subtitle,
                   t.created_at as createdAt,
                   w.public_id as workspacePublicId
            FROM tasks t
            JOIN workspaces w ON t.workspace_id = w.id
            JOIN workspace_members wm ON w.id = wm.workspace_id
            WHERE MATCH(t.title, t.description) AGAINST (:query IN BOOLEAN MODE)
              AND wm.user_id = :userId
              AND t.deleted_at IS NULL
              AND (:workspaceId IS NULL OR w.public_id = :workspaceId)
            ORDER BY t.created_at DESC
            LIMIT :limit OFFSET :offset
        """;

        return jdbcClient.sql(sql)
            .param("query", query + "*")
            .param("userId", userId)
            .param("workspaceId", workspacePublicId)
            .param("limit", limit)
            .param("offset", offset)
            .query((rs, rowNum) -> new SearchResultDto(
                rs.getString("id"),
                rs.getString("type"),
                rs.getString("title"),
                rs.getString("subtitle"),
                null,
                null,
                rs.getTimestamp("createdAt").toInstant(),
                Map.of("workspaceId", rs.getString("workspacePublicId"))
            )).list();
    }
}
