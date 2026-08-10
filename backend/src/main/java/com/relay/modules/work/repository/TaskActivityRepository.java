package com.relay.modules.work.repository;

import com.relay.modules.work.domain.TaskActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskActivityRepository extends JpaRepository<TaskActivity, Long> {

    @Query("SELECT ta FROM TaskActivity ta JOIN FETCH ta.actor WHERE ta.task.id = :taskId ORDER BY ta.createdAt DESC")
    List<TaskActivity> findAllByTaskIdOrderByCreatedAtDesc(@Param("taskId") Long taskId);
}
