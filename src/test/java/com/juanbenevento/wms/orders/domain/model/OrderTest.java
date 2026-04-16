package com.juanbenevento.wms.orders.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la entidad Order.
 * Verifica la lógica de dominio, transiciones de estado y validaciones.
 */
class OrderTest {

    private static final String CUSTOMER_ID = "CUST-001";
    private static final String CUSTOMER_NAME = "Juan Pérez";
    private static final String CUSTOMER_EMAIL = "juan@test.com";
    private static final String SHIPPING_ADDRESS = "Calle Falsa 123";
    private static final String WAREHOUSE_ID = "WH-001";

    @Nested
    @DisplayName("Creación de Orden")
    class OrderCreation {

        @Test
        @DisplayName("Debe crear orden con estado CREATED")
        void shouldCreateOrderWithCreatedStatus() {
            // WHEN
            Order order = createValidOrder();

            // THEN
            assertNotNull(order);
            assertNotNull(order.getOrderId());
            assertNotNull(order.getOrderNumber());
            assertTrue(order.getOrderNumber().startsWith("ORD-"));
            assertEquals(CUSTOMER_ID, order.getCustomerId());
            assertEquals(CUSTOMER_NAME, order.getCustomerName());
            assertEquals(CUSTOMER_EMAIL, order.getCustomerEmail());
            assertEquals(SHIPPING_ADDRESS, order.getShippingAddress());
            assertEquals(WAREHOUSE_ID, order.getWarehouseId());
            assertEquals("MEDIUM", order.getPriority()); // Default
            assertEquals(OrderStatus.CREATED, order.getStatus());
            assertTrue(order.getLines().isEmpty());
        }

        @Test
        @DisplayName("Debe rechazar orden sin customerId")
        void shouldRejectOrderWithoutCustomerId() {
            assertThrows(IllegalArgumentException.class, () ->
                Order.create(null, CUSTOMER_NAME, CUSTOMER_EMAIL, SHIPPING_ADDRESS, 
                            "HIGH", null, null, WAREHOUSE_ID, null));
        }

        @Test
        @DisplayName("Debe rechazar orden sin shippingAddress")
        void shouldRejectOrderWithoutShippingAddress() {
            assertThrows(IllegalArgumentException.class, () ->
                Order.create(CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_EMAIL, null, 
                            "HIGH", null, null, WAREHOUSE_ID, null));
        }

        @Test
        @DisplayName("Debe usar prioridad HIGH cuando se especifica")
        void shouldUseHighPriorityWhenSpecified() {
            Order order = Order.create(CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_EMAIL, 
                                      SHIPPING_ADDRESS, "HIGH", null, null, WAREHOUSE_ID, null);
            assertEquals("HIGH", order.getPriority());
        }

        @Test
        @DisplayName("Debe usar prioridad MEDIUM cuando no se especifica")
        void shouldUseMediumPriorityByDefault() {
            Order order = Order.create(CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_EMAIL, 
                                      SHIPPING_ADDRESS, null, null, null, WAREHOUSE_ID, null);
            assertEquals("MEDIUM", order.getPriority());
        }
    }

    @Nested
    @DisplayName("Gestión de Líneas")
    class OrderLinesManagement {

        @Test
        @DisplayName("Debe agregar línea a orden en estado CREATED")
        void shouldAddLineToCreatedOrder() {
            Order order = createValidOrder();
            OrderLine line = createValidOrderLine();

            order.addLine(line);

            assertEquals(1, order.getLineCount());
            assertEquals(1, order.getLines().size());
        }

        @Test
        @DisplayName("Debe agregar línea a orden en estado PENDING")
        void shouldAddLineToPendingOrder() {
            Order order = createValidOrder();
            order.confirm();
            order.markAsPending();
            
            OrderLine line = createValidOrderLine();
            order.addLine(line);

            assertEquals(1, order.getLineCount());
        }

        @Test
        @DisplayName("Debe rechazar agregar línea a orden en estado PICKING")
        void shouldRejectAddLineToPickingOrder() {
            Order order = createOrderWithLine();
            order.confirm();
            order.markAsPending();
            
            // Simular que Inventory asigna stock y pasa a ALLOCATED
            OrderLine line = order.getLines().get(0);
            order.assignStockToLine(line.getLineId(), line.getRequestedQuantity(), "LPN-001", "A-01-01");
            
            order.startPicking();

            OrderLine newLine = createValidOrderLine();
            assertThrows(IllegalStateException.class, () -> order.addLine(newLine));
        }

        @Test
        @DisplayName("Debe rechazar agregar línea nula")
        void shouldRejectNullLine() {
            Order order = createValidOrder();
            assertThrows(IllegalArgumentException.class, () -> order.addLine(null));
        }

        @Test
        @DisplayName("Debe calcular correctamente el total de cantidades")
        void shouldCalculateTotalQuantities() {
            Order order = createValidOrder();
            order.addLine(OrderLine.create(UUID.randomUUID().toString(), "SKU-001", 
                                          new BigDecimal("10"), null, null));
            order.addLine(OrderLine.create(UUID.randomUUID().toString(), "SKU-002", 
                                          new BigDecimal("5"), null, null));

            assertEquals(0, new BigDecimal("15").compareTo(order.getTotalRequestedQuantity()));
        }
    }

    @Nested
    @DisplayName("Transiciones de Estado")
    class StatusTransitions {

        @Test
        @DisplayName("Debe transicionar de CREATED a CONFIRMED")
        void shouldTransitionFromCreatedToConfirmed() {
            Order order = createValidOrder();

            order.confirm();

            assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        }

        @Test
        @DisplayName("Debe transicionar de CONFIRMED a PENDING")
        void shouldTransitionFromConfirmedToPending() {
            Order order = createValidOrder();
            order.confirm();

            order.markAsPending();

            assertEquals(OrderStatus.PENDING, order.getStatus());
        }

        @Test
        @DisplayName("Debe transicionar de PENDING a HOLD con razón")
        void shouldTransitionFromPendingToHold() {
            Order order = createOrderWithLine();
            order.confirm();
            order.markAsPending();

            order.hold(StatusReason.INVENTORY_SHORTAGE);

            assertEquals(OrderStatus.HOLD, order.getStatus());
            assertEquals(StatusReason.INVENTORY_SHORTAGE, order.getStatusReason());
        }

        @Test
        @DisplayName("Debe transicionar de HOLD a PENDING")
        void shouldTransitionFromHoldToPending() {
            Order order = createOrderWithLine();
            order.confirm();
            order.markAsPending();
            order.hold(StatusReason.INVENTORY_SHORTAGE);

            order.releaseFromHold();

            assertEquals(OrderStatus.PENDING, order.getStatus());
        }

        @Test
        @DisplayName("Debe transicionar a CANCELLED desde cualquier estado activo")
        void shouldTransitionToCancelledFromActiveState() {
            // Desde CREATED
            Order order1 = createValidOrder();
            order1.cancel(StatusReason.CUSTOMER_CANCELLED, "user-1", "Cliente canceló");
            assertEquals(OrderStatus.CANCELLED, order1.getStatus());

            // Desde CONFIRMED
            Order order2 = createValidOrder();
            order2.confirm();
            order2.cancel(StatusReason.OUT_OF_STOCK, "system", "Sin stock");
            assertEquals(OrderStatus.CANCELLED, order2.getStatus());

            // Desde PENDING
            Order order3 = createOrderWithLine();
            order3.confirm();
            order3.markAsPending();
            order3.cancel(StatusReason.PAYMENT_FAILED, "system", "Pago rechazado");
            assertEquals(OrderStatus.CANCELLED, order3.getStatus());
        }

        @Test
        @DisplayName("Debe rechazar transición inválida de PENDING a PICKING")
        void shouldRejectInvalidTransitionFromPendingToPicking() {
            Order order = createOrderWithLine();
            order.confirm();
            order.markAsPending();

            assertThrows(IllegalStateException.class, () -> order.startPicking());
        }

        @Test
        @DisplayName("Debe rechazar transición desde estado terminal")
        void shouldRejectTransitionFromTerminalState() {
            Order order = createValidOrder();
            order.confirm();
            order.cancel(StatusReason.CUSTOMER_CANCELLED, "user", null);

            assertThrows(IllegalStateException.class, () -> order.confirm());
            assertThrows(IllegalStateException.class, () -> order.pack());
            assertThrows(IllegalStateException.class, () -> order.startPicking());
        }
    }

    @Nested
    @DisplayName("Flujo Completo Happy Path")
    class HappyPathFlow {

        @Test
        @DisplayName("Debe completar flujo de CREATED a DELIVERED")
        void shouldCompleteFullFlow() {
            // GIVEN
            Order order = createOrderWithLine();
            OrderLine line = order.getLines().get(0);

            // CREATED → CONFIRMED
            order.confirm();
            assertEquals(OrderStatus.CONFIRMED, order.getStatus());

            // CONFIRMED → PENDING
            order.markAsPending();
            assertEquals(OrderStatus.PENDING, order.getStatus());

            // Inventory asigna stock
            order.assignStockToLine(line.getLineId(), line.getRequestedQuantity(), "LPN-001", "A-01-01");
            assertEquals(OrderStatus.ALLOCATED, order.getStatus());

            // ALLOCATED → PICKING
            order.startPicking();
            assertEquals(OrderStatus.PICKING, order.getStatus());

            // PICKING → PACKED
            line.pick(line.getRequestedQuantity());
            order.pack();
            assertEquals(OrderStatus.PACKED, order.getStatus());

            // PACKED → SHIPPED
            order.ship("CARRIER-001", "TRACK-123456");
            assertEquals(OrderStatus.SHIPPED, order.getStatus());

            // SHIPPED → DELIVERED
            order.deliver();
            assertEquals(OrderStatus.DELIVERED, order.getStatus());
        }
    }

    @Nested
    @DisplayName("Asignación de Stock")
    class StockAllocation {

        @Test
        @DisplayName("Debe asignar stock a línea correctamente")
        void shouldAssignStockToLine() {
            Order order = createOrderWithLine();
            OrderLine line = order.getLines().get(0);
            String lineId = line.getLineId();

            order.assignStockToLine(lineId, new BigDecimal("5"), "LPN-001", "A-01-01");

            OrderLine updatedLine = order.getLines().get(0);
            assertEquals(new BigDecimal("5"), updatedLine.getAllocatedQuantity());
            assertEquals("LPN-001", updatedLine.getInventoryItemId());
            assertEquals("A-01-01", updatedLine.getLocationCode());
        }

        @Test
        @DisplayName("Debe transicionar a ALLOCATED cuando todas las líneas tienen stock")
        void shouldTransitionToAllocatedWhenAllLinesHaveStock() {
            Order order = createValidOrder();
            order.addLine(OrderLine.create(UUID.randomUUID().toString(), "SKU-001", 
                                          new BigDecimal("10"), null, null));
            order.addLine(OrderLine.create(UUID.randomUUID().toString(), "SKU-002", 
                                          new BigDecimal("5"), null, null));
            
            order.confirm();
            order.markAsPending();

            // Asignar stock a primera línea
            order.assignStockToLine(order.getLines().get(0).getLineId(), 
                                   new BigDecimal("10"), "LPN-001", "A-01-01");
            assertEquals(OrderStatus.PENDING, order.getStatus());

            // Asignar stock a segunda línea
            order.assignStockToLine(order.getLines().get(1).getLineId(), 
                                   new BigDecimal("5"), "LPN-002", "A-01-02");
            assertEquals(OrderStatus.ALLOCATED, order.getStatus());
        }

        @Test
        @DisplayName("Debe reportar faltante cuando stock es parcial")
        void shouldReportShortageWhenStockIsPartial() {
            Order order = createOrderWithLine();
            OrderLine line = order.getLines().get(0);
            
            order.confirm();
            order.markAsPending();

            // Asignar menos de lo solicitado
            order.reportShortageForLine(line.getLineId(), new BigDecimal("3"));

            assertEquals(OrderStatus.HOLD, order.getStatus());
            assertEquals(StatusReason.INVENTORY_SHORTAGE, order.getStatusReason());
        }

        @Test
        @DisplayName("Debe rechazar asignación con cantidad inválida")
        void shouldRejectAllocationWithInvalidQuantity() {
            Order order = createOrderWithLine();
            OrderLine line = order.getLines().get(0);
            
            assertThrows(IllegalArgumentException.class, () ->
                order.assignStockToLine(line.getLineId(), null, "LPN-001", "A-01-01"));
        }
    }

    @Nested
    @DisplayName("Cancelación")
    class Cancellation {

        @Test
        @DisplayName("Debe cancelar orden y guardar razón")
        void shouldCancelOrderWithReason() {
            Order order = createOrderWithLine();
            order.confirm();

            order.cancel(StatusReason.OUT_OF_STOCK, "system", "Stock agotado");

            assertEquals(OrderStatus.CANCELLED, order.getStatus());
            assertEquals(StatusReason.OUT_OF_STOCK, order.getStatusReason());
            assertEquals("system", order.getCancelledBy());
            assertEquals("Stock agotado", order.getCancellationReason());
        }

        @Test
        @DisplayName("Debe cancelar líneas reservadas")
        void shouldCancelReservedLines() {
            Order order = createOrderWithLine();
            OrderLine line = order.getLines().get(0);
            order.confirm();
            order.markAsPending();
            
            // Simular que se reservó stock
            order.assignStockToLine(line.getLineId(), line.getRequestedQuantity(), "LPN-001", "A-01-01");
            
            order.cancel(StatusReason.CUSTOMER_CANCELLED, "user", null);

            // Verificar que la línea se canceló
            assertEquals(OrderLineStatus.CANCELLED, order.getLines().get(0).getStatus());
        }

        @Test
        @DisplayName("Debe usar razón por defecto si no se especifica")
        void shouldUseDefaultReasonIfNotSpecified() {
            Order order = createValidOrder();
            
            order.cancel(null, "user", null);

            assertEquals(StatusReason.CUSTOMER_CANCELLED, order.getStatusReason());
        }
    }

    @Nested
    @DisplayName("Validaciones")
    class Validations {

        @Test
        @DisplayName("Debe rechazar línea con SKU inválido")
        void shouldRejectLineWithInvalidSku() {
            Order order = createValidOrder();

            assertThrows(IllegalArgumentException.class, () ->
                OrderLine.create(UUID.randomUUID().toString(), null, new BigDecimal("10"), null, null));
            
            assertThrows(IllegalArgumentException.class, () ->
                OrderLine.create(UUID.randomUUID().toString(), "", new BigDecimal("10"), null, null));
        }

        @Test
        @DisplayName("Debe rechazar línea con cantidad inválida")
        void shouldRejectLineWithInvalidQuantity() {
            assertThrows(IllegalArgumentException.class, () ->
                OrderLine.create(UUID.randomUUID().toString(), "SKU-001", null, null, null));
            
            assertThrows(IllegalArgumentException.class, () ->
                OrderLine.create(UUID.randomUUID().toString(), "SKU-001", BigDecimal.ZERO, null, null));
            
            assertThrows(IllegalArgumentException.class, () ->
                OrderLine.create(UUID.randomUUID().toString(), "SKU-001", new BigDecimal("-5"), null, null));
        }

        @Test
        @DisplayName("Debe rechazar cantidad asignada mayor a la solicitada")
        void shouldRejectAllocatedQuantityGreaterThanRequested() {
            Order order = createOrderWithLine();
            OrderLine line = order.getLines().get(0);

            assertThrows(IllegalArgumentException.class, () ->
                order.assignStockToLine(line.getLineId(), 
                                       new BigDecimal("100"), // Mayor que requested (10)
                                       "LPN-001", "A-01-01"));
        }
    }

    @Nested
    @DisplayName("Cálculos")
    class Calculations {

        @Test
        @DisplayName("Debe calcular hasShortage correctamente")
        void shouldCalculateHasShortageCorrectly() {
            Order order = createOrderWithLine();
            OrderLine line = order.getLines().get(0);
            order.confirm();
            order.markAsPending();

            assertFalse(order.hasShortage());

            order.reportShortageForLine(line.getLineId(), new BigDecimal("5"));

            assertTrue(order.hasShortage());
        }

        @Test
        @DisplayName("Debe calcular isFullyFulfilled correctamente")
        void shouldCalculateIsFullyFulfilledCorrectly() {
            Order order = createOrderWithLine();
            OrderLine line = order.getLines().get(0);
            order.confirm();
            order.markAsPending();
            order.assignStockToLine(line.getLineId(), line.getRequestedQuantity(), "LPN-001", "A-01-01");
            
            // Marcar como entregado
            line.pick(line.getRequestedQuantity());
            line.ship(line.getRequestedQuantity());
            line.deliver(line.getRequestedQuantity());

            assertTrue(order.isFullyFulfilled());
        }

        @Test
        @DisplayName("Debe retornar cero cuando no hay líneas")
        void shouldReturnZeroWhenNoLines() {
            Order order = createValidOrder();

            assertEquals(BigDecimal.ZERO, order.getTotalRequestedQuantity());
            assertEquals(BigDecimal.ZERO, order.getTotalAllocatedQuantity());
            assertFalse(order.hasShortage());
        }
    }

    // ==================== HELPERS ====================

    private Order createValidOrder() {
        return Order.create(
            CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_EMAIL, SHIPPING_ADDRESS,
            "MEDIUM", LocalDate.now().plusDays(3), LocalDate.now().plusDays(5),
            WAREHOUSE_ID, "Notas de prueba"
        );
    }

    private Order createOrderWithLine() {
        Order order = createValidOrder();
        OrderLine line = OrderLine.create(
            UUID.randomUUID().toString(),
            "SKU-TEST-001",
            new BigDecimal("10"),
            LocalDate.now().plusDays(5),
            "Notas de línea"
        );
        order.addLine(line);
        return order;
    }

    private OrderLine createValidOrderLine() {
        return OrderLine.create(
            UUID.randomUUID().toString(),
            "SKU-001",
            new BigDecimal("10"),
            LocalDate.now().plusDays(5),
            null
        );
    }
}
