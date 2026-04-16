package com.juanbenevento.wms.inventory.infrastructure.in.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test de integración para idempotencia.
 * Requiere Docker en el entorno. Si Docker no está disponible, el test se salta.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "application.security.jwt.secret-key=${TEST_JWT_SECRET:una-clave-super-secreta-para-entornos-de-prueba-muy-larga-123456789}",
                "application.security.jwt.expiration=86400000"
        }
)
@Testcontainers
class IdempotencyIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("wms_db")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Debe procesar la petición 1 vez y rechazar la concurrente con 409 Conflict")
    void shouldHandleConcurrentRequestsIdempotently() throws InterruptedException, ExecutionException {
        String idempotencyKey = UUID.randomUUID().toString();
        String payload = """
                {
                    "productSku": "TV-LG-65",
                    "quantity": 10.0,
                    "locationCode": "RECEIVING-1"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Idempotency-Key", idempotencyKey);
        headers.setBearerAuth("TU_TOKEN_MOCK_SI_ES_NECESARIO");

        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        int numberOfThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);

        List<Callable<ResponseEntity<String>>> tasks = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            tasks.add(() -> {
                latch.await();
                return restTemplate.postForEntity("/api/v1/inventory/receive", request, String.class);
            });
        }

        // WHEN
        List<Future<ResponseEntity<String>>> futures = executor.invokeAll(tasks);
        latch.countDown();

        // THEN
        List<HttpStatus> statusCodes = new ArrayList<>();
        for (Future<ResponseEntity<String>> future : futures) {
            statusCodes.add((HttpStatus) future.get().getStatusCode());
        }

        executor.shutdown();

        // ASSERT
        assertTrue(statusCodes.contains(HttpStatus.CREATED) || statusCodes.contains(HttpStatus.OK),
                "Una petición debió ser exitosa");

        assertTrue(statusCodes.contains(HttpStatus.CONFLICT),
                "La petición concurrente debió ser bloqueada por el IdempotencyAspect");

    }
}
