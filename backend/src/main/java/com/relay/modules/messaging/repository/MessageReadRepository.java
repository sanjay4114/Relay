package com.relay.modules.messaging.repository;

import com.relay.modules.messaging.domain.MessageRead;
import com.relay.modules.messaging.domain.MessageReadId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageReadRepository extends JpaRepository<MessageRead, MessageReadId> {
}
