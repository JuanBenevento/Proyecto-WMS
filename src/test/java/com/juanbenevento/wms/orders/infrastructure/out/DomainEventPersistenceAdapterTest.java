package com.juanbenevento.wms.orders.infrastructure.out;

import com.juanbenevento.wms.orders.application.mapper.DomainEventMapper;
import com.juanbenevento.wms.orders.application.port.out.DomainEventRepositoryPort;
import com.juanbenevento.wms.orders.domain.event.OrderCreatedEvent;
import com.juanbenevento.wms.orders.domain.model.Order;
import com.juanbenevento.wms.orders.domain.model.OrderLine;
import com.juanbenevento.wms.orders.infrastructure.out.persistence.DomainEventEntity;
import com.juanbenevento.wms.orders.infrastructure.out.persistence.SpringDataDomainEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para DomainEventPersistenceAdapter.
 * Verifica la persistencia de eventos y las operaciones de consulta.
 */
@ExtendWith(MockitoExtension.class)
class DomainEventPersistenceAdapterTest {

    @Mock
    private SpringDataDomainEventRepository jpaRepository;

    @Mock
    private DomainEventMapper mapper;

    private DomainEventRepositoryPort adapter;

    @BeforeEach
    void setUp() {
        adapter = new DomainEventPersistenceAdapter(jpaRepository, mapper);
    }

    @Nested
    @DisplayName("Persistencia de Eventos")
    class EventPersistence {

        @Test
        @DisplayName("Debe persistir evento exitosamente")
        void shouldSaveEventSuccessfully() {
            // GIVEN
            Order order = createTestOrder();
            OrderCreatedEvent event = new OrderCreatedEvent(order, Map.of("user", "test-user"));
            
            DomainEventEntity savedEntity = DomainEventEntity.builder()
                .id(1L)
                .eventId(event.getEventId())
                .eventType("OrderCreatedEvent")
                .aggregateId(order.getOrderId())
                .aggregateType("Order")
                .payload("{}")
                .occurredAt(event.getOccurredAt())
                .correlationId(event.getCorrelationId())
                .build();

            when(mapper.toEntity(any())).thenReturn(savedEntity);
            when(jpaRepository.save(any())).thenReturn(savedEntity);

            // WHEN
            adapter.save(event);

            // THEN
            verify(jpaRepository).save(any(DomainEventEntity.class));
            verify(mapper).toEntity(event);
        }

        @Test
        @DisplayName("Debe guardar multiples eventos en batch")
        void shouldSaveAllEvents() {
            // GIVEN
            Order order = createTestOrder();
            OrderCreatedEvent event1 = new OrderCreatedEvent(order, Map.of("v", 1));
            OrderCreatedEvent event2 = new OrderCreatedEvent(order, Map.of("v", 2));

            when(mapper.toEntity(any())).thenReturn(
                DomainEventEntity.builder().id(1L).eventId("id1").build(),
                DomainEventEntity.builder().id(2L).eventId("id2").build()
            );

            // WHEN
            adapter.saveAll(List.of(event1, event2));

            // THEN
            verify(jpaRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("No debe fallar si saveAll recibe lista vacia")
        void shouldHandleEmptyListGracefully() {
            // WHEN
            adapter.saveAll(List.of());

            // THEN
            verify(jpaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Consultas de Eventos")
    class EventQueries {

        @Test
        @DisplayName("Debe buscar eventos por aggregate ID")
        void shouldFindByAggregateId() {
            // GIVEN
            String aggregateId = "order-123";
            DomainEventEntity entity = DomainEventEntity.builder()
                .id(1L)
                .eventId("event-1")
                .eventType("OrderCreatedEvent")
                .aggregateId(aggregateId)
                .aggregateType("Order")
                .payload("{}")
                .occurredAt(Instant.now())
                .build();

            when(jpaRepository.findByAggregateIdOrderByOccurredAtAsc(aggregateId))
                .thenReturn(List.of(entity));

            // WHEN
            var result = adapter.findByAggregateId(aggregateId);

            // THEN
            assertNotNull(result);
            verify(jpaRepository).findByAggregateIdOrderByOccurredAtAsc(aggregateId);
        }

        @Test
        @DisplayName("Debe buscar eventos por tipo")
        void shouldFindByEventType() {
            // GIVEN
            String eventType = "OrderCreatedEvent";
            when(jpaRepository.findByEventTypeOrderByOccurredAtAsc(eventType))
                .thenReturn(List.of());

            // WHEN
            adapter.findByEventType(eventType);

            // THEN
            verify(jpaRepository).findByEventTypeOrderByOccurredAtAsc(eventType);
        }

        @Test
        @DisplayName("Debe buscar eventos por correlation ID")
        void shouldFindByCorrelationId() {
            // GIVEN
            String correlationId = "corr-123";
            when(jpaRepository.findByCorrelationIdOrderByOccurredAtAsc(correlationId))
                .thenReturn(List.of());

            // WHEN
            adapter.findByCorrelationId(correlationId);

            // THEN
            verify(jpaRepository).findByCorrelationIdOrderByOccurredAtAsc(correlationId);
        }

        @Test
        @DisplayName("Debe encontrar ultimo evento de aggregate")
        void shouldFindLastEventByAggregateId() {
            // GIVEN
            String aggregateId = "order-123";
            DomainEventEntity entity = DomainEventEntity.builder()
                .id(1L)
                .eventId("last-event")
                .aggregateId(aggregateId)
                .build();

            when(jpaRepository.findLastEventByAggregateId(aggregateId))
                .thenReturn(entity);

            // WHEN
            var result = adapter.findLastEventByAggregateId(aggregateId);

            // THEN
            assertNotNull(result);
            verify(jpaRepository).findLastEventByAggregateId(aggregateId);
        }

        @Test
        @DisplayName("Debe retornar null si no hay eventos")
        void shouldReturnNullWhenNoEvents() {
            // GIVEN
            String aggregateId = "nonexistent";
            when(jpaRepository.findLastEventByAggregateId(aggregateId))
                .thenReturn(null);

            // WHEN
            var result = adapter.findLastEventByAggregateId(aggregateId);

            // THEN
            assertNull(result);
        }

        @Test
        @DisplayName("Debe contar eventos de un aggregate")
        void shouldCountByAggregateId() {
            // GIVEN
            String aggregateId = "order-123";
            when(jpaRepository.countByAggregateId(aggregateId))
                .thenReturn(5L);

            // WHEN
            long count = adapter.countByAggregateId(aggregateId);

            // THEN
            assertEquals(5L, count);
        }
    }

    // Helper methods

    private Order createTestOrder() {
        Order order = Order.create(
            "CUST-001", "Test Customer", "test@test.com",
            "Test Address 123", "MEDIUM", LocalDate.now().plusDays(3),
            LocalDate.now().plusDays(5), "WH-001", "Notas de prueba"
        );
        
        OrderLine line = OrderLine.create(
            UUID.randomUUID().toString(),
            "SKU-001",
            BigDecimal.TEN,
            LocalDate.now().plusDays(5),
            "Notas de linea"
        );
        order.addLine(line);
        
        return order;
    }
}