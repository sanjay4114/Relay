package com.relay.modules.directory.repository;

import com.relay.modules.directory.domain.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {
}
