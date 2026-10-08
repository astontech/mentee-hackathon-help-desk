# Help Desk

The support team's help desk API: tickets, the agents who work them, and (soon) everything that routes work between them. Spring Boot, Postgres, Gradle.

The work you're doing on it is described in [`docs/brief.md`](docs/brief.md). Start there.

## Run it

You need Java 25 and Docker running.

```sh
./gradlew bootRun
```

Spring Boot starts Postgres from `compose.yaml` by itself, on host port **5433** so it doesn't clash with another Postgres on 5432. The app runs on http://localhost:8080. When you stop the app, the database container stops too.

Your data survives restarts: it lives in a Docker volume. Only `docker compose down -v` deletes it.

## Start clean

```sh
./scripts/reset-data.sh
```

This puts the database back to its starting state: no tickets, only the seed agents, with IDs starting from 1 again. It empties every table, including ones you add. Run it while the app is running (the database has to be up). On Windows, run it from Git Bash.

## Test it

```sh
./gradlew test
```

The tests start their own throwaway Postgres container, so they never touch your local data. CI runs the same build on every pull request, and a pull request can't merge until it passes.

If the tests fail to start a container on a Mac using Colima, run `export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock` first.

## What's here

| Request | What it does |
|---|---|
| `POST /tickets` | Creates a ticket. Body: `{"title": string, "description": string, "priority": "LOW" \| "NORMAL" \| "HIGH" \| "URGENT"}`. Title and priority are required. Returns 201 with a `Location` header. |
| `GET /tickets/{id}` | Reads one ticket. 404 if it doesn't exist. |
| `GET /agents` | Lists the support agents. |

The agents come from `src/main/resources/data.sql`, which runs on every startup and leaves existing agents alone. There's no endpoint to add or change them.

Code is grouped by layer under `src/main/java/com/example/helpdesk`: `controller`, `service`, `domain`, `repo` and `dto`.

## Conventions

- **Errors are ProblemDetail bodies** ([RFC 9457](https://www.rfc-editor.org/rfc/rfc9457)), built in one place: `GlobalControllerAdvice`. Controllers throw; they never build error responses. Validation errors list each bad field under `errors`.
- **Requests and responses are records** in `dto`, separate from the database entities in `domain`.
- **Business rules live in services**, not controllers.

These are the existing team's conventions. You can change any of them if you have a reason. Say why in the pull request.

## Working together

- `main` is protected. Every change goes through a pull request that both teammates approve. A new push resets the approvals.
- Every pull request closes a GitHub issue: write `Closes #12` in its description.
- Product answers go in `docs/product-answers.md`, the agreed acceptance criteria in `docs/acceptance-criteria.md`, and the team's design in `docs/design.md`.
