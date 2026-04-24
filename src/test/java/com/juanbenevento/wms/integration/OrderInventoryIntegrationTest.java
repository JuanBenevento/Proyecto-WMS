package com.juanbenevento.wms.integration;

import com.juanbenevento.wms.integration.config.TestConfig;
import com.juanbenevento.wms.orders.application.port.out.OrderRepositoryPort;
import com.juanbenevento.wms.orders.application.port.out.OrderQueryPort;
import com.juanbenevento.wms.orders.application.service.InventoryEventHandler;
import com.juanbenevento.wms.orders.domain.model.Order;
import com.juanbenevento.wms.orders.domain.model.OrderLine;
import com.juanbenevento.wms.orders.domain.model.OrderStatus;
import com.juanbenevento.wms.orders.domain.event.OrderStatusChangedEvent;
import com.juanbenevento.wms.shared.infrastructure.tenant.TenantContext;
import com.juanbenevento.wms.shared.domain.valueobject.Lpn;
import com.juanbenevento.wms.shared.domain.valueobject.WmsConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Order ↔ Inventory flow.
 * Tests the complete flow of orders through the WMS system.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestConfig.class)
@Disabled("Requires PostgreSQL/Flyway - run with mvn test -Pci")
class OrderInventoryIntegrationTest {

    private static final String TEST_TENANT_ID = "test-tenant-001";

    @Autowired
    private OrderRepositoryPort orderRepository;

    @Autowired
    private OrderQueryPort orderQueryPort;

    @Autowired
    private InventoryEventHandler inventoryEventHandler;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(TEST_TENANT_ID);
    }

    @Test
    @DisplayName("Debe crear orden en estado PENDING")
    void shouldCreateOrderInPendingState() {
        // WHEN
        Order order = createTestOrder("SKU-TEST-001", new BigDecimal("10"));

        // THEN
        assertNotNull(order);
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(1, order.getLines().size());
    }

    @Test
    @DisplayName("Debe detectar orden PENDING")
    void shouldDetectPendingOrder() {
        // GIVEN
        Order order = createTestOrder("SKU-TEST-002", new BigDecimal("5"));
        orderRepository.save(order);

        // WHEN
        List<String> pendingOrderIds = orderQueryPort.findPendingOrderIds();

        // THEN
        assertTrue(pendingOrderIds.contains(order.getOrderId()),
                "Pending order should be detected");
    }

    @Test
    @DisplayName("Debe obtener info de orden pendiente")
    void shouldGetPendingOrderInfo() {
        // GIVEN
        Order order = createTestOrder("SKU-TEST-003", new BigDecimal("5"));
        orderRepository.save(order);

        // WHEN
        OrderQueryPort.PendingOrderInfo orderInfo = orderQueryPort.getPendingOrderInfo(order.getOrderId());

        // THEN
        assertNotNull(orderInfo);
        assertEquals(order.getOrderId(), orderInfo.orderId());
        assertEquals(1, orderInfo.lines().size());
    }

    @Test
    @DisplayName("Debe asignar stock via InventoryEventHandler")
    void shouldAssignStockViaHandler() {
        // GIVEN
        Order order = createTestOrder("SKU-TEST-004", new BigDecimal("10"));
        orderRepository.save(order);

        String lineId = order.getLines().get(0).getLineId();
        List<InventoryEventHandler.StockAssignedEvent.LineAssignment> lines = List.of(
            new InventoryEventHandler.StockAssignedEvent.LineAssignment(
                lineId,
                "SKU-TEST-004",
                new BigDecimal("10"),
                "LPN-" + UUID.randomUUID().toString().substring(0, 8),
                "A-01-01"
            )
        );

        InventoryEventHandler.StockAssignedEvent event = new InventoryEventHandler.StockAssignedEvent(
            order.getOrderId(),
            order.getOrderNumber(),
            java.time.Instant.now(),
            lines
        );

        // WHEN
        inventoryEventHandler.onStockAssigned(event);

        // THEN
        Order updatedOrder = orderRepository.findById(order.getOrderId()).orElseThrow();
        assertEquals(OrderStatus.ALLOCATED, updatedOrder.getStatus());
        
        OrderLine line = updatedOrder.getLines().get(0);
        assertNotNull(line.getInventoryItemId());
        assertNotNull(line.getLocationCode());
    }

    @Test
    @DisplayName("Debe reportar faltante de stock")
    void shouldHandleStockShortage() {
        // GIVEN
        Order order = createTestOrder("SKU-TEST-005", new BigDecimal("100"));
        orderRepository.save(order);

        String lineId = order.getLines().get(0).getLineId();
        List<InventoryEventHandler.StockShortageEvent.LineShortage> shortages = List.of(
            new InventoryEventHandler.StockShortageEvent.LineShortage(
                lineId,
                "SKU-TEST-005",
                new BigDecimal("100"),
                BigDecimal.ZERO
            )
        );

        InventoryEventHandler.StockShortageEvent event = new InventoryEventHandler.StockShortageEvent(
            order.getOrderId(),
            order.getOrderNumber(),
            java.time.Instant.now(),
            com.juanbenevento.wms.orders.domain.model.StatusReason.INVENTORY_SHORTAGE,
            shortages
        );

        // WHEN
        inventoryEventHandler.onStockShortage(event);

        // THEN
        Order updatedOrder = orderRepository.findById(order.getOrderId()).orElseThrow();
        assertEquals(OrderStatus.HOLD, updatedOrder.getStatus());
    }

    // ==================== HELPER METHODS ====================

    /**
     * Creates a test order in PENDING state (ready for Inventory allocation).
     * This simulates the state after OrderService.createOrder() completes.
     */
    private Order createTestOrder(String sku, BigDecimal quantity) {
        String orderId = UUID.randomUUID().toString();
        String orderNumber = "ORD-" + System.currentTimeMillis();
        
        Order order = Order.create(
            "customer-001",
            "Test Customer",
            "test@example.com",
            "123 Test Street",
            "MEDIUM",
            LocalDate.now().plusDays(3),
            LocalDate.now().plusDays(5),
            TEST_TENANT_ID,
            null
        );

        order.addLine(OrderLine.create(
            UUID.randomUUID().toString(),
            sku,
            quantity,
            LocalDate.now().plusDays(5),
            null
        ));

        // Transition to PENDING state (Inventory Leads pattern)
        order.confirm();
        order.markAsPending();

        return order;
    }
}
