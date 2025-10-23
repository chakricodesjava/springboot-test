package com.example;

import com.example.it.config.TestcontainersConfiguration;
import org.springframework.boot.SpringApplication;

public class TestTodoTestingDemoApplication {

	public static void main(String[] args) {
		SpringApplication.from(TodoTestingDemoApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
