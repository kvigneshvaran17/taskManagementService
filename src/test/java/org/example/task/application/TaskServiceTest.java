package org.example.task.application;

import org.example.task.domain.Task;
import org.example.task.domain.TaskNotFoundException;
import org.example.task.domain.TaskRepository;
import org.example.task.domain.TaskStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Test
    void createTask_defaultsStatusToPending_whenNull() {
        TaskRepository repo = mock(TaskRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        TaskService service = new TaskService(repo, clock);

        when(repo.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task created = service.createTask("t", null, null, LocalDate.parse("2026-01-02"));

        assertNotNull(created.getId());
        assertEquals(TaskStatus.PENDING, created.getStatus());
    }

    @Test
    void getTask_throwsNotFound() {
        TaskRepository repo = mock(TaskRepository.class);
        TaskService service = new TaskService(repo, Clock.systemUTC());

        when(repo.findById("x")).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> service.getTask("x"));
    }

    @Test
    void updateTask_appliesOnlyProvidedFields() {
        TaskRepository repo = mock(TaskRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        TaskService service = new TaskService(repo, clock);

        Task existing = new Task("1", "old", "d", TaskStatus.PENDING, LocalDate.parse("2026-01-10"));
        when(repo.findById("1")).thenReturn(Optional.of(existing));
        when(repo.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task updated = service.updateTask("1", "new", null, TaskStatus.DONE, null);

        assertEquals("new", updated.getTitle());
        assertEquals("d", updated.getDescription());
        assertEquals(TaskStatus.DONE, updated.getStatus());
        assertEquals(LocalDate.parse("2026-01-10"), updated.getDueDate());

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(repo).save(captor.capture());
        assertEquals("1", captor.getValue().getId());
    }

    @Test
    void listTasks_sortsByDueDate_andCanFilterByStatus() {
        TaskRepository repo = mock(TaskRepository.class);
        TaskService service = new TaskService(repo, Clock.systemUTC());

        Task t1 = new Task("1", "a", null, TaskStatus.PENDING, LocalDate.parse("2026-01-10"));
        Task t2 = new Task("2", "b", null, TaskStatus.DONE, LocalDate.parse("2026-01-05"));
        Task t3 = new Task("3", "c", null, TaskStatus.PENDING, LocalDate.parse("2026-01-07"));

        when(repo.findAll()).thenReturn(List.of(t1, t2, t3));
        when(repo.findByStatus(any(), any())).thenAnswer(invocation -> {
            TaskStatus status = invocation.getArgument(0);
            List<Task> filtered = List.of(t1, t2, t3).stream()
                    .filter(t -> t.getStatus() == status)
                    .collect(Collectors.toList());
            return new PageImpl<>(filtered);
        });

        // Test with all tasks (status = null)
        Page<Task> allPage = service.listTasks(null, 0, 10, Sort.by("dueDate"));
        List<Task> allTasks = allPage.getContent();
        assertEquals(List.of("2", "3", "1"), allTasks.stream().map(Task::getId).toList());

        // Test with filtered tasks (status = PENDING)
        Page<Task> pendingPage = service.listTasks(TaskStatus.PENDING, 0, 10, Sort.by("dueDate"));
        List<Task> pendingTasks = pendingPage.getContent();
        assertEquals(List.of("3", "1"), pendingTasks.stream().map(Task::getId).toList());
    }

    @Test
    void createTask_rejectsNonFutureDueDate() {
        TaskRepository repo = mock(TaskRepository.class);
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        TaskService service = new TaskService(repo, clock);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createTask("t", null, TaskStatus.PENDING, LocalDate.parse("2026-01-01")));

        assertTrue(ex.getMessage().contains("future"));
    }
}
