package org.example.task.web;

import jakarta.validation.Valid;
import org.example.task.application.TaskService;
import org.example.task.domain.Task;
import org.example.task.domain.TaskStatus;
import org.example.task.web.dto.CreateTaskRequest;
import org.example.task.web.dto.TaskResponse;
import org.example.task.web.dto.UpdateTaskRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Task created = taskService.createTask(
                request.getTitle(),
                request.getDescription(),
                request.getStatus(),
                request.getDueDate()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(created));
    }

    @GetMapping("/{id}")
    public TaskResponse getTask(@PathVariable String id) {
        return TaskResponse.from(taskService.getTask(id));
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(@PathVariable String id, @Valid @RequestBody UpdateTaskRequest request) {
        Task updated = taskService.updateTask(
                id,
                request.getTitle(),
                request.getDescription(),
                request.getStatus(),
                request.getDueDate()
        );
        return TaskResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
    }

    @GetMapping
    public Page<TaskResponse> getAllTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dueDate,asc") String[] sort) {

        // Convert to 0-based page index for Spring Data
        int pageNumber = page > 0 ? page - 1 : 0;
        
        // Parse sort parameters
        Sort sortBy = Sort.by(
            Arrays.stream(sort)
                .map(s -> s.split(","))
                .map(arr -> {
                    String property = arr[0];
                    Sort.Direction direction = arr.length > 1 && arr[1].equalsIgnoreCase("desc") 
                        ? Sort.Direction.DESC 
                        : Sort.Direction.ASC;
                    return new Sort.Order(direction, property);
                })
                .collect(Collectors.toList())
        );

        Page<Task> tasks = taskService.listTasks(status, pageNumber, size, sortBy);
        
        return tasks.map(TaskResponse::from);
    }
}
