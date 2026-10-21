package com.juanbenevento.wms.integration;

import com.juanbenevento.wms.integration.config.RequiresDocker;
import com.juanbenevento.wms.shared.infrastructure.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * S2 E2E — Fail-closed a nivel HTTP.
 *
 * Request autenticada con JWT real cuyo claim tenantId apunta a un schema
 * INEXISTENTE: la respuesta debe ser un error explícito (5xx) con mensaje de
 * schema missing y CERO datos servidos desde public. La aislamiento activo
 * ({@code wms.tenant.isolation.enabled=true}) es el mismo modelo de producción.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@RequiresDocker("Requires Docker + PostgreSQL for fail-closed E2E")
class TenantFailClosedEndToEndTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("wms_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(false);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("wms.tenant.isolation.enabled", () -> "true");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("S2: tenant con schema inexistente → 5xx explícito 'Tenant schema missing', cero datos")
    void requestForMissingTenantSchemaReturnsExplicitError() {
        // GIVEN — JWT real del superadmin sembrado por DataInitializer, con
        // claim tenantId apuntando a un schema que nadie provisionó
        UserDetails superAdmin = userDetailsService.loadUserByUsername("superadmin");
        assertNotNull(superAdmin, "DataInitializer debe haber creado superadmin");
        Map<String, Object> claims = new HashMap<>();
        claims.put("tenantId", "ghost_tenant");
        String token = jwtService.generateToken(claims, superAdmin);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        // WHEN — endpoint que consulta datos del tenant
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/products", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        // THEN — error explícito, cero filas servidas
        assertTrue(response.getStatusCode().is5xxServerError(),
                "Debe fallar con 5xx, no servir datos. Status=" + response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body, "Debe haber body de error");
        assertTrue(body.contains("Tenant schema missing"),
                "El error debe mencionar el schema faltante. Body=" + body);
    }
}
