package com.relay.modules.notification.api;

import com.relay.common.dto.ApiResponse;
import com.relay.config.security.AuthenticatedUser;
import com.relay.modules.notification.api.dto.NotificationDto;
import com.relay.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification Management API")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get notifications", description = "Retrieves a paginated list of notifications for the current user.")
    @GetMapping
    public ApiResponse<List<NotificationDto>> getNotifications(
            @RequestParam(defaultValue = "50") int limit,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(notificationService.getNotifications(user.userId(), limit));
    }

    @Operation(summary = "Get unread count", description = "Retrieves the count of unread notifications for the current user.")
    @GetMapping("/unread-count")
    public ApiResponse<Integer> getUnreadCount(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(notificationService.getUnreadCount(user.userId()));
    }

    @Operation(summary = "Mark notification as read", description = "Marks a specific notification as read.")
    @PostMapping("/{publicId}/read")
    public ApiResponse<Void> markAsRead(
            @PathVariable String publicId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        notificationService.markAsRead(user.userId(), publicId);
        return ApiResponse.ok(null, "Notification marked as read");
    }

    @Operation(summary = "Mark all notifications as read", description = "Marks all unread notifications as read for the current user.")
    @PostMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(@AuthenticationPrincipal AuthenticatedUser user) {
        notificationService.markAllAsRead(user.userId());
        return ApiResponse.ok(null, "All notifications marked as read");
    }

    @Operation(summary = "Delete notification", description = "Deletes a specific notification.")
    @DeleteMapping("/{publicId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotification(
            @PathVariable String publicId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        notificationService.deleteNotification(user.userId(), publicId);
    }
}
