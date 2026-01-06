package org.example.task.domain;

import java.time.LocalDate;
import java.util.Objects;

public class Task {
    private final String id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDate dueDate;

    public Task(String id, String title, String description, TaskStatus status, LocalDate dueDate) {
        this.id = Objects.requireNonNull(id, "id");
        this.title = Objects.requireNonNull(title, "title");
        this.description = description;
        this.status = status == null ? TaskStatus.PENDING : status;
        this.dueDate = Objects.requireNonNull(dueDate, "dueDate");
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = Objects.requireNonNull(title, "title");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status == null ? TaskStatus.PENDING : status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = Objects.requireNonNull(dueDate, "dueDate");
    }
}
