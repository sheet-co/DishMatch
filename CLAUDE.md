# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

DishMatch is a Telegram bot for dish matching ("Tg bot for cute dish matches"), built with Spring Boot 4.1 on Java 25. The project is at an early/skeleton stage: only the Spring Boot entry point (`DishMatchApplication`) and the database schema exist so far — no controllers, services, or Telegram/AI integration code has been added yet.

It uses java 25 and Spring 4, so feel free to use latest language features here. Use var, structured concurrency, virtual threads, records etc.

Package root: `dev.sheet_co.dishMatch`. Build group: `dev.sheet_co`.

## Commands

Build tool is Gradle (Kotlin DSL) via the wrapper — always use `./gradlew`, not a global `gradle`.

```shell
./gradlew build                # compile + test + assemble
./gradlew bootRun              # run the app (requires env vars, see Configuration below)
./gradlew test                 # run all tests
./gradlew test --tests "dev.sheet_co.dishMatch.DishMatchApplicationTests"   # run a single test class
```

### Database (Flyway + Postgres)

The Flyway Gradle plugin targets the local Docker Compose Postgres by default.

```shell
docker compose up -d postgres
./gradlew flywayInfo
./gradlew flywayMigrate
```

Other tasks: `flywayValidate`, `flywayRepair`, `flywayBaseline`, `flywayClean` (list all with `./gradlew tasks --group flyway`).

Override the connection via Gradle properties or environment variables (Gradle properties win):

```shell
./gradlew flywayMigrate \
  -Pflyway.url=jdbc:postgresql://localhost:5432/dish_match_bd \
  -Pflyway.user=your_user \
  -Pflyway.password=your_password \
  -Pflyway.locations=filesystem:src/main/resources/db/migrations \
  -Pflyway.schemas=public
```

Env var equivalents: `FLYWAY_URL`, `FLYWAY_USER`, `FLYWAY_PASSWORD`, `FLYWAY_LOCATIONS`, `FLYWAY_SCHEMAS` (comma-separate multiple locations/schemas). Migrations live in `src/main/resources/db/migrations`, named `V<n>__Description.sql`.

## Configuration

`application.yaml` (default profile) is env-var driven and expects: `GEMINI_KEY`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`. JPA `ddl-auto` is `none` — schema changes go through Flyway migrations only, never Hibernate auto-DDL.

`application-local.yaml` (`local` profile) hardcodes the datasource to the Docker Compose Postgres instance (`dish_match_bd` / `your_user` / `your_password` on `localhost:5432`) for local development.

## Architecture

- **Framework**: Spring Boot 4.1, Java 25 toolchain, Lombok for boilerplate.
- **Persistence**: Spring Data JPA + Postgres, with schema managed exclusively via Flyway migrations (`src/main/resources/db/migrations`).
- **AI**: Spring AI with the Google GenAI (Gemini) starter, configured via `spring.ai.google.genai.api-key`.
- **Telegram**: `telegrambots-client` + `telegrambots-springboot-longpolling-starter` (long-polling bot, not webhook-based).

### Data model (from `V1__Initial_schema.sql`)

- `dishes`: a dish owned by a `user_id` (Telegram user), with `ingredients` and `tags` as Postgres text arrays.
- `history`: records of a `user_id` eating a `dish_id` at `eaten_at`, FK to `dishes` with `ON DELETE CASCADE`.
- Indexes support lookup by `dishes.user_id`, `history.user_id, eaten_at DESC`, and `history.dish_id`.

There is no ORM entity/repository layer yet — code that adds one should follow this schema.
