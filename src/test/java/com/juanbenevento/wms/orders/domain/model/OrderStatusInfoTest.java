package com.juanbenevento.wms.orders.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para el Value Object OrderStatusInfo.
 */
class OrderStatusInfoTest {

    @Nested
    @DisplayName("Creación")
    class Creation {

        @Test
        @DisplayName("Debe crear con estado simple")
        void shouldCreateWithSimpleStatus() {
            OrderStatusInfo info = OrderStatusInfo.of(OrderStatus.PENDING);

            assertEquals(OrderStatus.PENDING, info.getStatus());
            assertEquals(StatusReason.NONE, info.getReason());
            assertNotNull(info.getChangedAt());
        }

        @Test
        @DisplayName("Debe crear con estado y razón")
        void shouldCreateWithStatusAndReason() {
            OrderStatusInfo info = OrderStatusInfo.of(OrderStatus.HOLD, StatusReason.INVENTORY_SHORTAGE);

            assertEquals(OrderStatus.HOLD, info.getStatus());
            assertEquals(StatusReason.INVENTORY_SHORTAGE, info.getReason());
        }

        @Test
        @DisplayName("Debe crear con toda la información")
        void shouldCreateWithFullInfo() {
            Map<String, Object> metadata = Map.of("previousQty", 100, "newQty", 80);
            OrderStatusInfo info = OrderStatusInfo.of(
                OrderStatus.HOLD, 
                StatusReason.PAYMENT_HOLD, 
                metadata, 
                "user-123"
            );

            assertEquals(OrderStatus.HOLD, info.getStatus());
            assertEquals(StatusReason.PAYMENT_HOLD, info.getReason());
            assertEquals("user-123", info.getChangedBy());
            assertEquals(100, info.getMetadataValue("previousQty"));
            assertEquals(80, info.getMetadataValue("newQty"));
        }

        @Test
        @DisplayName("Debe rechazar estado null")
        void shouldRejectNullStatus() {
            assertThrows(NullPointerException.class, () -> OrderStatusInfo.of(null));
        }

        @Test
        @DisplayName("Debe usar NONE si razón es null")
        void shouldUseNoneIfReasonIsNull() {
            OrderStatusInfo info = OrderStatusInfo.of(OrderStatus.PENDING, null);

            assertEquals(StatusReason.NONE, info.getReason());
        }
    }

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("hold() crea estado HOLD con razón válida")
        void shouldCreateHoldWithValidReason() {
            OrderStatusInfo info = OrderStatusInfo.hold(StatusReason.PAYMENT_HOLD);

            assertEquals(OrderStatus.HOLD, info.getStatus());
            assertEquals(StatusReason.PAYMENT_HOLD, info.getReason());
        }

        @Test
        @DisplayName("hold() rechaza razón no-HOLD")
        void shouldRejectNonHoldReasonForHold() {
            assertThrows(IllegalArgumentException.class, () ->
                OrderStatusInfo.hold(StatusReason.CUSTOMER_CANCELLED));
        }

        @Test
        @DisplayName("cancelled() crea estado CANCELLED con razón válida")
        void shouldCreateCancelledWithValidReason() {
            OrderStatusInfo info = OrderStatusInfo.cancelled(StatusReason.OUT_OF_STOCK);

            assertEquals(OrderStatus.CANCELLED, info.getStatus());
            assertEquals(StatusReason.OUT_OF_STOCK, info.getReason());
        }

        @Test
        @DisplayName("cancelled() rechaza razón no-CANCELLED")
        void shouldRejectNonCancelledReasonForCancelled() {
            assertThrows(IllegalArgumentException.class, () ->
                OrderStatusInfo.cancelled(StatusReason.PAYMENT_HOLD));
        }

        @Test
        @DisplayName("NONE es válido para cualquier estado")
        void noneIsValidForAnyStatus() {
            OrderStatusInfo info = OrderStatusInfo.of(OrderStatus.PENDING, StatusReason.NONE);
            assertEquals(StatusReason.NONE, info.getReason());
        }
    }

    @Nested
    @DisplayName("Metadata")
    class Metadata {

        @Test
        @DisplayName("Debe retornar null para clave inexistente")
        void shouldReturnNullForNonexistentKey() {
            OrderStatusInfo info = OrderStatusInfo.of(OrderStatus.PENDING);
            
            assertNull(info.getMetadataValue("inexistent"));
        }

        @Test
        @DisplayName("getMetadataString retorna String")
        void shouldReturnStringValue() {
            Map<String, Object> metadata = Map.of("carrierId", "CARRIER-001");
            OrderStatusInfo info = OrderStatusInfo.of(OrderStatus.SHIPPED, StatusReason.NONE, 
                                                    metadata, null);

            assertEquals("CARRIER-001", info.getMetadataString("carrierId"));
        }

        @Test
        @DisplayName("getMetadataString retorna null para clave inexistente")
        void shouldReturnNullStringForNonexistentKey() {
            OrderStatusInfo info = OrderStatusInfo.of(OrderStatus.SHIPPED);
            
            assertNull(info.getMetadataString("inexistent"));
        }

        @Test
        @DisplayName("hasMetadata retorna true cuando hay metadata")
        void shouldReturnTrueWhenHasMetadata() {
            OrderStatusInfo info = OrderStatusInfo.of(OrderStatus.HOLD, StatusReason.INVENTORY_SHORTAGE,
                                                    Map.of("shortageQty", 5), null);
            assertTrue(info.hasMetadata());
        }

        @Test
        @DisplayName("hasMetadata retorna false cuando no hay metadata")
        void shouldReturnFalseWhenNoMetadata() {
            OrderStatusInfo info = OrderStatusInfo.of(OrderStatus.PENDING);
            assertFalse(info.hasMetadata());
        }
    }

    @Nested
    @DisplayName("Convenience Methods")
    class ConvenienceMethods {

        @Test
        @DisplayName("isPending retorna true para PENDING")
        void isPendingReturnsTrueForPending() {
            OrderStatusInfo info = OrderStatusInfo.of(OrderStatus.PENDING);
            assertTrue(info.isPending());
        }

        @Test
        @DisplayName("isHold retorna true para HOLD")
        void isHoldReturnsTrueForHold() {
            OrderStatusInfo info = OrderStatusInfo.of(OrderStatus.HOLD, StatusReason.INVENTORY_SHORTAGE);
            assertTrue(info.isHold());
        }

        @Test
        @DisplayName("isCancelled retorna true para CANCELLED")
        void isCancelledReturnsTrueForCancelled() {
            OrderStatusInfo info = OrderStatusInfo.of(OrderStatus.CANCELLED, StatusReason.OUT_OF_STOCK);
            assertTrue(info.isCancelled());
        }

        @Test
        @DisplayName("isTerminal retorna true para estados terminales")
        void isTerminalReturnsTrueForTerminalStates() {
            assertTrue(OrderStatusInfo.of(OrderStatus.DELIVERED).isTerminal());
            assertTrue(OrderStatusInfo.of(OrderStatus.CANCELLED, StatusReason.CUSTOMER_CANCELLED).isTerminal());
        }

        @Test
        @DisplayName("isTerminal retorna false para estados no-terminales")
        void isTerminalReturnsFalseForNonTerminalStates() {
            assertFalse(OrderStatusInfo.of(OrderStatus.PENDING).isTerminal());
            assertFalse(OrderStatusInfo.of(OrderStatus.PICKING).isTerminal());
            assertFalse(OrderStatusInfo.of(OrderStatus.HOLD, StatusReason.PAYMENT_HOLD).isTerminal());
        }
    }

    @Nested
    @DisplayName("Withers")
    class Withers {

        @Test
        @DisplayName("withStatus crea nueva instancia con estado diferente")
        void shouldCreateNewInstanceWithDifferentStatus() {
            OrderStatusInfo original = OrderStatusInfo.of(OrderStatus.PENDING, StatusReason.NONE);
            OrderStatusInfo updated = original.withStatus(OrderStatus.ALLOCATED);

            assertEquals(OrderStatus.PENDING, original.getStatus());
            assertEquals(OrderStatus.ALLOCATED, updated.getStatus());
        }

        @Test
        @DisplayName("withReason crea nueva instancia con razón diferente")
        void shouldCreateNewInstanceWithDifferentReason() {
            OrderStatusInfo original = OrderStatusInfo.of(OrderStatus.HOLD, StatusReason.PAYMENT_HOLD);
            OrderStatusInfo updated = original.withReason(StatusReason.INVENTORY_SHORTAGE);

            assertEquals(StatusReason.PAYMENT_HOLD, original.getReason());
            assertEquals(StatusReason.INVENTORY_SHORTAGE, updated.getReason());
        }

        @Test
        @DisplayName("withChangedBy actualiza el usuario")
        void shouldUpdateChangedBy() {
            OrderStatusInfo original = OrderStatusInfo.of(OrderStatus.PENDING);
            OrderStatusInfo updated = original.withChangedBy("user-123");

            assertNull(original.getChangedBy());
            assertEquals("user-123", updated.getChangedBy());
        }

        @Test
        @DisplayName("withMetadata crea nueva instancia con metadata diferente")
        void shouldCreateNewInstanceWithDifferentMetadata() {
            OrderStatusInfo original = OrderStatusInfo.of(OrderStatus.HOLD, StatusReason.PAYMENT_HOLD,
                                                    Map.of("key", "value1"), null);
            OrderStatusInfo updated = original.withMetadata(Map.of("key", "value2"));

            assertEquals("value1", original.getMetadataString("key"));
            assertEquals("value2", updated.getMetadataString("key"));
        }
    }

    @Nested
    @DisplayName("Inmutabilidad")
    class Immutability {

        @Test
        @DisplayName("Instancia es inmutable - getters no retornan colecciones mutables")
        void instanceIsImmutable() {
            Map<String, Object> metadata = Map.of("key", "value");
            OrderStatusInfo info = OrderStatusInfo.of(OrderStatus.PENDING, StatusReason.NONE, 
                                                    metadata, null);

            // Obtener metadata y tratar de modificar
            Map<String, Object> retrieved = info.getMetadata();
            assertThrows(UnsupportedOperationException.class, () -> retrieved.put("new", "value"));
        }

        @Test
        @DisplayName("Withers no modifican la instancia original")
        void withersDoNotModifyOriginal() {
            OrderStatusInfo original = OrderStatusInfo.of(OrderStatus.PENDING, StatusReason.NONE);

            original.withStatus(OrderStatus.ALLOCATED);
            original.withReason(StatusReason.NONE);
            original.withChangedBy("user");

            // Original no cambió
            assertEquals(OrderStatus.PENDING, original.getStatus());
            assertEquals(StatusReason.NONE, original.getReason());
            assertNull(original.getChangedBy());
        }
    }
}
