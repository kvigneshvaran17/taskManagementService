package org.example.task.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Optional;

public interface TaskRepository {
    Task save(Task task);
    Optional<Task> findById(String id);
    boolean existsById(String id);
    void deleteById(String id);
    Collection<Task> findAll();
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
}
