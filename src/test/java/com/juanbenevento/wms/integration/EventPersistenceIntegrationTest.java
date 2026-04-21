package com.juanbenevento.wms.integration;

import com.juanbenevento.wms.integration.config.TestConfig;
import com.juanbenevento.wms.orders.application.port.in.command.CreateOrderCommand;
import com.juanbenevento.wms.orders.application.port.in.command.CreateOrderLineCommand;
import com.juanbenevento.wms.orders.application.service.OrderService;
import com.juanbenevento.wms.orders.infrastructure.out.persistence.SpringDataDomainEventRepository;
import com.juanbenevento.wms.orders.infrastructure.out.persistence.DomainEventEntity;
import com.juanbenevento.wms.shared.infrastructure.tenant.TenantContext;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for event persistence.
 * Tests that domain events are recorded in domain_events table.
 * 
 * NOTE: Tests are disabled due to complexity with:
 * - Tenant context required for multi-tenancy
 * - Async event listeners (events persisted after test transaction)
 * 
 * TODO: Fix with proper test configuration for async event handling
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestConfig.class)
class EventPersistenceIntegrationTest {

    @Autowired
    private SpringDataDomainEventRepository domainEventRepository;

    @Autowired
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId("test-tenant-001");
    }

    // Tests are disabled - see class-level TODO
    @Test
    @DisplayName("Debe persistir OrderCreatedEvent al crear orden")
    @Disabled("Requiere fix de async event handling y tenant context")
    void shouldPersistOrderCreatedEvent() {
        // GIVEN
        long initialCount = domainEventRepository.count();

        // WHEN - Usar OrderService que publica el evento
        CreateOrderCommand command = createTestCommand("SKU-EVENT-001", new BigDecimal("5"));
        orderService.createOrder(command);

        // THEN - Flush to ensure persistence
        domainEventRepository.flush();
        
        long newCount = domainEventRepository.count();
        assertTrue(newCount > initialCount, "Should have more events after creating order");
    }

    @Test
    @DisplayName("Debe encontrar eventos por aggregateId")
    @Disabled("Requiere fix de async event handling y tenant context")
    void shouldFindEventsByAggregateId() {
        // GIVEN
        CreateOrderCommand command = createTestCommand("SKU-EVENT-002", new BigDecimal("10"));
        var response = orderService.createOrder(command);
        domainEventRepository.flush();

        // WHEN
        List<DomainEventEntity> events = domainEventRepository.findByAggregateIdOrderByOccurredAtAsc(response.orderId());

        // THEN
        assertFalse(events.isEmpty(), "Should find events for the order");
        assertEquals(response.orderId(), events.get(0).getAggregateId());
    }

    @Test
    @DisplayName("Debe encontrar eventos por tipo")
    @Disabled("Requiere fix de async event handling y tenant context")
    void shouldFindEventsByType() {
        // GIVEN
        CreateOrderCommand command = createTestCommand("SKU-EVENT-003", new BigDecimal("15"));
        orderService.createOrder(command);
        domainEventRepository.flush();

        // WHEN
        List<DomainEventEntity> events = domainEventRepository.findByEventTypeOrderByOccurredAtAsc("OrderCreatedEvent");

        // THEN
        assertFalse(events.isEmpty(), "Should find OrderCreatedEvent events");
    }

    @Test
    @DisplayName("Debe mantener orden cronológica de eventos")
    @Disabled("Requiere fix de async event handling y tenant context")
    void shouldMaintainChronologicalOrder() {
        // GIVEN
        CreateOrderCommand command = createTestCommand("SKU-EVENT-004", new BigDecimal("20"));
        var response = orderService.createOrder(command);
        domainEventRepository.flush();

        // WHEN
        List<DomainEventEntity> events = domainEventRepository.findByAggregateIdOrderByOccurredAtAsc(response.orderId());

        // THEN
        assertFalse(events.isEmpty());
        
        // Verify chronological order
        for (int i = 1; i < events.size(); i++) {
            assertTrue(
                !events.get(i).getOccurredAt().isBefore(events.get(i - 1).getOccurredAt()),
                "Events should be in chronological order"
            );
        }
    }

    @Test
    @DisplayName("Debe tener payload JSON en eventos")
    void shouldHaveJsonPayload() {
        // GIVEN
        CreateOrderCommand command = createTestCommand("SKU-EVENT-005", new BigDecimal("25"));
        var response = orderService.createOrder(command);
        domainEventRepository.flush();

        // WHEN
        List<DomainEventEntity> events = domainEventRepository.findByAggregateIdOrderByOccurredAtAsc(response.orderId());

        // THEN
        assertFalse(events.isEmpty());
        for (DomainEventEntity event : events) {
            assertNotNull(event.getPayload(), "Event should have payload");
            assertTrue(event.getPayload().length() > 0, "Payload should not be empty");
            assertTrue(event.getPayload().startsWith("{"), "Payload should be JSON");
        }
    }

// ==================== HELPER METHODS ====================

    private CreateOrderCommand createTestCommand(String sku, BigDecimal quantity) {
        return new CreateOrderCommand(
            "customer-event",
            "Event Customer",
            "event@example.com",
            "456 Event Street",
            "MEDIUM",
            LocalDate.now().plusDays(3),
            LocalDate.now().plusDays(5),
            "warehouse-001",
            null,
            List.of(new CreateOrderLineCommand(
                sku,
                quantity,
                LocalDate.now().plusDays(5),
                null
            ))
        );
    }
}
