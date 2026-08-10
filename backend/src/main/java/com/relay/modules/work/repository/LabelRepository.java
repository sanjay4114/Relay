package com.relay.modules.work.repository;

import com.relay.modules.work.domain.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LabelRepository extends JpaRepository<Label, Long> {
    
    List<Label> findAllByWorkspaceId(Long workspaceId);
    
    Optional<Label> findByWorkspaceIdAndName(Long workspaceId, String name);
}
