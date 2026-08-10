package com.relay.modules.work.service;

import com.relay.common.exception.RelayException;
import com.relay.modules.identity.domain.User;
import com.relay.modules.identity.repository.UserRepository;
import com.relay.modules.messaging.repository.MessageRepository;
import com.relay.modules.notification.service.NotificationService;
import com.relay.modules.tenant.domain.Workspace;
import com.relay.modules.tenant.repository.WorkspaceRepository;
import com.relay.modules.directory.repository.WorkspaceMemberRepository;
import com.relay.modules.work.api.dto.CreateTaskRequest;
import com.relay.modules.work.api.dto.TaskDto;
import com.relay.modules.work.domain.Task;
import com.relay.modules.work.domain.TaskPriority;
import com.relay.modules.work.domain.TaskStatus;
import com.relay.modules.work.repository.LabelRepository;
import com.relay.modules.work.repository.TaskActivityRepository;
import com.relay.modules.work.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @Mock private WorkspaceRepository workspaceRepository;
    @Mock private WorkspaceMemberRepository workspaceMemberRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private LabelRepository labelRepository;
    @Mock private TaskActivityRepository activityRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private TaskService taskService;

    private Workspace workspace;
    private User user;

    @BeforeEach
    void setUp() {
        workspace = new Workspace();
        workspace.setId(1L);
        workspace.setPublicId(UUID.randomUUID().toString());

        user = new User();
        user.setId(100L);
        user.setPublicId(UUID.randomUUID().toString());
        user.setDisplayName("Test User");
    }

    @Test
    void createTask_Success() {
        // Arrange
        CreateTaskRequest request = new CreateTaskRequest("Fix Bug", "Description", TaskPriority.HIGH, null, null, null, null, null, null, null);
        
        when(workspaceRepository.findByPublicId(workspace.getPublicId())).thenReturn(Optional.of(workspace));
        when(workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspace.getId(), user.getId())).thenReturn(true);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        
        Task savedTask = new Task();
        savedTask.setPublicId(UUID.randomUUID().toString());
        savedTask.setTitle("Fix Bug");
        savedTask.setDescription("Description");
        savedTask.setPriority(TaskPriority.HIGH);
        savedTask.setStatus(TaskStatus.TODO);
        savedTask.setCreator(user);
        savedTask.setWorkspace(workspace);
        
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        // Act
        TaskDto result = taskService.createTask(workspace.getPublicId(), request, user.getId());

        // Assert
        assertNotNull(result);
        assertEquals("Fix Bug", result.title());
        assertEquals("HIGH", result.priority());
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(activityRepository, times(1)).save(any());
    }

    @Test
    void createTask_ThrowsForbidden_WhenNotMember() {
        // Arrange
        CreateTaskRequest request = new CreateTaskRequest("Fix Bug", null, null, null, null, null, null, null, null, null);
        
        when(workspaceRepository.findByPublicId(workspace.getPublicId())).thenReturn(Optional.of(workspace));
        when(workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspace.getId(), user.getId())).thenReturn(false);
        
        // Act & Assert
        RelayException ex = assertThrows(RelayException.class, () -> 
            taskService.createTask(workspace.getPublicId(), request, user.getId())
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }
}
