package com.juanbenevento.wms.integration;

import com.juanbenevento.wms.integration.config.TestConfig;
import com.juanbenevento.wms.orders.domain.model.Order;
import com.juanbenevento.wms.orders.domain.model.OrderLine;
import com.juanbenevento.wms.orders.domain.model.OrderStatus;
import com.juanbenevento.wms.shared.infrastructure.tenant.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Order state transitions.
 * Tests the session lifecycle and short pick handling scenarios.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional
@Import(TestConfig.class)
class PickingWorkflowIntegrationTest {

    private static final String TEST_TENANT_ID = "test-tenant-001";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("wms_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(TEST_TENANT_ID);
    }

    @Test
    @DisplayName("Debe crear orden lista para picking")
    void shouldCreateOrderReadyForPicking() {
        // GIVEN - Use helper that creates order in PENDING state
        Order order = createOrderForPicking();
        
        // THEN - Order should be in PENDING state (Inventory Leads pattern)
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(1, order.getLines().size());
        assertFalse(order.getLines().get(0).isFulfilled());
    }

    @Test
    @DisplayName("Debe simular transición a estado PICKING")
    void shouldSimulateTransitionToPicking() {
        // GIVEN
        Order order = createOrderForPicking();
        
        // WHEN - Simulate stock assignment first
        order.assignStockToLine(
            order.getLines().get(0).getLineId(),
            new BigDecimal("50"),
            "LPN-PICK001",
            "A-01-01"
        );

        // THEN - Order should be allocated
        assertEquals(OrderStatus.ALLOCATED, order.getStatus());
        
        OrderLine line = order.getLines().get(0);
        assertNotNull(line.getInventoryItemId());
        assertNotNull(line.getLocationCode());
    }

    @Test
    @DisplayName("Debe manejar short pick en orden")
    void shouldHandleShortPickInOrder() {
        // GIVEN
        Order order = createOrderForPicking();
        order.assignStockToLine(
            order.getLines().get(0).getLineId(),
            new BigDecimal("30"),  // Short by 20
            "LPN-SHORT01",
            "A-01-01"
        );

        // THEN - Line should have shortage
        OrderLine line = order.getLines().get(0);
        assertTrue(line.hasShortage());
        assertEquals(new BigDecimal("30"), line.getAllocatedQuantity());
        assertEquals(new BigDecimal("50"), line.getRequestedQuantity());
    }

    @Test
    @DisplayName("Debe procesar orden con stock parcial")
    void shouldProcessPartialStockOrder() {
        // GIVEN
        Order order = createOrderForPicking();
        
        // Asignar stock parcial
        OrderLine line = order.getLines().get(0);
        order.assignStockToLine(line.getLineId(), new BigDecimal("25"), "LPN-PARTIAL", "A-02-01");

        // THEN
        assertEquals(OrderStatus.ALLOCATED, order.getStatus());
        // Verify partial allocation
        BigDecimal allocated = order.getTotalAllocatedQuantity();
        assertEquals(new BigDecimal("25"), allocated);
    }

    @Test
    @DisplayName("Debe calcular quantities correctamente")
    void shouldCalculateQuantitiesCorrectly() {
        // GIVEN
        Order order = createOrderForPicking();
        OrderLine line = order.getLines().get(0);
        
        // WHEN
        BigDecimal totalRequested = order.getTotalRequestedQuantity();
        BigDecimal totalAllocated = order.getTotalAllocatedQuantity();

        // THEN
        assertEquals(new BigDecimal("50"), totalRequested);
        assertEquals(BigDecimal.ZERO, totalAllocated); // No allocated yet

        // Assign partial
        order.assignStockToLine(line.getLineId(), new BigDecimal("30"), "LPN-TEST", "A-01-01");
        
        totalAllocated = order.getTotalAllocatedQuantity();
        assertEquals(new BigDecimal("30"), totalAllocated);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Creates order in PENDING state (Inventory Leads pattern).
     */
    private Order createOrderForPicking() {
        Order order = Order.create(
            "customer-pick",
            "Pick Customer",
            "pick@example.com",
            "789 Pick Street",
            "HIGH",
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(2),
            TEST_TENANT_ID,
            null
        );

        order.addLine(OrderLine.create(
            UUID.randomUUID().toString(),
            "SKU-PICK-" + UUID.randomUUID().toString().substring(0, 8),
            new BigDecimal("50"),
            LocalDate.now().plusDays(2),
            null
        ));

        // Transition to PENDING state (Inventory Leads pattern)
        order.confirm();
        order.markAsPending();

        return order;
    }
}
