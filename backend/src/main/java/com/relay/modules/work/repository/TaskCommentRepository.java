package com.relay.modules.work.repository;

import com.relay.modules.work.domain.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {

    Optional<TaskComment> findByPublicIdAndDeletedAtIsNull(String publicId);

    @Query("SELECT tc FROM TaskComment tc JOIN FETCH tc.author WHERE tc.task.id = :taskId AND tc.deletedAt IS NULL ORDER BY tc.createdAt ASC")
    List<TaskComment> findAllByTaskIdOrderByCreatedAtAsc(@Param("taskId") Long taskId);
}
