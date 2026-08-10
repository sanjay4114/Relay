package com.relay.modules.messaging.repository;

import com.relay.modules.messaging.domain.ChannelMember;
import com.relay.modules.messaging.domain.ChannelMemberId;
import com.relay.modules.messaging.domain.ChannelRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChannelMemberRepository extends JpaRepository<ChannelMember, ChannelMemberId> {

    @Query("SELECT cm FROM ChannelMember cm JOIN FETCH cm.user WHERE cm.channel.id = :channelId")
    List<ChannelMember> findAllByChannelIdWithUser(@Param("channelId") Long channelId);

    Optional<ChannelMember> findByChannelIdAndUserId(Long channelId, Long userId);
    
    boolean existsByChannelIdAndUserId(Long channelId, Long userId);
}
