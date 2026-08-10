package com.relay.modules.tenant.service;

import com.relay.common.exception.RelayException;
import com.relay.common.util.SlugUtils;
import com.relay.config.security.AuthenticatedUser;
import com.relay.modules.directory.domain.WorkspaceMember;
import com.relay.modules.directory.domain.WorkspaceRole;
import com.relay.modules.directory.repository.WorkspaceMemberRepository;
import com.relay.modules.identity.domain.User;
import com.relay.modules.identity.repository.UserRepository;
import com.relay.modules.tenant.api.dto.CreateWorkspaceRequest;
import com.relay.modules.tenant.api.dto.UpdateWorkspaceRequest;
import com.relay.modules.tenant.api.dto.WorkspaceDto;
import com.relay.modules.tenant.domain.Organization;
import com.relay.modules.tenant.domain.Workspace;
import com.relay.modules.tenant.repository.OrganizationRepository;
import com.relay.modules.tenant.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public List<WorkspaceDto> listUserWorkspaces(Long userId) {
        return workspaceMemberRepository.findAllByUserId(userId).stream()
                .filter(wm -> !wm.getWorkspace().isDeleted())
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkspaceDto getWorkspace(String publicId, Long userId) {
        WorkspaceMember member = validateAccess(publicId, userId);
        return mapToDto(member);
    }

    @Transactional
    public WorkspaceDto createWorkspace(CreateWorkspaceRequest request, AuthenticatedUser userDetails) {
        User user = userRepository.findById(userDetails.userId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Use the first organization the user belongs to (simplification for personal workspaces)
        // In a real multi-tenant setup, the Org ID would be passed.
        // For Relay, we assume they have a default org created during bootstrap.
        Organization organization = organizationRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("No organization found"));

        String slug = request.slug() != null && !request.slug().isBlank() 
                ? SlugUtils.slugify(request.slug()) 
                : resolveUniqueWorkspaceSlug(organization.getId(), request.name());

        Workspace workspace = Workspace.create(organization, request.name(), slug);
        if (request.description() != null && !request.description().isBlank()) {
            workspace.setDescription(request.description());
        }
        workspace = workspaceRepository.save(workspace);

        WorkspaceMember member = WorkspaceMember.create(workspace, user, WorkspaceRole.OWNER);
        workspaceMemberRepository.save(member);

        return mapToDto(member);
    }

    @Transactional
    public WorkspaceDto updateWorkspace(String publicId, UpdateWorkspaceRequest request, Long userId) {
        WorkspaceMember member = validateAccess(publicId, userId);

        if (member.getWorkspaceRole() != WorkspaceRole.OWNER && member.getWorkspaceRole() != WorkspaceRole.ADMIN) {
            throw new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only Admins and Owners can update workspaces.");
        }

        Workspace workspace = member.getWorkspace();
        workspace.setName(request.name());
        workspace.setSlug(SlugUtils.slugify(request.slug()));
        workspace.setDescription(request.description());
        workspaceRepository.save(workspace);

        return mapToDto(member);
    }

    @Transactional
    public void deleteWorkspace(String publicId, Long userId) {
        WorkspaceMember member = validateAccess(publicId, userId);

        if (member.getWorkspaceRole() != WorkspaceRole.OWNER) {
            throw new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only Owners can delete workspaces.");
        }

        Workspace workspace = member.getWorkspace();
        workspace.setDeletedAt(Instant.now());
        workspace.setDeletedBy(userId);
        workspaceRepository.save(workspace);
    }

    private WorkspaceMember validateAccess(String workspacePublicId, Long userId) {
        Workspace workspace = workspaceRepository.findByPublicId(workspacePublicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Workspace not found"));

        if (workspace.isDeleted()) {
            throw new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Workspace not found");
        }

        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspace.getId(), userId)
                .orElseThrow(() -> new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied"));
    }

    private String resolveUniqueWorkspaceSlug(Long organizationId, String name) {
        String base = SlugUtils.slugify(name);
        if (!workspaceRepository.existsByOrganizationIdAndSlug(organizationId, base)) {
            return base;
        }
        return base + "-" + SlugUtils.uniqueSuffix();
    }

    private WorkspaceDto mapToDto(WorkspaceMember member) {
        Workspace ws = member.getWorkspace();
        return new WorkspaceDto(
                ws.getPublicId(),
                ws.getName(),
                ws.getSlug(),
                ws.getDescription(),
                member.getWorkspaceRole().name(),
                ws.getCreatedAt(),
                ws.getUpdatedAt()
        );
    }
}
