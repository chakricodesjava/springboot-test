package com.example.unit.service;

import com.example.exception.NotFoundException;
import com.example.model.Task;
import com.example.repository.TaskRepository;
import com.example.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TaskRepository repository;

    @InjectMocks
    TaskService service;

    @Test
    @DisplayName("add() should save a new Task with given title")
    void add_shouldSaveTask() {
        when(repository.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            t.setId(1L);
            t.setCreatedAt(LocalDateTime.now());
            return t;
        });

        Task saved = service.add("Learn Mockito");

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(repository).save(captor.capture());
        Task toSave = captor.getValue();

        assertThat(toSave.getTitle()).isEqualTo("Learn Mockito");
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.isCompleted()).isFalse();
    }

    @Test
    @DisplayName("toggleComplete() should flip the completed flag")
    void toggleComplete_shouldFlip() {
        Task existing = new Task("Demo");
        existing.setId(42L);
        existing.setCompleted(false);
        existing.setCreatedAt(LocalDateTime.now());
        when(repository.findById(42L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task updated = service.toggleComplete(42L);

        assertThat(updated.isCompleted()).isTrue();
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("getById() should throw NotFoundException when missing")
    void getById_shouldThrowNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("getById() should return entity when present")
    void getById_shouldReturnEntity() {
        Task t = new Task("A");
        t.setId(5L);
        when(repository.findById(5L)).thenReturn(Optional.of(t));

        Task found = service.getById(5L);

        assertThat(found).isSameAs(t);
    }

    @Test
    @DisplayName("getAll() should return repository.findAll() result")
    void getAll_shouldDelegate() {
        Task a = new Task("A");
            a.setId(1L);
            a.setCreatedAt(LocalDateTime.now());
        Task b = new Task("B");
            b.setCreatedAt(LocalDateTime.now());
            b.setId(2L);
        when(repository.findAll()).thenReturn(List.of(a, b));

        List<Task> all = service.getAll();

        assertThat(all).containsExactly(a, b);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("delete() should load entity and delete it")
    void delete_shouldDeleteLoadedEntity() {
        Task t = new Task("X");
        t.setId(7L);
        when(repository.findById(7L)).thenReturn(Optional.of(t));
        doNothing().when(repository).delete(t);

        service.delete(7L);

        verify(repository).delete(t);
    }

    @Test
    @DisplayName("listByCompleted() should delegate to repository")
    void listByCompleted_shouldDelegate() {
        Task done = new Task("Done");
        done.setCompleted(true);
        when(repository.findByCompleted(true)).thenReturn(List.of(done));

        List<Task> result = service.listByCompleted(true);

        assertThat(result).containsExactly(done);
        verify(repository).findByCompleted(true);
    }
}
