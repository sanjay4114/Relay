package com.relay.modules.identity.service;

import com.relay.modules.identity.api.dto.UserDto;
import com.relay.modules.identity.api.dto.WorkspaceSummaryDto;
import com.relay.modules.identity.domain.User;
import com.relay.modules.tenant.domain.Workspace;

final class AuthMapper {

    private AuthMapper() {
    }

    static UserDto toUserDto(User user) {
        return new UserDto(
                user.getPublicId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getStatusMessage(),
                user.getTimezone()
        );
    }

    static WorkspaceSummaryDto toWorkspaceDto(Workspace workspace) {
        return new WorkspaceSummaryDto(
                workspace.getPublicId(),
                workspace.getName(),
                workspace.getSlug()
        );
    }
}
