# Spring Boot Testing Tutorial: JUnit, Integration Testing, Testcontainers, and Debugging

Repo: https://github.com/chakricodesjava/springboot-test.git

This tutorial walks through the project structure, REST API, testing strategy (unit, slice, and integration tests), Testcontainers setup for PostgreSQL, JaCoCo coverage, Jenkins CI, and practical debugging tips.

---

## Quickstart

- Requirements
  - Java 17, Maven Wrapper included
  - Docker (for integration tests running Testcontainers)

- Build app
  ```bash
  ./mvnw -q clean package
  ```

- Run app (H2 in-memory)
  ```bash
  ./mvnw spring-boot:run
  ```

- Run all tests (unit + IT)
  ```bash
  ./mvnw -q clean verify
  ```

- Run only unit tests (Surefire)
  ```bash
  ./mvnw -q clean verify -DskipITs=true
  ```

- Run only integration tests (Failsafe, requires Docker)
  ```bash
  ./mvnw -q clean verify -DskipTests=true
  ```

- Open coverage report
  - Generated at: `target/site/jacoco/index.html` (after `verify`)

---

## Architecture at a Glance

- Package root: `com.example`
- Entry point: `TodoTestingDemoApplication`
- Domain: `Task` (@Entity)
  - id: Long (IDENTITY)
  - title: String (not null)
  - completed: boolean (default false)
  - createdAt: LocalDateTime (set in `@PrePersist` if null)
- Repository: `TaskRepository extends JpaRepository<Task, Long>`
  - Custom: `List<Task> findByCompleted(boolean completed)`
- Service: `TaskService`
  - `getAll()`, `getById(id)`, `add(title)`, `toggleComplete(id)`, `delete(id)`, `listByCompleted(boolean)`
  - Throws `NotFoundException` (annotated with `@ResponseStatus(404)`) when id missing
- Controller: `TaskController`
  - Base path: `/api/tasks`
  - Endpoints: list, get by id, create, toggle, delete
  - Request DTO: `CreateTaskRequest { title }`
- Config: `application.properties` uses H2 (dev/runtime); tests tune JPA settings

---

## REST API

Base path: `/api/tasks`

- GET `/api/tasks`
  - 200 OK → `List<Task>`
- GET `/api/tasks/{id}`
  - 200 OK → `Task`
  - 404 Not Found → when missing (via `NotFoundException`)
- POST `/api/tasks`
  - Body: `{ "title": "Write docs" }`
  - 201 Created → `Task` (completed=false by default)
- PUT `/api/tasks/{id}/toggle`
  - 200 OK → `Task` with flipped `completed`
  - 404 Not Found → when missing
- DELETE `/api/tasks/{id}`
  - 204 No Content

Example cURL
```bash
# Create
curl -s -X POST http://localhost:8080/api/tasks \
  -H 'Content-Type: application/json' \
  -d '{"title":"Write docs"}'

# List
curl -s http://localhost:8080/api/tasks | jq .

# Get by id
curl -s http://localhost:8080/api/tasks/1 | jq .

# Toggle
curl -s -X PUT http://localhost:8080/api/tasks/1/toggle | jq .

# Delete
curl -i -X DELETE http://localhost:8080/api/tasks/1
```

---

## JUnit Testing Strategy (Unit, Slice, Integration)

This project demonstrates three levels of tests using JUnit 5 and Spring Test.

1) Unit tests (fast, isolated)
- Frameworks: JUnit 5 + Mockito
- Goal: Verify business logic and controller behavior in isolation
- Examples:
  - `TaskServiceTest` (@ExtendWith(MockitoExtension.class))
    - Mocks `TaskRepository`, verifies behavior (add, toggle, get, delete, listByCompleted)
    - Asserts `NotFoundException` when entity missing
  - `TaskControllerTest` (@WebMvcTest)
    - Mocks `TaskService` via `@MockitoBean`
    - Uses `MockMvc` to assert status codes and JSON payloads
  - Model/DTO tests: `TaskTest`, `CreateTaskRequestTest`

2) Slice tests (data layer)
- `TaskRepositoryTest` (@DataJpaTest)
  - Starts a JPA slice with embedded H2
  - Verifies `findByCompleted` query

3) Integration tests (end-to-end with real Postgres)
- Frameworks: Spring Boot Test + Testcontainers
- Config: `TestcontainersConfiguration` provides `@ServiceConnection PostgreSQLContainer` (postgres:15-alpine)
- Examples:
  - `TaskRepositoryIT` (@DataJpaTest + Replace.NONE + @Import(TestcontainersConfiguration))
  - `TaskServiceIT` (@SpringBootTest + Replace.NONE + @Import(TestcontainersConfiguration))
  - `TaskControllerIT` (@SpringBootTest(webEnvironment=RANDOM_PORT) + @AutoConfigureMockMvc + @Import(TestcontainersConfiguration))

Key Annotations and Patterns
- `@SpringBootTest` → boots app context (optionally web env RANDOM_PORT)
- `@AutoConfigureMockMvc` → injects `MockMvc` for controller testing
- `@DataJpaTest` → JPA slice; add `@AutoConfigureTestDatabase(replace = NONE)` to use Testcontainers/Postgres
- `@WebMvcTest(Controller.class)` → MVC slice for controller + Jackson only; mock collaborators
- Mockito
  - `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`
  - `when(...).thenReturn(...)`, `given(...).willReturn(...)`, `verify(...)`
- Assertions
  - AssertJ is used across tests for fluent assertions (`assertThat`, `assertThatThrownBy`)

---

## Testcontainers: Setup and Tips

Configuration
- `TestcontainersConfiguration` bean:
  - `@TestConfiguration` + `@ServiceConnection` to auto-wire datasource
  - Uses `postgres:15-alpine`
  - Waits for listening port and log message "database system is ready to accept connections"
  - Username: `testuser`, Password: `testpass`, DB: `tempdb`

How tests use it
- Replace embedded DB with containerized Postgres:
  - Add `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)`
  - Import the configuration: `@Import(TestcontainersConfiguration.class)`

Common issues and debugging
- Docker not available or permission denied
  - Ensure Docker Desktop is running on macOS
  - Verify with:
    ```bash
    docker version
    docker ps
    ```
- Slow startup time / timeouts
  - Increase startup timeout in the wait strategy (already set to 120s)
  - Run tests verbosely to see container logs
- Inspect container logs
  - Testcontainers prints container logs on failure; you can also enable more logging:
    ```bash
    export TESTCONTAINERS_LOG_LEVEL=DEBUG
    ./mvnw -q -DtrimStackTrace=false -Dtest=**/*IT test
    ```
- Reuse containers locally to speed up runs (opt-in)
  - Create `~/.testcontainers.properties` with:
    ```
    testcontainers.reuse.enable=true
    ```
  - Then annotate containers with `.withReuse(true)` if you manage them manually (not needed with `@ServiceConnection`).

---

## Maven: Surefire, Failsafe, and Coverage

- Unit tests (Surefire)
  - Excludes `**/*IT.java`
  - Reports: `target/surefire-reports`
- Integration tests (Failsafe)
  - Includes `**/*IT.java`
  - Runs in the `integration-test` and `verify` phases
  - Reports: `target/failsafe-reports`
- Coverage (JaCoCo)
  - Enforced in `verify` with 100% line coverage per class
  - Excludes only `com.example.TodoTestingDemoApplication`
  - Report: `target/site/jacoco/index.html`

Handy commands
```bash
# Fast feedback: unit tests only
./mvnw -q clean verify -DskipITs=true

# IT only (requires Docker)
./mvnw -q clean verify -DskipTests=true

# What failed?
ls -1 target/surefire-reports
ls -1 target/failsafe-reports

# Open coverage (macOS)
open target/site/jacoco/index.html || true
```

---

## Debugging: App and Tests (IntelliJ IDEA)

Debug app
- Run configuration: `TodoTestingDemoApplication` (Spring Boot)
- Set breakpoints in controller/service/repository
- Use HTTP client/cURL to hit endpoints; inspect variables in the debugger

Debug unit tests
- Right-click a test class/method → Debug
- Mockito debugging
  - Use `ArgumentCaptor` to inspect values passed to collaborators
  - If a stub isn’t hit, check argument matchers and method overloads

Debug integration tests
- Ensure Docker is running
- Add breakpoints across layers; run the IT in Debug mode
- Increase logs for SQL/JPA in `src/test/resources/application.properties` if needed
  ```properties
  logging.level.org.hibernate.SQL=debug
  logging.level.org.hibernate.type.descriptor.sql=trace
  ```
- Examine container logs in test output on failures

General tips
- Flaky tests usually come from data ordering assumptions or missing `@Transactional`/cleanup; prefer explicit expectations and fresh DB state per test
- For endpoint tests, assert both status and relevant JSON fields
- For service tests, assert both state and interactions (`verify(...)` calls)

---

## Jenkins CI

Two pipelines separate fast unit tests from slower integration tests.

- `Jenkinsfile.unit`
  - Any agent with JDK 17
  - Runs: `./mvnw -B -q clean verify -DskipITs=true`
  - Publishes Surefire reports and archives artifacts

- `Jenkinsfile.it`
  - Agent labeled `docker` (Docker available)
  - Verifies Docker with `docker version`
  - Runs: `./mvnw -B -q clean verify -DskipTests=true -Dit.test="com.example.it.**.*IT"`
  - Publishes Failsafe reports

Best practices
- Keep unit pipeline as the default gate for PRs
- Trigger IT pipeline nightly or on demand to save CI time

---

## Notes and Possible Enhancements

- Validation: Add `@NotBlank` to `CreateTaskRequest.title` and a `@ControllerAdvice` to return 400 with error details
- API: Add `/api/tasks?completed=true|false` to expose `listByCompleted`
- Sorting/Pagination: Return `findAll(Sort.by("id"))` or `Page<Task>`
- Migrations: Introduce Flyway for schema versioning
- OpenAPI: Add springdoc-openapi for interactive docs

---

## Project Structure (key paths)

- `src/main/java/com/example`
  - `TodoTestingDemoApplication.java`
  - `exception/NotFoundException.java`
  - `model/Task.java`
  - `repository/TaskRepository.java`
  - `service/TaskService.java`
  - `web/TaskController.java`
  - `web/dto/CreateTaskRequest.java`
- `src/main/resources/application.properties`
- `src/test/java/com/example`
  - `unit/` (service, repository, controller, model, dto)
  - `it/` (repository, service, web + Testcontainers config)
- `src/test/resources/application.properties`
- `pom.xml` (Surefire/Failsafe/JaCoCo setup)
- `Jenkinsfile.unit`, `Jenkinsfile.it`

---

## Appendix: What this tutorial covers

- JUnit 5 patterns for unit, slice, and integration tests
- Spring Test annotations (`@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`)
- Mockito usage and verification patterns
- Testcontainers with `@ServiceConnection` for seamless datasource wiring
- Debugging approaches in IntelliJ for both unit and integration tests
- CI separation of concerns (unit vs. integration)

If you follow the commands and patterns above, you’ll be able to: run fast unit tests locally, run reliable integration tests against real Postgres, understand and enforce coverage, and debug across layers efficiently.

