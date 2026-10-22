package com.juanbenevento.wms.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * S6 — Flyway crea TODAS las tablas de la aplicación.
 *
 * Base PostgreSQL limpia + Flyway ON + ddl-auto=validate: el contexto debe
 * arrancar sin errores de relación inexistente. Es la condición exacta de
 * docker-compose.yml (el arranque real se verifica en T7).
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class FlywaySchemaComplianceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("wms_flyway_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(false);  // DB garantizadamente limpia: solo Flyway crea el schema

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Test
    void contextLoadsWithFlywayMigrationsAndValidate() {
        // S6: si el contexto llegó aquí, Flyway creó todas las tablas que
        // Hibernate valida. Antes de V2__application_tables esto falla con
        // SchemaValidationException (missing table).
    }
}
