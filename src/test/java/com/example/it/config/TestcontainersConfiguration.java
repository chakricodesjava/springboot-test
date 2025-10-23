package com.example.it.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        WaitAllStrategy waitStrategy = new WaitAllStrategy()
                .withStrategy(Wait.forListeningPort())
                .withStrategy(Wait.forLogMessage(".*database system is ready to accept connections.*", 2))
                .withStartupTimeout(Duration.ofSeconds(120));

        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withUsername("testuser")
                .withPassword("testpass")
                .withDatabaseName("tempdb")
                .waitingFor(waitStrategy);
    }

}
