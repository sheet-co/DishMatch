# DishMatch
Tg bot for cute dish matches

## Database migrations

The Flyway Gradle plugin uses the local Docker Compose database by default. Start PostgreSQL and inspect or apply migrations with:

```shell
docker compose up -d postgres
./gradlew flywayInfo
./gradlew flywayMigrate
```

Other useful tasks are `flywayValidate`, `flywayRepair`, `flywayBaseline`, and `flywayClean`. Run `./gradlew tasks --group flyway` to list all available Flyway tasks.

Override the connection through Gradle properties (similar to Maven's `-D` properties):

```shell
./gradlew flywayMigrate \
  -Pflyway.url=jdbc:postgresql://localhost:5432/dish_match_bd \
  -Pflyway.user=your_user \
  -Pflyway.password=your_password \
  -Pflyway.locations=filesystem:src/main/resources/db/migrations \
  -Pflyway.schemas=public
```

The equivalent environment variables are `FLYWAY_URL`, `FLYWAY_USER`, `FLYWAY_PASSWORD`, `FLYWAY_LOCATIONS`, and `FLYWAY_SCHEMAS`. Comma-separate multiple locations or schemas. Gradle properties take precedence over environment variables.
