package com.relay.modules.directory.service;

import com.relay.common.exception.RelayException;
import com.relay.modules.directory.api.dto.InviteMemberRequest;
import com.relay.modules.directory.api.dto.WorkspaceMemberDto;
import com.relay.modules.directory.domain.WorkspaceMember;
import com.relay.modules.directory.domain.WorkspaceRole;
import com.relay.modules.directory.repository.WorkspaceMemberRepository;
import com.relay.modules.identity.domain.User;
import com.relay.modules.identity.repository.UserRepository;
import com.relay.modules.tenant.domain.Workspace;
import com.relay.modules.tenant.repository.WorkspaceRepository;
import com.relay.modules.notification.domain.NotificationType;
import com.relay.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceMemberService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<WorkspaceMemberDto> listMembers(String workspaceId, Long currentUserId) {
        Workspace workspace = getWorkspaceAndValidateAccess(workspaceId, currentUserId);
        return workspaceMemberRepository.findAllByWorkspaceId(workspace.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void inviteMember(String workspaceId, InviteMemberRequest request, Long currentUserId) {
        WorkspaceMember currentUserMember = getMemberAndValidateAdminAccess(workspaceId, currentUserId);
        
        User invitee = userRepository.findByEmailAndDeletedAtIsNull(request.email().toLowerCase().trim())
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found with provided email"));

        if (workspaceMemberRepository.findByWorkspaceIdAndUserId(currentUserMember.getWorkspace().getId(), invitee.getId()).isPresent()) {
            throw new RelayException(HttpStatus.CONFLICT, "ALREADY_MEMBER", "User is already a member of this workspace");
        }

        WorkspaceRole roleToAssign = WorkspaceRole.valueOf(request.role());
        WorkspaceMember newMember = WorkspaceMember.create(currentUserMember.getWorkspace(), invitee, roleToAssign);
        workspaceMemberRepository.save(newMember);
        
        notificationService.createNotification(
            invitee.getId(),
            currentUserId,
            NotificationType.WORKSPACE_INVITE,
            "Workspace Invitation",
            "You have been invited to join " + currentUserMember.getWorkspace().getName(),
            "WORKSPACE",
            currentUserMember.getWorkspace().getPublicId()
        );
        // Note: For a real app, you'd send an email invite or create an Invitation record. 
        // Here, we instantly add them to the workspace per direct invite semantics.
    }

    @Transactional
    public void removeMember(String workspaceId, String userPublicId, Long currentUserId) {
        WorkspaceMember currentUserMember = getMemberAndValidateAdminAccess(workspaceId, currentUserId);
        
        WorkspaceMember targetMember = workspaceMemberRepository.findByWorkspacePublicIdAndUserPublicId(workspaceId, userPublicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "Member not found in workspace"));

        if (targetMember.getWorkspaceRole() == WorkspaceRole.OWNER) {
            throw new RelayException(HttpStatus.FORBIDDEN, "CANNOT_REMOVE_OWNER", "Cannot remove the workspace owner");
        }

        workspaceMemberRepository.delete(targetMember);
    }

    @Transactional
    public void leaveWorkspace(String workspaceId, Long currentUserId) {
        WorkspaceMember currentUserMember = getMemberAndValidateAccess(workspaceId, currentUserId);

        if (currentUserMember.getWorkspaceRole() == WorkspaceRole.OWNER) {
            long ownerCount = workspaceMemberRepository.countByWorkspaceIdAndWorkspaceRole(currentUserMember.getWorkspace().getId(), WorkspaceRole.OWNER);
            if (ownerCount <= 1) {
                throw new RelayException(HttpStatus.BAD_REQUEST, "OWNER_CANNOT_LEAVE", "Owner cannot leave the workspace without transferring ownership first");
            }
        }

        workspaceMemberRepository.delete(currentUserMember);
    }

    @Transactional
    public void transferOwnership(String workspaceId, String newOwnerPublicId, Long currentUserId) {
        WorkspaceMember currentUserMember = getMemberAndValidateAccess(workspaceId, currentUserId);

        if (currentUserMember.getWorkspaceRole() != WorkspaceRole.OWNER) {
            throw new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only the owner can transfer ownership");
        }

        if (currentUserMember.getUser().getPublicId().equals(newOwnerPublicId)) {
            throw new RelayException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "You already own this workspace");
        }

        WorkspaceMember targetMember = workspaceMemberRepository.findByWorkspacePublicIdAndUserPublicId(workspaceId, newOwnerPublicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "Target member not found in workspace"));

        targetMember.setWorkspaceRole(WorkspaceRole.OWNER);
        currentUserMember.setWorkspaceRole(WorkspaceRole.ADMIN);

        workspaceMemberRepository.save(targetMember);
        workspaceMemberRepository.save(currentUserMember);
    }

    private WorkspaceMember getMemberAndValidateAccess(String workspacePublicId, Long userId) {
        Workspace workspace = workspaceRepository.findByPublicId(workspacePublicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Workspace not found"));

        if (workspace.isDeleted()) {
            throw new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Workspace not found");
        }

        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspace.getId(), userId)
                .orElseThrow(() -> new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied"));
    }

    private WorkspaceMember getMemberAndValidateAdminAccess(String workspacePublicId, Long userId) {
        WorkspaceMember member = getMemberAndValidateAccess(workspacePublicId, userId);
        if (member.getWorkspaceRole() != WorkspaceRole.OWNER && member.getWorkspaceRole() != WorkspaceRole.ADMIN) {
            throw new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only Admins and Owners can perform this action");
        }
        return member;
    }

    private Workspace getWorkspaceAndValidateAccess(String workspacePublicId, Long userId) {
        return getMemberAndValidateAccess(workspacePublicId, userId).getWorkspace();
    }

    private WorkspaceMemberDto mapToDto(WorkspaceMember member) {
        User u = member.getUser();
        return new WorkspaceMemberDto(
                u.getPublicId(),
                u.getEmail(),
                u.getDisplayName(),
                u.getAvatarUrl(),
                member.getWorkspaceRole().name(),
                member.getJoinedAt()
        );
    }
}
