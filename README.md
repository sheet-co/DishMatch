# DishMatch
Телеграм бот помощник.

## Основные ценности бота

- уникальность в человечности
- бот запоминает ваши предпочтения в еде, а также ваши блюда,
  используйте его как записную книжку, а когда не знаете что-бы такого вам хочется сьесть на ужин -
  спросите его - дорогой мой, я устала от курочки, вспомни, что я могу такого вкусного себе приготовить?
  (расчет на то, что бот покопается в памяти, предложит пару блюд + задаст наводящие вопросы)
- бот решает проблему забывания любимых блюд, мы что-то ели год назад, потом устали, переключились,
  а теперь снова гадаем что-же делать дальше




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

## License

DishMatch is licensed under the [PolyForm Noncommercial License 1.0.0](LICENSE). You're free to read, run, and modify the code for noncommercial purposes (personal projects, learning, research, etc.). Commercial use — including hosting a paid or ad-supported service based on this code — is not permitted under this license. Get in touch if you want to discuss commercial licensing.
