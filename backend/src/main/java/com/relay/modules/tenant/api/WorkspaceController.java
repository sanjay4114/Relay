package com.relay.modules.tenant.api;

import com.relay.common.dto.ApiResponse;
import com.relay.config.security.AuthenticatedUser;
import com.relay.modules.tenant.api.dto.CreateWorkspaceRequest;
import com.relay.modules.tenant.api.dto.UpdateWorkspaceRequest;
import com.relay.modules.tenant.api.dto.WorkspaceDto;
import com.relay.modules.tenant.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping
    public ApiResponse<List<WorkspaceDto>> listUserWorkspaces(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(workspaceService.listUserWorkspaces(user.userId()));
    }

    @GetMapping("/{workspaceId}")
    public ApiResponse<WorkspaceDto> getWorkspace(
            @PathVariable String workspaceId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ApiResponse.ok(workspaceService.getWorkspace(workspaceId, user.userId()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WorkspaceDto> createWorkspace(
            @Valid @RequestBody CreateWorkspaceRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ApiResponse.ok(workspaceService.createWorkspace(request, user));
    }

    @PatchMapping("/{workspaceId}")
    public ApiResponse<WorkspaceDto> updateWorkspace(
            @PathVariable String workspaceId,
            @Valid @RequestBody UpdateWorkspaceRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ApiResponse.ok(workspaceService.updateWorkspace(workspaceId, request, user.userId()));
    }

    @DeleteMapping("/{workspaceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkspace(
            @PathVariable String workspaceId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        workspaceService.deleteWorkspace(workspaceId, user.userId());
    }
}
