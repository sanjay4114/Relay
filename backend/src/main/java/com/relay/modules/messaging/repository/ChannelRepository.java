package com.relay.modules.messaging.repository;

import com.relay.modules.messaging.domain.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChannelRepository extends JpaRepository<Channel, Long> {

    Optional<Channel> findByPublicId(String publicId);

    List<Channel> findAllByWorkspaceId(Long workspaceId);

    boolean existsByNameAndWorkspaceId(String name, Long workspaceId);

    boolean existsBySlugAndWorkspaceId(String slug, Long workspaceId);
}
