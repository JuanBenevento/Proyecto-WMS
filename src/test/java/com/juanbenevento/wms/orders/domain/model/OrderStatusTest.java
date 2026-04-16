package com.juanbenevento.wms.orders.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para el enum OrderStatus y sus transiciones.
 */
class OrderStatusTest {

    @Nested
    @DisplayName("Transiciones desde CREATED")
    class FromCreated {

        @Test
        @DisplayName("Puede transicionar a CONFIRMED")
        void canTransitionToConfirmed() {
            assertTrue(OrderStatus.CREATED.canTransitionTo(OrderStatus.CONFIRMED));
        }

        @Test
        @DisplayName("Puede transicionar a CANCELLED")
        void canTransitionToCancelled() {
            assertTrue(OrderStatus.CREATED.canTransitionTo(OrderStatus.CANCELLED));
        }

        @Test
        @DisplayName("No puede transicionar directamente a PENDING")
        void cannotTransitionToPending() {
            assertFalse(OrderStatus.CREATED.canTransitionTo(OrderStatus.PENDING));
        }

        @Test
        @DisplayName("No puede transicionar a estados avanzados")
        void cannotTransitionToAdvancedStates() {
            assertFalse(OrderStatus.CREATED.canTransitionTo(OrderStatus.PICKING));
            assertFalse(OrderStatus.CREATED.canTransitionTo(OrderStatus.SHIPPED));
            assertFalse(OrderStatus.CREATED.canTransitionTo(OrderStatus.DELIVERED));
        }
    }

    @Nested
    @DisplayName("Transiciones desde CONFIRMED")
    class FromConfirmed {

        @Test
        @DisplayName("Puede transicionar a PENDING")
        void canTransitionToPending() {
            assertTrue(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.PENDING));
        }

        @Test
        @DisplayName("Puede transicionar a HOLD")
        void canTransitionToHold() {
            assertTrue(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.HOLD));
        }

        @Test
        @DisplayName("Puede transicionar a CANCELLED")
        void canTransitionToCancelled() {
            assertTrue(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.CANCELLED));
        }
    }

    @Nested
    @DisplayName("Transiciones desde PENDING")
    class FromPending {

        @Test
        @DisplayName("Puede transicionar a ALLOCATED")
        void canTransitionToAllocated() {
            assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.ALLOCATED));
        }

        @Test
        @DisplayName("Puede transicionar a HOLD")
        void canTransitionToHold() {
            assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.HOLD));
        }

        @Test
        @DisplayName("Puede transicionar a CANCELLED")
        void canTransitionToCancelled() {
            assertTrue(OrderStatus.PENDING.canTransitionTo(OrderStatus.CANCELLED));
        }
    }

    @Nested
    @DisplayName("Transiciones desde HOLD")
    class FromHold {

        @Test
        @DisplayName("Puede transicionar a PENDING (release)")
        void canTransitionToPending() {
            assertTrue(OrderStatus.HOLD.canTransitionTo(OrderStatus.PENDING));
        }

        @Test
        @DisplayName("Puede transicionar a ALLOCATED")
        void canTransitionToAllocated() {
            assertTrue(OrderStatus.HOLD.canTransitionTo(OrderStatus.ALLOCATED));
        }

        @Test
        @DisplayName("Puede transicionar a CANCELLED")
        void canTransitionToCancelled() {
            assertTrue(OrderStatus.HOLD.canTransitionTo(OrderStatus.CANCELLED));
        }

        @Test
        @DisplayName("No puede transicionar a PICKING directamente")
        void cannotTransitionToPicking() {
            assertFalse(OrderStatus.HOLD.canTransitionTo(OrderStatus.PICKING));
        }
    }

    @Nested
    @DisplayName("Transiciones desde ALLOCATED")
    class FromAllocated {

        @Test
        @DisplayName("Puede transicionar a PICKING")
        void canTransitionToPicking() {
            assertTrue(OrderStatus.ALLOCATED.canTransitionTo(OrderStatus.PICKING));
        }

        @Test
        @DisplayName("Puede transicionar a CANCELLED")
        void canTransitionToCancelled() {
            assertTrue(OrderStatus.ALLOCATED.canTransitionTo(OrderStatus.CANCELLED));
        }

        @Test
        @DisplayName("No puede transicionar a PACKED directamente")
        void cannotTransitionToPacked() {
            assertFalse(OrderStatus.ALLOCATED.canTransitionTo(OrderStatus.PACKED));
        }
    }

    @Nested
    @DisplayName("Transiciones desde PICKING")
    class FromPicking {

        @Test
        @DisplayName("Puede transicionar a PACKED")
        void canTransitionToPacked() {
            assertTrue(OrderStatus.PICKING.canTransitionTo(OrderStatus.PACKED));
        }

        @Test
        @DisplayName("Puede transicionar a CANCELLED")
        void canTransitionToCancelled() {
            assertTrue(OrderStatus.PICKING.canTransitionTo(OrderStatus.CANCELLED));
        }
    }

    @Nested
    @DisplayName("Estados Terminales")
    class TerminalStates {

        @Test
        @DisplayName("DELIVERED es terminal")
        void deliveredIsTerminal() {
            assertTrue(OrderStatus.DELIVERED.isTerminal());
            assertTrue(OrderStatus.DELIVERED.getValidTransitions().isEmpty());
        }

        @Test
        @DisplayName("CANCELLED es terminal")
        void cancelledIsTerminal() {
            assertTrue(OrderStatus.CANCELLED.isTerminal());
            assertTrue(OrderStatus.CANCELLED.getValidTransitions().isEmpty());
        }

        @ParameterizedTest
        @EnumSource(value = OrderStatus.class, names = {"DELIVERED", "CANCELLED"})
        @DisplayName("Estados terminales no pueden transicionar")
        void terminalStatesCannotTransition(OrderStatus status) {
            for (OrderStatus target : OrderStatus.values()) {
                assertFalse(status.canTransitionTo(target),
                    () -> status + " no debería poder transicionar a " + target);
            }
        }
    }

    @Nested
    @DisplayName("Helpers")
    class Helpers {

        @Test
        @DisplayName("isActive retorna true para estados activos")
        void isActiveReturnsTrueForActiveStates() {
            assertTrue(OrderStatus.CREATED.isActive());
            assertTrue(OrderStatus.CONFIRMED.isActive());
            assertTrue(OrderStatus.PENDING.isActive());
            assertTrue(OrderStatus.ALLOCATED.isActive());
            assertTrue(OrderStatus.PICKING.isActive());
            assertTrue(OrderStatus.PACKED.isActive());
            assertTrue(OrderStatus.SHIPPED.isActive());
        }

        @Test
        @DisplayName("isActive retorna false para estados no activos")
        void isActiveReturnsFalseForNonActiveStates() {
            assertFalse(OrderStatus.HOLD.isActive()); // Espera
            assertFalse(OrderStatus.DELIVERED.isActive()); // Terminal
            assertFalse(OrderStatus.CANCELLED.isActive()); // Terminal
        }

        @Test
        @DisplayName("requiresIntervention retorna true para HOLD")
        void requiresInterventionForHold() {
            assertTrue(OrderStatus.HOLD.requiresIntervention());
            
            for (OrderStatus status : OrderStatus.values()) {
                if (status != OrderStatus.HOLD) {
                    assertFalse(status.requiresIntervention(),
                        () -> status + " no debería requerir intervención");
                }
            }
        }
    }
}
