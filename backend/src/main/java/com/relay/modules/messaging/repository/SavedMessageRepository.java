package com.relay.modules.messaging.repository;

import com.relay.modules.messaging.domain.SavedMessage;
import com.relay.modules.messaging.domain.SavedMessageId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface SavedMessageRepository extends JpaRepository<SavedMessage, SavedMessageId> {

    @Query("SELECT sm FROM SavedMessage sm JOIN FETCH sm.message m JOIN FETCH m.sender JOIN FETCH m.channel WHERE sm.user.id = :userId AND sm.savedAt < :cursor ORDER BY sm.savedAt DESC")
    Page<SavedMessage> findByUserIdAndSavedAtBeforeOrderBySavedAtDesc(
            @Param("userId") Long userId,
            @Param("cursor") Instant cursor,
            Pageable pageable);

    @Query("SELECT sm FROM SavedMessage sm JOIN FETCH sm.message m JOIN FETCH m.sender JOIN FETCH m.channel WHERE sm.user.id = :userId ORDER BY sm.savedAt DESC")
    Page<SavedMessage> findByUserIdOrderBySavedAtDesc(
            @Param("userId") Long userId,
            Pageable pageable);
}
