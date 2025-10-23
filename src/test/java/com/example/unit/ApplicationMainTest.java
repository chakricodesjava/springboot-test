package com.example.unit;

import com.example.TodoTestingDemoApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationMainTest {

    @Test
    void applicationContext_startsAndCloses() {
        String prev = System.getProperty("server.port");
        System.setProperty("server.port", "0");
        try (ConfigurableApplicationContext ctx = SpringApplication.run(TodoTestingDemoApplication.class)) {
            assertThat(ctx).isNotNull();
            assertThat(ctx.isActive()).isTrue();
        } finally {
            if (prev != null) System.setProperty("server.port", prev); else System.clearProperty("server.port");
        }
    }
}
