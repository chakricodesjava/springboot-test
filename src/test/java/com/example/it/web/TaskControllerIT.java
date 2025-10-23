package com.example.it.web;

import com.example.it.config.TestcontainersConfiguration;
import com.example.model.Task;
import com.example.service.TaskService;
import com.example.web.dto.CreateTaskRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class TaskControllerIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TaskService service;

    @Test
    void list_shouldReturnTasks() throws Exception {
        service.add("A");
        service.add("B");
//        service.add("C");

        mvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("A")))
                .andExpect(jsonPath("$[1].title", is("B")));
    }

    @Test
    void create_shouldReturn201AndBody() throws Exception {
        CreateTaskRequest req = new CreateTaskRequest("Create endpoint");

        mvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Create endpoint")))
                .andExpect(jsonPath("$.completed", is(false)));
    }

    @Test
    void toggle_shouldReturnUpdatedTask() throws Exception {
        Task existing = service.add("Toggle me");
        Long id = existing.getId();

        mvc.perform(put("/api/tasks/" + id + "/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed", is(true)));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        Task existing = service.add("Delete me");
        Long id = existing.getId();

        mvc.perform(delete("/api/tasks/" + id))
                .andExpect(status().isNoContent());
    }
}
