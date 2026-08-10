package com.relay.modules.directory.api;

import com.relay.common.dto.ApiResponse;
import com.relay.config.security.AuthenticatedUser;
import com.relay.modules.directory.api.dto.InviteMemberRequest;
import com.relay.modules.directory.api.dto.TransferOwnershipRequest;
import com.relay.modules.directory.api.dto.WorkspaceMemberDto;
import com.relay.modules.directory.service.WorkspaceMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/members")
@RequiredArgsConstructor
public class WorkspaceMemberController {

    private final WorkspaceMemberService workspaceMemberService;

    @GetMapping
    public ApiResponse<List<WorkspaceMemberDto>> listMembers(
            @PathVariable String workspaceId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ApiResponse.ok(workspaceMemberService.listMembers(workspaceId, user.userId()));
    }

    @PostMapping("/invite")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> inviteMember(
            @PathVariable String workspaceId,
            @Valid @RequestBody InviteMemberRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        workspaceMemberService.inviteMember(workspaceId, request, user.userId());
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{userPublicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(
            @PathVariable String workspaceId,
            @PathVariable String userPublicId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        workspaceMemberService.removeMember(workspaceId, userPublicId, user.userId());
    }

    @PostMapping("/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveWorkspace(
            @PathVariable String workspaceId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        workspaceMemberService.leaveWorkspace(workspaceId, user.userId());
    }

    @PostMapping("/transfer-ownership")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transferOwnership(
            @PathVariable String workspaceId,
            @Valid @RequestBody TransferOwnershipRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        workspaceMemberService.transferOwnership(workspaceId, request.newOwnerPublicId(), user.userId());
    }
}
