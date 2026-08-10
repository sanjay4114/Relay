package com.relay.modules.messaging.repository;

import com.relay.modules.messaging.domain.MessageMention;
import com.relay.modules.messaging.domain.MessageMentionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageMentionRepository extends JpaRepository<MessageMention, MessageMentionId> {
}
