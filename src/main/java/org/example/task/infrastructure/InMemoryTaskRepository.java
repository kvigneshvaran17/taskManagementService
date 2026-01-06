package org.example.task.infrastructure;

import org.example.task.domain.Task;
import org.example.task.domain.TaskRepository;
import org.example.task.domain.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryTaskRepository implements TaskRepository {
    private final ConcurrentHashMap<String, Task> store = new ConcurrentHashMap<>();

    @Override
    public Task save(Task task) {
        store.put(task.getId(), task);
        return task;
    }

    @Override
    public Optional<Task> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public boolean existsById(String id) {
        return store.containsKey(id);
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }

    @Override
    public Collection<Task> findAll() {
        return store.values();
    }

    @Override
    public Page<Task> findByStatus(TaskStatus status, Pageable pageable) {
        List<Task> filteredTasks = store.values().stream()
                .filter(task -> task.getStatus() == status)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredTasks.size());
        
        if (start > filteredTasks.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, filteredTasks.size());
        }
        
        return new PageImpl<>(
                filteredTasks.subList(start, end),
                pageable,
                filteredTasks.size()
        );
    }
}
