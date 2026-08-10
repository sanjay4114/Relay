package com.relay.modules.work.api;

import com.relay.common.dto.ApiResponse;
import com.relay.config.security.AuthenticatedUser;
import com.relay.modules.work.api.dto.CreateTaskRequest;
import com.relay.modules.work.api.dto.TaskDto;
import com.relay.modules.work.api.dto.UpdateTaskRequest;
import com.relay.modules.work.domain.TaskStatus;
import com.relay.modules.work.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks API", description = "Endpoints for managing workspace tasks")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "List tasks", description = "List all tasks in a workspace")
    @GetMapping
    public ApiResponse<Page<TaskDto>> listTasks(
            @PathVariable String workspaceId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(taskService.listWorkspaceTasks(workspaceId, status, user.userId(), PageRequest.of(page, size)));
    }

    @Operation(summary = "Create task", description = "Create a new task in the workspace")
    @PostMapping
    public ApiResponse<TaskDto> createTask(
            @PathVariable String workspaceId,
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(taskService.createTask(workspaceId, request, user.userId()), "Task created successfully");
    }

    @Operation(summary = "Update task", description = "Update an existing task")
    @PatchMapping("/{taskId}")
    public ApiResponse<TaskDto> updateTask(
            @PathVariable String workspaceId,
            @PathVariable String taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok(taskService.updateTask(workspaceId, taskId, request, user.userId()), "Task updated successfully");
    }

    @Operation(summary = "Delete task", description = "Soft delete a task")
    @DeleteMapping("/{taskId}")
    public ApiResponse<Void> deleteTask(
            @PathVariable String workspaceId,
            @PathVariable String taskId,
            @AuthenticationPrincipal AuthenticatedUser user) {
        taskService.deleteTask(workspaceId, taskId, user.userId());
        return ApiResponse.ok(null, "Task deleted successfully");
    }
}
