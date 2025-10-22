package com.example.web.dto;


public class CreateTaskRequest {
    private String title;

    public CreateTaskRequest() {
    }

    public CreateTaskRequest(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
