package org.example.task.application;

import org.example.task.domain.Task;
import org.example.task.domain.TaskNotFoundException;
import org.example.task.domain.TaskRepository;
import org.example.task.domain.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final Clock clock;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this(taskRepository, Clock.systemDefaultZone());
    }

    TaskService(TaskRepository taskRepository, Clock clock) {
        this.taskRepository = taskRepository;
        this.clock = clock;
    }

    public Task createTask(String title, String description, TaskStatus status, LocalDate dueDate) {
        validateDueDateInFuture(dueDate);

        String id = UUID.randomUUID().toString();
        Task task = new Task(id, title, description, status, dueDate);
        return taskRepository.save(task);
    }

    public Task getTask(String id) {
        return taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }

    public Task updateTask(String id, String title, String description, TaskStatus status, LocalDate dueDate) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));

        if (title != null) {
            task.setTitle(title);
        }
        if (description != null) {
            task.setDescription(description);
        }
        if (status != null) {
            task.setStatus(status);
        }
        if (dueDate != null) {
            validateDueDateInFuture(dueDate);
            task.setDueDate(dueDate);
        }

        return taskRepository.save(task);
    }

    public void deleteTask(String id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }

    public Page<Task> listTasks(TaskStatus status, int page, int size, Sort sort) {
        Pageable pageable = PageRequest.of(page, size, sort);
        
        List<Task> tasks;
        if (status != null) {
            // Get filtered tasks and sort them
            tasks = new ArrayList<>(taskRepository.findAll())
                    .stream()
                    .filter(task -> task.getStatus() == status)
                    .sorted(Comparator.comparing(Task::getDueDate))
                    .collect(Collectors.toList());
        } else {
            // Get all tasks and sort them
            tasks = new ArrayList<>(taskRepository.findAll());
            tasks.sort(Comparator.comparing(Task::getDueDate));
        }
        
        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), tasks.size());
        
        if (start > tasks.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, tasks.size());
        }
        
        return new PageImpl<>(
                tasks.subList(start, end),
                pageable,
                tasks.size()
        );
    }

    private void validateDueDateInFuture(LocalDate dueDate) {
        Objects.requireNonNull(dueDate, "dueDate");
        LocalDate today = LocalDate.now(clock);
        if (!dueDate.isAfter(today)) {
            throw new IllegalArgumentException("due_date must be a valid date in the future");
        }
    }

    public Page<Task> getAllTasks(TaskStatus status, Pageable pageable) {
        // Get all tasks and apply status filter if provided
        List<Task> filteredTasks = taskRepository.findAll().stream()
                .filter(task -> status == null || task.getStatus() == status)
                .collect(Collectors.toList());

        // Get the sort parameters
        Sort sort = pageable.getSort();
        List<Sort.Order> orders = sort.toList();

        // Apply sorting
        List<Task> sortedTasks = filteredTasks.stream()
                .sorted((t1, t2) -> {
                    if (orders.isEmpty()) {
                        return 0; // No sorting
                    }

                    for (Sort.Order order : orders) {
                        int result = 0;
                        switch (order.getProperty().toLowerCase()) {
                            case "title":
                                result = t1.getTitle().compareTo(t2.getTitle());
                                break;
                            case "duedate":
                                result = t1.getDueDate().compareTo(t2.getDueDate());
                                break;
                            case "status":
                                result = t1.getStatus().compareTo(t2.getStatus());
                                break;
                            default:
                                result = 0;
                        }
                        if (result != 0) {
                            return order.isAscending() ? result : -result;
                        }
                    }
                    return 0;
                })
                .collect(Collectors.toList());

        // Apply pagination
        int total = sortedTasks.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), total);

        if (start > total) {
            return new PageImpl<>(List.of(), pageable, total);
        }

        List<Task> content = sortedTasks.subList(start, end);

        return new PageImpl<>(content, pageable, total);
    }
}
