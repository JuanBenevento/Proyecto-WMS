package com.juanbenevento.wms.orders.application.service;

import com.juanbenevento.wms.orders.application.mapper.OrderMapper;
import com.juanbenevento.wms.orders.application.port.in.command.AddOrderLineCommand;
import com.juanbenevento.wms.orders.application.port.in.command.CancelOrderCommand;
import com.juanbenevento.wms.orders.application.port.in.command.CreateOrderCommand;
import com.juanbenevento.wms.orders.application.port.in.command.CreateOrderLineCommand;
import com.juanbenevento.wms.orders.application.port.in.dto.OrderResponse;
import com.juanbenevento.wms.orders.application.port.out.OrderRepositoryPort;
import com.juanbenevento.wms.orders.domain.event.DomainEvent;
import com.juanbenevento.wms.orders.domain.model.Order;
import com.juanbenevento.wms.orders.domain.model.OrderStatus;
import com.juanbenevento.wms.orders.infrastructure.event.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para OrderService.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepositoryPort orderRepository;

    @Mock
    private EventBus eventBus;

    private OrderMapper orderMapper;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderMapper = new OrderMapper();
        orderService = new OrderService(orderRepository, orderMapper, eventBus);
    }

    @Nested
    @DisplayName("createOrder")
    class CreateOrder {

        @Test
        @DisplayName("Debe crear orden y publicar evento")
        void shouldCreateOrderAndPublishEvent() {
            // GIVEN
            CreateOrderCommand command = createValidCommand();
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            // WHEN
            OrderResponse response = orderService.createOrder(command);

            // THEN
            assertNotNull(response);
            assertNotNull(response.orderId());
            assertNotNull(response.orderNumber());
            assertEquals("CUST-001", response.customerId());
            assertEquals(OrderStatus.CONFIRMED.name(), response.status());

            // Verificar que se guardó
            verify(orderRepository).save(any(Order.class));

            // Verificar que se publicó el evento
            ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
            verify(eventBus).publish(eventCaptor.capture());
            assertEquals("OrderCreatedEvent", eventCaptor.getValue().getEventType());
        }

        @Test
        @DisplayName("Debe incluir líneas en la orden creada")
        void shouldIncludeLinesInCreatedOrder() {
            // GIVEN
            CreateOrderCommand command = createCommandWithLines();
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            // WHEN
            OrderResponse response = orderService.createOrder(command);

            // THEN
            assertEquals(2, response.lineCount());
            assertEquals(2, response.lines().size());
        }

        @Test
        @DisplayName("Debe confirmar automáticamente la orden creada")
        void shouldAutoConfirmCreatedOrder() {
            // GIVEN
            CreateOrderCommand command = createValidCommand();
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            // WHEN
            OrderResponse response = orderService.createOrder(command);

            // THEN
            assertEquals(OrderStatus.CONFIRMED.name(), response.status());
        }
    }

    @Nested
    @DisplayName("cancelOrder")
    class CancelOrder {

        @Test
        @DisplayName("Debe cancelar orden existente")
        void shouldCancelExistingOrder() {
            // GIVEN
            Order order = createPendingOrder();
            when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            CancelOrderCommand command = new CancelOrderCommand(
                order.getOrderId(), "user-123", "Cliente canceló"
            );

            // WHEN
            OrderResponse response = orderService.cancelOrder(command);

            // THEN
            assertEquals(OrderStatus.CANCELLED.name(), response.status());
            assertEquals("user-123", response.cancelledBy());
            verify(eventBus, atLeastOnce()).publish(any(DomainEvent.class));
        }

        @Test
        @DisplayName("Debe rechazar cancelación de orden inexistente")
        void shouldRejectCancellationOfNonExistentOrder() {
            // GIVEN
            when(orderRepository.findById("non-existent")).thenReturn(Optional.empty());

            CancelOrderCommand command = new CancelOrderCommand(
                "non-existent", "user", null
            );

            // WHEN/THEN
            assertThrows(IllegalArgumentException.class, () -> 
                orderService.cancelOrder(command));
        }
    }

    @Nested
    @DisplayName("holdOrder")
    class HoldOrder {

        @Test
        @DisplayName("Debe poner orden en espera con razón")
        void shouldHoldOrderWithReason() {
            // GIVEN
            Order order = createPendingOrder();
            when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            // WHEN
            OrderResponse response = orderService.holdOrder(order.getOrderId(), "PAYMENT_HOLD");

            // THEN
            assertEquals(OrderStatus.HOLD.name(), response.status());
            assertEquals("PAYMENT_HOLD", response.statusReason());
        }

        @Test
        @DisplayName("Debe usar MANUAL_REVIEW si no se especifica razón")
        void shouldUseManualReviewIfNoReason() {
            // GIVEN
            Order order = createPendingOrder();
            when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            // WHEN
            OrderResponse response = orderService.holdOrder(order.getOrderId(), null);

            // THEN
            assertEquals("MANUAL_REVIEW", response.statusReason());
        }
    }

    @Nested
    @DisplayName("releaseOrder")
    class ReleaseOrder {

        @Test
        @DisplayName("Debe liberar orden de espera")
        void shouldReleaseOrderFromHold() {
            // GIVEN
            Order order = createPendingOrder();
            order.hold(com.juanbenevento.wms.orders.domain.model.StatusReason.PAYMENT_HOLD);
            
            when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            // WHEN
            OrderResponse response = orderService.releaseOrder(order.getOrderId());

            // THEN
            assertEquals(OrderStatus.PENDING.name(), response.status());
        }
    }

    @Nested
    @DisplayName("Transiciones de estado")
    class StatusTransitions {

        @Test
        @DisplayName("Debe iniciar picking correctamente")
        void shouldStartPickingCorrectly() {
            // GIVEN
            Order order = createAllocatedOrder();
            when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            // WHEN
            OrderResponse response = orderService.startPicking(order.getOrderId());

            // THEN
            assertEquals(OrderStatus.PICKING.name(), response.status());
        }

        @Test
        @DisplayName("Debe empacar orden")
        void shouldPackOrder() {
            // GIVEN
            Order order = createPickingOrder();
            when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            // WHEN
            OrderResponse response = orderService.packOrder(order.getOrderId());

            // THEN
            assertEquals(OrderStatus.PACKED.name(), response.status());
        }

        @Test
        @DisplayName("Debe enviar orden con carrier y tracking")
        void shouldShipOrderWithCarrierAndTracking() {
            // GIVEN
            Order order = createPackedOrder();
            when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            // WHEN
            OrderResponse response = orderService.shipOrder(
                order.getOrderId(), "CARRIER-001", "TRACK-123456"
            );

            // THEN
            assertEquals(OrderStatus.SHIPPED.name(), response.status());
            assertEquals("CARRIER-001", response.carrierId());
            assertEquals("TRACK-123456", response.trackingNumber());
        }

        @Test
        @DisplayName("Debe marcar orden como entregada")
        void shouldDeliverOrder() {
            // GIVEN
            Order order = createShippedOrder();
            when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            // WHEN
            OrderResponse response = orderService.deliverOrder(order.getOrderId());

            // THEN
            assertEquals(OrderStatus.DELIVERED.name(), response.status());
        }
    }

    @Nested
    @DisplayName("addLine")
    class AddLine {

        @Test
        @DisplayName("Debe agregar línea a orden existente")
        void shouldAddLineToExistingOrder() {
            // GIVEN
            Order order = createPendingOrder();
            when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            AddOrderLineCommand command = new AddOrderLineCommand(
                "SKU-NEW", new BigDecimal("5"), null, "Nueva línea"
            );

            // WHEN
            OrderResponse response = orderService.addLine(order.getOrderId(), command);

            // THEN
            assertEquals(2, response.lineCount());
        }
    }

    @Nested
    @DisplayName("Queries")
    class Queries {

        @Test
        @DisplayName("Debe obtener orden por ID")
        void shouldGetOrderById() {
            // GIVEN
            Order order = createPendingOrder();
            when(orderRepository.findById(order.getOrderId())).thenReturn(Optional.of(order));

            // WHEN
            Optional<OrderResponse> response = orderService.getOrder(order.getOrderId());

            // THEN
            assertTrue(response.isPresent());
            assertEquals(order.getOrderId(), response.get().orderId());
        }

        @Test
        @DisplayName("Debe retornar vacío para orden inexistente")
        void shouldReturnEmptyForNonExistentOrder() {
            // GIVEN
            when(orderRepository.findById("non-existent")).thenReturn(Optional.empty());

            // WHEN
            Optional<OrderResponse> response = orderService.getOrder("non-existent");

            // THEN
            assertTrue(response.isEmpty());
        }
    }

    // ==================== HELPERS ====================

    private CreateOrderCommand createValidCommand() {
        return new CreateOrderCommand(
            "CUST-001", "Juan Pérez", "juan@test.com",
            "Calle Falsa 123", "MEDIUM",
            LocalDate.now().plusDays(3), LocalDate.now().plusDays(5),
            "WH-001", "Notas",
            List.of()
        );
    }

    private CreateOrderCommand createCommandWithLines() {
        List<CreateOrderLineCommand> lines = List.of(
            new CreateOrderLineCommand("SKU-001", new BigDecimal("10"), null, null),
            new CreateOrderLineCommand("SKU-002", new BigDecimal("5"), null, null)
        );

        return new CreateOrderCommand(
            "CUST-001", "Juan Pérez", "juan@test.com",
            "Calle Falsa 123", "MEDIUM",
            LocalDate.now().plusDays(3), LocalDate.now().plusDays(5),
            "WH-001", "Notas",
            lines
        );
    }

    private Order createPendingOrder() {
        Order order = Order.create(
            "CUST-001", "Juan Pérez", "juan@test.com",
            "Calle Falsa 123", "MEDIUM",
            LocalDate.now().plusDays(3), LocalDate.now().plusDays(5),
            "WH-001", null
        );
        order.addLine(com.juanbenevento.wms.orders.domain.model.OrderLine.create(
            UUID.randomUUID().toString(), "SKU-001",
            new BigDecimal("10"), null, null
        ));
        order.confirm();
        order.markAsPending();
        
        // Simular persistencia (asignar ID)
        return Order.fromRepository(
            order.getOrderId(), order.getOrderNumber(),
            order.getCustomerId(), order.getCustomerName(), order.getCustomerEmail(),
            order.getShippingAddress(), order.getPriority(), order.getStatus(),
            order.getPromisedShipDate(), order.getPromisedDeliveryDate(),
            order.getWarehouseId(), null, null, order.getNotes(),
            order.getCreatedAt(), order.getUpdatedAt(), null, null,
            1L, order.getLines()
        );
    }

    private Order createAllocatedOrder() {
        Order order = createPendingOrder();
        var line = order.getLines().get(0);
        order.assignStockToLine(line.getLineId(), line.getRequestedQuantity(), "LPN-001", "A-01-01");
        return order;
    }

    private Order createPickingOrder() {
        Order order = createAllocatedOrder();
        order.startPicking();
        return order;
    }

    private Order createPackedOrder() {
        Order order = createPickingOrder();
        var line = order.getLines().get(0);
        line.pick(line.getRequestedQuantity());
        order.pack();
        return order;
    }

    private Order createShippedOrder() {
        Order order = createPackedOrder();
        order.ship("CARRIER-001", "TRACK-123");
        return order;
    }
}
