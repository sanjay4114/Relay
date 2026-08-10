package com.relay.modules.work.service;

import com.relay.common.exception.RelayException;
import com.relay.modules.identity.domain.User;
import com.relay.modules.identity.repository.UserRepository;
import com.relay.modules.messaging.domain.Message;
import com.relay.modules.messaging.repository.MessageRepository;
import com.relay.modules.notification.domain.NotificationType;
import com.relay.modules.notification.service.NotificationService;
import com.relay.modules.tenant.domain.Workspace;
import com.relay.modules.tenant.repository.WorkspaceRepository;
import com.relay.modules.directory.repository.WorkspaceMemberRepository;
import com.relay.modules.work.api.dto.CreateTaskRequest;
import com.relay.modules.work.api.dto.LabelDto;
import com.relay.modules.work.api.dto.TaskDto;
import com.relay.modules.work.api.dto.UpdateTaskRequest;
import com.relay.modules.work.domain.*;
import com.relay.modules.work.repository.LabelRepository;
import com.relay.modules.work.repository.TaskActivityRepository;
import com.relay.modules.work.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final MessageRepository messageRepository;
    private final LabelRepository labelRepository;
    private final TaskActivityRepository activityRepository;
    private final NotificationService notificationService;

    @Transactional
    public TaskDto createTask(String workspacePublicId, CreateTaskRequest request, Long creatorId) {
        Workspace workspace = workspaceRepository.findByPublicId(workspacePublicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Workspace not found"));
        
        validateWorkspaceMembership(workspace.getId(), creatorId);
        User creator = userRepository.findById(creatorId).orElseThrow();

        Task task = new Task();
        task.setWorkspace(workspace);
        task.setCreator(creator);
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM);
        task.setStartDate(request.startDate());
        task.setDueDate(request.dueDate());
        task.setEstimate(request.estimate());
        task.setStoryPoints(request.storyPoints());

        if (request.linkedMessagePublicId() != null) {
            Message message = messageRepository.findByPublicId(request.linkedMessagePublicId())
                    .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "MESSAGE_NOT_FOUND", "Message not found"));
            task.setLinkedMessage(message);
        }

        task = taskRepository.save(task);

        if (request.assigneePublicIds() != null) {
            for (String userPublicId : request.assigneePublicIds()) {
                assignUserToTask(task, userPublicId, creator);
            }
        }

        if (request.labelNames() != null) {
            for (String labelName : request.labelNames()) {
                addLabelToTask(task, labelName, creator);
            }
        }

        logActivity(task, creator, TaskActivityType.CREATED, null, null);

        return mapToDto(task);
    }

    @Transactional
    public TaskDto updateTask(String workspacePublicId, String taskPublicId, UpdateTaskRequest request, Long userId) {
        Task task = getTaskAndValidateAccess(workspacePublicId, taskPublicId, userId);
        User actor = userRepository.findById(userId).orElseThrow();

        if (request.title() != null && !request.title().equals(task.getTitle())) {
            task.setTitle(request.title());
            logActivity(task, actor, TaskActivityType.UPDATED, "title", request.title());
        }
        if (request.description() != null && !request.description().equals(task.getDescription())) {
            task.setDescription(request.description());
            logActivity(task, actor, TaskActivityType.UPDATED, "description", "updated");
        }
        if (request.status() != null && task.getStatus() != request.status()) {
            String oldStatus = task.getStatus().name();
            task.setStatus(request.status());
            if (request.status() == TaskStatus.DONE) {
                task.setCompletedAt(Instant.now());
                notifyAssignees(task, actor, "Task Completed", "Task " + task.getPublicId() + " has been marked as DONE.");
            } else {
                task.setCompletedAt(null);
            }
            logActivity(task, actor, TaskActivityType.STATUS_CHANGED, oldStatus, request.status().name());
        }
        if (request.priority() != null && task.getPriority() != request.priority()) {
            String oldPri = task.getPriority().name();
            task.setPriority(request.priority());
            logActivity(task, actor, TaskActivityType.PRIORITY_CHANGED, oldPri, request.priority().name());
        }
        if (request.startDate() != null) task.setStartDate(request.startDate());
        if (request.dueDate() != null) task.setDueDate(request.dueDate());
        if (request.estimate() != null) task.setEstimate(request.estimate());
        if (request.storyPoints() != null) task.setStoryPoints(request.storyPoints());

        taskRepository.save(task);

        if (request.assigneePublicIds() != null) {
            // Very simplified: replace assignees
            task.getAssignees().clear();
            for (String uid : request.assigneePublicIds()) {
                assignUserToTask(task, uid, actor);
            }
        }

        if (request.labelNames() != null) {
            task.getLabels().clear();
            for (String ln : request.labelNames()) {
                addLabelToTask(task, ln, actor);
            }
        }

        return mapToDto(task);
    }

    @Transactional(readOnly = true)
    public Page<TaskDto> listWorkspaceTasks(String workspacePublicId, TaskStatus status, Long userId, Pageable pageable) {
        Workspace workspace = workspaceRepository.findByPublicId(workspacePublicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Workspace not found"));
        validateWorkspaceMembership(workspace.getId(), userId);

        Page<Task> tasks;
        if (status != null) {
            tasks = taskRepository.findAllByWorkspaceIdAndStatus(workspace.getId(), status, pageable);
        } else {
            tasks = taskRepository.findAllByWorkspaceId(workspace.getId(), pageable);
        }

        return tasks.map(this::mapToDto);
    }

    @Transactional
    public void deleteTask(String workspacePublicId, String taskPublicId, Long userId) {
        Task task = getTaskAndValidateAccess(workspacePublicId, taskPublicId, userId);
        task.setDeletedAt(Instant.now());
        taskRepository.save(task);
    }

    private void assignUserToTask(Task task, String userPublicId, User actor) {
        User assignee = userRepository.findByPublicIdAndDeletedAtIsNull(userPublicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Assignee not found"));
        
        validateWorkspaceMembership(task.getWorkspace().getId(), assignee.getId());
        
        if (task.getAssignees().add(assignee)) {
            logActivity(task, actor, TaskActivityType.ASSIGNED, null, assignee.getPublicId());
            if (!assignee.getId().equals(actor.getId())) {
                notificationService.createNotification(
                        assignee.getId(),
                        actor.getId(),
                        NotificationType.TASK_ASSIGNMENT,
                        "Assigned to Task",
                        actor.getDisplayName() + " assigned you to task: " + task.getTitle(),
                        "TASK",
                        task.getPublicId()
                );
            }
        }
    }

    private void addLabelToTask(Task task, String labelName, User actor) {
        Label label = labelRepository.findByWorkspaceIdAndName(task.getWorkspace().getId(), labelName)
                .orElseGet(() -> {
                    Label newLabel = new Label();
                    newLabel.setWorkspace(task.getWorkspace());
                    newLabel.setName(labelName);
                    // generate random color or default
                    newLabel.setColor("#3182CE");
                    return labelRepository.save(newLabel);
                });
        if (task.getLabels().add(label)) {
            logActivity(task, actor, TaskActivityType.LABEL_ADDED, null, labelName);
        }
    }

    private void logActivity(Task task, User actor, TaskActivityType type, String oldValue, String newValue) {
        TaskActivity activity = new TaskActivity();
        activity.setTask(task);
        activity.setActor(actor);
        activity.setActivityType(type);
        activity.setOldValue(oldValue);
        activity.setNewValue(newValue);
        activityRepository.save(activity);
    }

    private void notifyAssignees(Task task, User actor, String title, String body) {
        for (User assignee : task.getAssignees()) {
            if (!assignee.getId().equals(actor.getId())) {
                notificationService.createNotification(
                        assignee.getId(),
                        actor.getId(),
                        NotificationType.TASK_ASSIGNMENT,
                        title,
                        body,
                        "TASK",
                        task.getPublicId()
                );
            }
        }
    }

    private Task getTaskAndValidateAccess(String workspacePublicId, String taskPublicId, Long userId) {
        Workspace workspace = workspaceRepository.findByPublicId(workspacePublicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Workspace not found"));
        validateWorkspaceMembership(workspace.getId(), userId);

        Task task = taskRepository.findByPublicIdWithDetails(taskPublicId)
                .orElseThrow(() -> new RelayException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Task not found"));

        if (!task.getWorkspace().getId().equals(workspace.getId())) {
            throw new RelayException(HttpStatus.BAD_REQUEST, "BAD_WORKSPACE", "Task does not belong to this workspace");
        }
        return task;
    }

    private void validateWorkspaceMembership(Long workspaceId, Long userId) {
        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new RelayException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Not a member of this workspace");
        }
    }

    private TaskDto mapToDto(Task task) {
        return new TaskDto(
                task.getPublicId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name(),
                task.getPriority().name(),
                task.getStartDate(),
                task.getDueDate(),
                task.getCompletedAt(),
                task.getEstimate(),
                task.getStoryPoints(),
                task.getCreator().getPublicId(),
                task.getLinkedMessage() != null ? task.getLinkedMessage().getPublicId() : null,
                task.getAssignees().stream().map(User::getPublicId).collect(Collectors.toList()),
                task.getLabels().stream().map(l -> new LabelDto(l.getName(), l.getColor())).collect(Collectors.toList()),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
