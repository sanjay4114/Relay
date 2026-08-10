package com.relay.modules.tenant.repository;

import com.relay.modules.tenant.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    boolean existsBySlug(String slug);

    Optional<Organization> findByPublicId(String publicId);
}
