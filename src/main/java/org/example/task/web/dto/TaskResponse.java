package org.example.task.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.task.domain.Task;
import org.example.task.domain.TaskStatus;

import java.time.LocalDate;

public class TaskResponse {
    private String id;
    private String title;
    private String description;
    private TaskStatus status;

    @JsonProperty("due_date")
    private LocalDate dueDate;

    public static TaskResponse from(Task task) {
        TaskResponse dto = new TaskResponse();
        dto.id = task.getId();
        dto.title = task.getTitle();
        dto.description = task.getDescription();
        dto.status = task.getStatus();
        dto.dueDate = task.getDueDate();
        return dto;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }
}
