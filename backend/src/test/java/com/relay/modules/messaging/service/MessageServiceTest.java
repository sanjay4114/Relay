package com.relay.modules.messaging.service;

import com.relay.common.exception.RelayException;
import com.relay.modules.identity.domain.User;
import com.relay.modules.identity.repository.UserRepository;
import com.relay.modules.messaging.api.dto.MessageDto;
import com.relay.modules.messaging.api.dto.SendMessageRequest;
import com.relay.modules.messaging.domain.Channel;
import com.relay.modules.messaging.domain.Message;
import com.relay.modules.messaging.domain.MessageType;
import com.relay.modules.messaging.repository.ChannelMemberRepository;
import com.relay.modules.messaging.repository.ChannelRepository;
import com.relay.modules.messaging.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock private MessageRepository messageRepository;
    @Mock private ChannelRepository channelRepository;
    @Mock private ChannelMemberRepository channelMemberRepository;
    @Mock private UserRepository userRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private MessageService messageService;

    private User sender;
    private Channel channel;
    private Message message;

    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setId(1L);
        sender.setPublicId("user-1");
        sender.setDisplayName("Test User");

        channel = new Channel();
        channel.setId(1L);
        channel.setPublicId("channel-1");

        message = new Message();
        message.setId(1L);
        message.setPublicId("msg-1");
        message.setChannel(channel);
        message.setSender(sender);
        message.setContent("Hello World");
        message.setMessageType(MessageType.TEXT);
        message.setCreatedAt(Instant.now());
        message.setUpdatedAt(Instant.now());
        message.setReplyCount(0);
    }

    @Test
    void sendMessage_ShouldSaveAndBroadcast() {
        SendMessageRequest request = new SendMessageRequest("Hello World", null, null);
        
        when(channelRepository.findByPublicId("channel-1")).thenReturn(Optional.of(channel));
        when(channelMemberRepository.existsByChannelIdAndUserId(1L, 1L)).thenReturn(true);
        when(userRepository.getReferenceById(1L)).thenReturn(sender);
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        MessageDto result = messageService.sendMessage("channel-1", request, 1L);

        assertNotNull(result);
        assertEquals("Hello World", result.content());
        verify(messageRepository).save(any(Message.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/channels.channel-1.messages"), any(Object.class));
    }

    @Test
    void sendMessage_WhenNotMember_ShouldThrowException() {
        SendMessageRequest request = new SendMessageRequest("Hello World", null, null);
        
        when(channelRepository.findByPublicId("channel-1")).thenReturn(Optional.of(channel));
        when(channelMemberRepository.existsByChannelIdAndUserId(1L, 1L)).thenReturn(false);

        assertThrows(RelayException.class, () -> {
            messageService.sendMessage("channel-1", request, 1L);
        });
        
        verify(messageRepository, never()).save(any(Message.class));
    }
}
