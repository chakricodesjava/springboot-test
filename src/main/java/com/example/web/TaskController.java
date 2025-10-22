package com.example.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.example.model.Task;
import com.example.service.TaskService;
import com.example.web.dto.CreateTaskRequest;


@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping
    public List<Task> list() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Task get(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task create(@RequestBody CreateTaskRequest request) {
        return service.add(request.getTitle());
    }

    @PutMapping("/{id}/toggle")
    public Task toggle(@PathVariable Long id) {
        return service.toggleComplete(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
