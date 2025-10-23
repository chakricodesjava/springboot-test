package com.example.unit.web;

import com.example.model.Task;
import com.example.service.TaskService;
import com.example.web.TaskController;
import com.example.web.dto.CreateTaskRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    TaskService service;

    @Test
    void list_shouldReturnTasks() throws Exception {
        Task t1 = new Task(1L, "A", false, LocalDateTime.now());
        Task t2 = new Task(2L, "B", true, LocalDateTime.now());
        given(service.getAll()).willReturn(List.of(t1, t2));

        mvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("A")))
                .andExpect(jsonPath("$[1].completed", is(true)));
    }

    @Test
    void create_shouldReturn201AndBody() throws Exception {
        CreateTaskRequest req = new CreateTaskRequest("Create endpoint");
        Task saved = new Task(10L, "Create endpoint", false, LocalDateTime.now());
        given(service.add(anyString())).willReturn(saved);

        mvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.title", is("Create endpoint")));
    }

    @Test
    void toggle_shouldReturnUpdatedTask() throws Exception {
        Task updated = new Task(5L, "Toggle me", true, LocalDateTime.now());
        given(service.toggleComplete(5L)).willReturn(updated);

        mvc.perform(put("/api/tasks/5/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed", is(true)));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(service).delete(anyLong());

        mvc.perform(delete("/api/tasks/7"))
                .andExpect(status().isNoContent());
    }

    @Test
    void get_shouldReturnTaskById() throws Exception {
        Task t = new Task(3L, "Look up", false, LocalDateTime.now());
        given(service.getById(3L)).willReturn(t);

        mvc.perform(get("/api/tasks/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.title", is("Look up")))
                .andExpect(jsonPath("$.completed", is(false)));
    }

    @Test
    void get_shouldReturn404WhenMissing() throws Exception {
        willThrow(new com.example.exception.NotFoundException("Task not found: 55"))
                .given(service).getById(55L);

        mvc.perform(get("/api/tasks/55"))
                .andExpect(status().isNotFound());
    }
}
