buildscript {
	repositories {
		mavenLocal()
		mavenCentral()
	}
	dependencies {
		classpath("org.postgresql:postgresql:42.7.2")
		classpath("org.flywaydb:flyway-database-postgresql:10.12.0")
	}
}

plugins {
	java
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
	id("io.freefair.lombok") version "8.4"
	id("org.flywaydb.flyway") version "10.10.0"
}

group = "org.js"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.5.0")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.jetbrains:annotations:24.0.0")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
	implementation("org.springframework.kafka:spring-kafka")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.13")

	runtimeOnly("org.postgresql:postgresql:42.7.2")
	testImplementation("org.mockito:mockito-core:5.7.0")
	testImplementation("junit:junit:4.13.1")

	// MapStruct
	implementation("org.mapstruct:mapstruct:1.4.2.Final")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.4.2.Final")

	// Health check for Spring Boot
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	// Logstash
	implementation("net.logstash.logback:logstash-logback-encoder:9.0")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

flyway {
	url = "jdbc:postgresql://localhost:5432/postgres"
	user = "user"
	password = "password"
}
