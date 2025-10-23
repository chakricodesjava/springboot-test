package com.example.unit.model;

import com.example.model.Task;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TaskTest {

    @Test
    void gettersSetters_andToString_andEqualsHashCode() {
        LocalDateTime now = LocalDateTime.now();
        Task a = new Task();
        a.setId(1L);
        a.setTitle("Title");
        a.setCompleted(true);
        a.setCreatedAt(now);

        Task b = new Task(1L, "Title", true, now);

        assertThat(a.getId()).isEqualTo(1L);
        assertThat(a.getTitle()).isEqualTo("Title");
        assertThat(a.isCompleted()).isTrue();
        assertThat(a.getCreatedAt()).isEqualTo(now);

        // equals/hashCode
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());

        // toString contains fields
        String ts = a.toString();
        assertThat(ts).contains("id=1").contains("Title").contains("completed=true");
    }

    @Test
    void prePersist_setsCreatedAtWhenNull() {
        Task t = new Task("New");
        assertThat(t.getCreatedAt()).isNull();
        t.prePersist();
        assertThat(t.getCreatedAt()).isNotNull();
    }
}

