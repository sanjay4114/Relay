package com.relay.modules.tenant.repository;

import com.relay.modules.tenant.domain.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    boolean existsByOrganizationIdAndSlug(Long organizationId, String slug);

    Optional<Workspace> findByPublicId(String publicId);
}
