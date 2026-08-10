package com.relay.modules.tenant.service;

import com.relay.common.util.SlugUtils;
import com.relay.modules.directory.domain.OrgRole;
import com.relay.modules.directory.domain.OrganizationMember;
import com.relay.modules.directory.domain.WorkspaceMember;
import com.relay.modules.directory.domain.WorkspaceRole;
import com.relay.modules.directory.repository.OrganizationMemberRepository;
import com.relay.modules.directory.repository.WorkspaceMemberRepository;
import com.relay.modules.identity.domain.User;
import com.relay.modules.tenant.domain.Organization;
import com.relay.modules.tenant.domain.Workspace;
import com.relay.modules.tenant.repository.OrganizationRepository;
import com.relay.modules.tenant.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkspaceBootstrapService {

    private final OrganizationRepository organizationRepository;
    private final WorkspaceRepository workspaceRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    @Transactional
    public Workspace bootstrapForNewUser(User user) {
        String orgName = user.getDisplayName() + "'s Organization";
        String orgSlug = resolveUniqueOrgSlug(user.getDisplayName());

        Organization organization = organizationRepository.save(Organization.create(orgName, orgSlug));
        Workspace workspace = workspaceRepository.save(
                Workspace.create(organization, "General", resolveUniqueWorkspaceSlug(organization.getId()))
        );

        organizationMemberRepository.save(OrganizationMember.create(organization, user, OrgRole.OWNER));
        workspaceMemberRepository.save(WorkspaceMember.create(workspace, user, WorkspaceRole.OWNER));

        return workspace;
    }

    private String resolveUniqueOrgSlug(String base) {
        String slug = SlugUtils.slugify(base);
        if (!organizationRepository.existsBySlug(slug)) {
            return slug;
        }
        return slug + "-" + SlugUtils.uniqueSuffix();
    }

    private String resolveUniqueWorkspaceSlug(Long organizationId) {
        String slug = "general";
        if (!workspaceRepository.existsByOrganizationIdAndSlug(organizationId, slug)) {
            return slug;
        }
        return slug + "-" + SlugUtils.uniqueSuffix();
    }

    @Transactional(readOnly = true)
    public java.util.Optional<Workspace> findDefaultWorkspaceForUser(User user) {
        return workspaceMemberRepository.findDefaultWorkspaceForUser(user.getId());
    }
}
