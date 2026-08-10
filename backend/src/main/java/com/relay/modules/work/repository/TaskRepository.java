package com.relay.modules.work.repository;

import com.relay.modules.work.domain.Task;
import com.relay.modules.work.domain.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.creator WHERE t.publicId = :publicId AND t.deletedAt IS NULL")
    Optional<Task> findByPublicIdWithDetails(@Param("publicId") String publicId);
    
    Optional<Task> findByPublicIdAndDeletedAtIsNull(String publicId);

    @Query(value = "SELECT t FROM Task t LEFT JOIN FETCH t.creator WHERE t.workspace.id = :workspaceId AND t.deletedAt IS NULL",
           countQuery = "SELECT count(t) FROM Task t WHERE t.workspace.id = :workspaceId AND t.deletedAt IS NULL")
    Page<Task> findAllByWorkspaceId(@Param("workspaceId") Long workspaceId, Pageable pageable);

    @Query(value = "SELECT t FROM Task t LEFT JOIN FETCH t.creator WHERE t.workspace.id = :workspaceId AND t.status = :status AND t.deletedAt IS NULL",
           countQuery = "SELECT count(t) FROM Task t WHERE t.workspace.id = :workspaceId AND t.status = :status AND t.deletedAt IS NULL")
    Page<Task> findAllByWorkspaceIdAndStatus(@Param("workspaceId") Long workspaceId, @Param("status") TaskStatus status, Pageable pageable);
}
