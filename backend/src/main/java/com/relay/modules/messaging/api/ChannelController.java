package com.relay.modules.messaging.api;

import com.relay.common.dto.ApiResponse;
import com.relay.config.security.AuthenticatedUser;
import com.relay.modules.messaging.api.dto.ChannelDto;
import com.relay.modules.messaging.api.dto.ChannelMemberDto;
import com.relay.modules.messaging.api.dto.CreateChannelRequest;
import com.relay.modules.messaging.api.dto.UpdateChannelRequest;
import com.relay.modules.messaging.service.ChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @GetMapping
    public ApiResponse<List<ChannelDto>> listWorkspaceChannels(
            @PathVariable String workspaceId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(channelService.listWorkspaceChannels(workspaceId, user.userId()));
    }

    @GetMapping("/{channelId}")
    public ApiResponse<ChannelDto> getChannelDetails(
            @PathVariable String workspaceId,
            @PathVariable String channelId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(channelService.getChannelDetails(workspaceId, channelId, user.userId()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ChannelDto> createChannel(
            @PathVariable String workspaceId,
            @Valid @RequestBody CreateChannelRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(channelService.createChannel(workspaceId, request, user), "Channel created successfully");
    }

    @PatchMapping("/{channelId}")
    public ApiResponse<ChannelDto> updateChannel(
            @PathVariable String workspaceId,
            @PathVariable String channelId,
            @Valid @RequestBody UpdateChannelRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(channelService.updateChannel(workspaceId, channelId, request, user.userId()), "Channel updated");
    }

    @PostMapping("/{channelId}/archive")
    public ApiResponse<Void> archiveChannel(
            @PathVariable String workspaceId,
            @PathVariable String channelId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        channelService.archiveChannel(workspaceId, channelId, user.userId());
        return ApiResponse.ok(null, "Channel archived");
    }

    @PostMapping("/{channelId}/restore")
    public ApiResponse<Void> restoreChannel(
            @PathVariable String workspaceId,
            @PathVariable String channelId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        channelService.restoreChannel(workspaceId, channelId, user.userId());
        return ApiResponse.ok(null, "Channel restored");
    }

    @DeleteMapping("/{channelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChannel(
            @PathVariable String workspaceId,
            @PathVariable String channelId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        channelService.deleteChannel(workspaceId, channelId, user.userId());
    }

    @PostMapping("/{channelId}/join")
    public ApiResponse<Void> joinChannel(
            @PathVariable String workspaceId,
            @PathVariable String channelId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        channelService.joinChannel(workspaceId, channelId, user.userId());
        return ApiResponse.ok(null, "Joined channel");
    }

    @PostMapping("/{channelId}/leave")
    public ApiResponse<Void> leaveChannel(
            @PathVariable String workspaceId,
            @PathVariable String channelId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        channelService.leaveChannel(workspaceId, channelId, user.userId());
        return ApiResponse.ok(null, "Left channel");
    }

    @GetMapping("/{channelId}/members")
    public ApiResponse<List<ChannelMemberDto>> listChannelMembers(
            @PathVariable String workspaceId,
            @PathVariable String channelId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(channelService.listChannelMembers(workspaceId, channelId, user.userId()));
    }

    @PostMapping("/{channelId}/members/invite")
    public ApiResponse<Void> inviteUser(
            @PathVariable String workspaceId,
            @PathVariable String channelId,
            @Valid @RequestBody com.relay.modules.messaging.api.dto.InviteUserRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        channelService.inviteUser(workspaceId, channelId, request.userPublicId(), user.userId());
        return ApiResponse.ok(null, "User invited");
    }

    @DeleteMapping("/{channelId}/members/{userPublicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUser(
            @PathVariable String workspaceId,
            @PathVariable String channelId,
            @PathVariable String userPublicId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        channelService.removeUser(workspaceId, channelId, userPublicId, user.userId());
    }
}
