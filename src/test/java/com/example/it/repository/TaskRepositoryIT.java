package com.example.it.repository;

import com.example.it.config.TestcontainersConfiguration;
import com.example.model.Task;
import com.example.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class TaskRepositoryIT {

    @Autowired
    TaskRepository repository;

    @Test
    void findByCompleted_shouldReturnFiltered() {
        repository.save(new Task("A")); // completed false
        Task done = new Task("B");
        done.setCompleted(true);
        repository.save(done);

        List<Task> incomplete = repository.findByCompleted(false);
        List<Task> complete = repository.findByCompleted(true);

        assertThat(incomplete).extracting(Task::getTitle).containsExactly("A");
        assertThat(complete).extracting(Task::getTitle).containsExactly("B");
    }
}
