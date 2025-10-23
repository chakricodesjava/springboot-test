package com.example.unit.web.dto;

import com.example.web.dto.CreateTaskRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateTaskRequestTest {

    @Test
    void gettersAndSetters_work() {
        CreateTaskRequest req = new CreateTaskRequest();
        req.setTitle("Hello");
        assertThat(req.getTitle()).isEqualTo("Hello");

        CreateTaskRequest req2 = new CreateTaskRequest("World");
        assertThat(req2.getTitle()).isEqualTo("World");
    }
}

