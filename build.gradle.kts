buildscript {
	dependencies {
		classpath("org.flywaydb:flyway-database-postgresql:13.2.0")
		classpath("org.postgresql:postgresql:42.7.8")
	}
}

plugins {
	java
	id("org.springframework.boot") version "4.1.0"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.flywaydb.flyway") version "13.2.0"
}

group = "dev.sheet_co"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

extra["springAiVersion"] = "2.0.0"

fun flywayProperty(name: String, environmentVariable: String, default: String): String =
	providers.gradleProperty(name)
		.orElse(providers.environmentVariable(environmentVariable))
		.orElse(default)
		.get()

flyway {
	url = flywayProperty("flyway.url", "FLYWAY_URL", "jdbc:postgresql://localhost:5432/dish_match_bd")
	user = flywayProperty("flyway.user", "FLYWAY_USER", "your_user")
	password = flywayProperty("flyway.password", "FLYWAY_PASSWORD", "your_password")
	locations = flywayProperty(
		"flyway.locations",
		"FLYWAY_LOCATIONS",
		"filesystem:src/main/resources/db/migrations",
	).split(',').map(String::trim).toTypedArray()
	schemas = flywayProperty("flyway.schemas", "FLYWAY_SCHEMAS", "public")
		.split(',').map(String::trim).toTypedArray()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.ai:spring-ai-starter-model-google-genai")
	// Source: https://mvnrepository.com/artifact/org.telegram/telegrambots-client
	implementation("org.telegram:telegrambots-client:10.2.0")
	// Source: https://mvnrepository.com/artifact/org.telegram/telegrambots-springboot-longpolling-starter
	implementation("org.telegram:telegrambots-springboot-longpolling-starter:10.2.0")
	implementation("org.flywaydb:flyway-database-postgresql:13.2.0")
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor("org.projectlombok:lombok")

}

dependencyManagement {
	imports {
		mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
