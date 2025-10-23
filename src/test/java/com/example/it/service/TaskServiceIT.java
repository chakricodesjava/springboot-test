package com.example.it.service;

import com.example.it.config.TestcontainersConfiguration;
import com.example.exception.NotFoundException;
import com.example.model.Task;
import com.example.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class TaskServiceIT {

    @Autowired
    TaskService service;

    @Test
    @DisplayName("add() should save a new Task with given title")
    void add_shouldSaveTask() {
        Task saved = service.add("Learn Mockito");

        assertThat(saved.getTitle()).isEqualTo("Learn Mockito");
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.isCompleted()).isFalse();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("toggleComplete() should flip the completed flag")
    void toggleComplete_shouldFlip() {
        Task existing = service.add("Demo");
        Long id = existing.getId();

        Task updated = service.toggleComplete(id);

        assertThat(updated.isCompleted()).isTrue();
        assertThat(updated.getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("getById() should throw NotFoundException when missing")
    void getById_shouldThrowNotFound() {
        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }
}
