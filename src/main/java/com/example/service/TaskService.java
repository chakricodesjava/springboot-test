package com.example.service;


import com.example.exception.NotFoundException;
import com.example.model.Task;
import com.example.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public List<Task> getAll() {
        return repository.findAll();
    }

    public Task getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Task not found: " + id));
    }

    public Task add(String title) {
        Task task = new Task(title);
        return repository.save(task);
    }

    public Task toggleComplete(Long id) {
        Task task = getById(id);
        task.setCompleted(!task.isCompleted());
        return repository.save(task);
    }

    public void delete(Long id) {
        Task task = getById(id);
        repository.delete(task);
    }

    public List<Task> listByCompleted(boolean completed) {
        return repository.findByCompleted(completed);
    }
}