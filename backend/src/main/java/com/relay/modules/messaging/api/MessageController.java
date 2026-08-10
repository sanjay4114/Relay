package com.relay.modules.messaging.api;

import com.relay.common.dto.ApiResponse;
import com.relay.config.security.AuthenticatedUser;
import com.relay.modules.messaging.api.dto.MessageDto;
import com.relay.modules.messaging.api.dto.SendMessageRequest;
import com.relay.modules.messaging.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import com.relay.modules.messaging.api.dto.EditMessageRequest;
import java.security.Principal;
import java.time.Instant;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/channels/{channelId}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public ApiResponse<List<MessageDto>> getChannelMessages(
            @PathVariable String channelId,
            @RequestParam(required = false) Instant cursor,
            @RequestParam(defaultValue = "50") int limit,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(messageService.getChannelMessages(channelId, cursor, limit, user.userId()));
    }

    @GetMapping("/{messageId}/replies")
    public ApiResponse<List<MessageDto>> getThreadMessages(
            @PathVariable String channelId,
            @PathVariable String messageId,
            @RequestParam(required = false) Instant cursor,
            @RequestParam(defaultValue = "50") int limit,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(messageService.getThreadMessages(channelId, messageId, cursor, limit, user.userId()));
    }

    @GetMapping("/pinned")
    public ApiResponse<List<MessageDto>> getPinnedMessages(
            @PathVariable String channelId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(messageService.getPinnedMessages(channelId, user.userId()));
    }

    @GetMapping("/saved")
    public ApiResponse<List<MessageDto>> getSavedMessages(
            @PathVariable String channelId,
            @RequestParam(required = false) Instant cursor,
            @RequestParam(defaultValue = "50") int limit,
            @AuthenticationPrincipal AuthenticatedUser user) {
        // Technically saved messages are per user, channelId can be optional or ignored.
        // Let's pass it to a service method that fetches all saved messages for the user.
        return ApiResponse.ok(messageService.getSavedMessages(user.userId(), cursor, limit));
    }

    @MessageMapping("/channels.{channelId}.send")
    public void handleSendMessage(
            @DestinationVariable String channelId,
            Principal principal,
            @Valid @Payload SendMessageRequest request) {

        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            if (auth.getPrincipal() instanceof AuthenticatedUser user) {
                messageService.sendMessage(channelId, request, user.userId());
            }
        }
    }

    @PatchMapping("/{messageId}")
    public ApiResponse<MessageDto> editMessage(
            @PathVariable String channelId,
            @PathVariable String messageId,
            @Valid @RequestBody EditMessageRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(messageService.editMessage(channelId, messageId, request, user.userId()), "Message edited");
    }

    @DeleteMapping("/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(
            @PathVariable String channelId,
            @PathVariable String messageId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        messageService.deleteMessage(channelId, messageId, user.userId());
    }

    @PostMapping("/{messageId}/restore")
    public ApiResponse<Void> restoreMessage(
            @PathVariable String channelId,
            @PathVariable String messageId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        messageService.restoreMessage(channelId, messageId, user.userId());
        return ApiResponse.ok(null, "Message restored");
    }

    @PostMapping("/{messageId}/reactions")
    public ApiResponse<Void> addReaction(
            @PathVariable String channelId,
            @PathVariable String messageId,
            @RequestParam String emoji,
            @AuthenticationPrincipal AuthenticatedUser user) {
        messageService.addReaction(channelId, messageId, emoji, user.userId());
        return ApiResponse.ok(null, "Reaction added");
    }

    @DeleteMapping("/{messageId}/reactions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeReaction(
            @PathVariable String channelId,
            @PathVariable String messageId,
            @RequestParam String emoji,
            @AuthenticationPrincipal AuthenticatedUser user) {
        messageService.removeReaction(channelId, messageId, emoji, user.userId());
    }

    @PostMapping("/{messageId}/pin")
    public ApiResponse<Void> pinMessage(
            @PathVariable String channelId,
            @PathVariable String messageId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        messageService.pinMessage(channelId, messageId, user.userId());
        return ApiResponse.ok(null, "Message pinned");
    }

    @DeleteMapping("/{messageId}/pin")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unpinMessage(
            @PathVariable String channelId,
            @PathVariable String messageId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        messageService.unpinMessage(channelId, messageId, user.userId());
    }

    @PostMapping("/{messageId}/save")
    public ApiResponse<Void> saveMessage(
            @PathVariable String channelId,
            @PathVariable String messageId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        messageService.saveMessage(channelId, messageId, user.userId());
        return ApiResponse.ok(null, "Message saved");
    }

    @DeleteMapping("/{messageId}/save")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsaveMessage(
            @PathVariable String channelId,
            @PathVariable String messageId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        messageService.unsaveMessage(channelId, messageId, user.userId());
    }

    @PostMapping("/{messageId}/delivered")
    public ApiResponse<Void> markMessageDelivered(
            @PathVariable String channelId,
            @PathVariable String messageId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        messageService.markMessageDelivered(channelId, messageId, user.userId());
        return ApiResponse.ok(null, "Message delivered");
    }

    @PostMapping("/{messageId}/read")
    public ApiResponse<Void> markMessageRead(
            @PathVariable String channelId,
            @PathVariable String messageId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        messageService.markMessageRead(channelId, messageId, user.userId());
        return ApiResponse.ok(null, "Message read");
    }

    @PostMapping("/read-all")
    public ApiResponse<Void> markChannelRead(
            @PathVariable String channelId,
            @RequestParam String lastMessageId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        messageService.markChannelRead(channelId, lastMessageId, user.userId());
        return ApiResponse.ok(null, "Channel read");
    }

    @GetMapping("/unread-count")
    public ApiResponse<Integer> getUnreadCount(
            @PathVariable String channelId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(messageService.getUnreadCount(channelId, user.userId()));
    }
}
