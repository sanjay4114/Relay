package com.relay.modules.messaging.repository;

import com.relay.modules.messaging.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m JOIN FETCH m.sender JOIN FETCH m.channel LEFT JOIN FETCH m.parentMessage WHERE m.publicId = :publicId")
    Optional<Message> findByPublicId(@Param("publicId") String publicId);

    // Cursor-based pagination for main channel (exclude replies)
    @Query("SELECT m FROM Message m JOIN FETCH m.sender JOIN FETCH m.channel LEFT JOIN FETCH m.parentMessage WHERE m.channel.id = :channelId AND m.parentMessage IS NULL AND m.createdAt < :cursor ORDER BY m.createdAt DESC")
    Page<Message> findByChannelIdAndParentMessageIsNullAndCreatedAtBeforeOrderByCreatedAtDesc(
            @Param("channelId") Long channelId, 
            @Param("cursor") Instant cursor, 
            Pageable pageable);

    // Get latest messages for main channel (exclude replies)
    @Query("SELECT m FROM Message m JOIN FETCH m.sender JOIN FETCH m.channel LEFT JOIN FETCH m.parentMessage WHERE m.channel.id = :channelId AND m.parentMessage IS NULL ORDER BY m.createdAt DESC")
    Page<Message> findByChannelIdAndParentMessageIsNullOrderByCreatedAtDesc(
            @Param("channelId") Long channelId, 
            Pageable pageable);

    // Cursor-based pagination for thread messages (replies to a specific parent)
    @Query("SELECT m FROM Message m JOIN FETCH m.sender JOIN FETCH m.channel LEFT JOIN FETCH m.parentMessage WHERE m.parentMessage.id = :parentId AND m.createdAt < :cursor ORDER BY m.createdAt DESC")
    Page<Message> findByParentMessageIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            @Param("parentId") Long parentId, 
            @Param("cursor") Instant cursor, 
            Pageable pageable);

    // Get latest messages for thread
    @Query("SELECT m FROM Message m JOIN FETCH m.sender JOIN FETCH m.channel LEFT JOIN FETCH m.parentMessage WHERE m.parentMessage.id = :parentId ORDER BY m.createdAt DESC")
    Page<Message> findByParentMessageIdOrderByCreatedAtDesc(
            @Param("parentId") Long parentId, 
            Pageable pageable);

    @Query("SELECT m FROM Message m JOIN FETCH m.sender JOIN FETCH m.channel LEFT JOIN FETCH m.parentMessage WHERE m.channel.id = :channelId AND m.pinnedAt IS NOT NULL ORDER BY m.pinnedAt DESC")
    List<Message> findByChannelIdAndPinnedAtIsNotNullOrderByPinnedAtDesc(@Param("channelId") Long channelId);

    int countByChannelIdAndCreatedAtAfter(Long channelId, java.time.Instant createdAt);
}
