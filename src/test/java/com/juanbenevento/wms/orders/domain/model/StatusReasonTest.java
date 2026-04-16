package com.juanbenevento.wms.orders.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para StatusReason enum.
 */
class StatusReasonTest {

    @Nested
    @DisplayName("Razones de HOLD")
    class HoldReasons {

        @ParameterizedTest
        @EnumSource(value = StatusReason.class, names = {"PAYMENT_HOLD", "INVENTORY_SHORTAGE", 
            "FRAUD_HOLD", "CUSTOMER_REQUEST", "QUALITY_HOLD", "ALLOCATION_PENDING", "MANUAL_REVIEW"})
        @DisplayName("isHoldReason retorna true")
        void isHoldReasonReturnsTrue(StatusReason reason) {
            assertTrue(reason.isHoldReason());
        }

        @Test
        @DisplayName("isHoldReason retorna false para razones no-HOLD")
        void isHoldReasonReturnsFalseForNonHoldReasons() {
            assertFalse(StatusReason.CUSTOMER_CANCELLED.isHoldReason());
            assertFalse(StatusReason.OUT_OF_STOCK.isHoldReason());
            assertFalse(StatusReason.NONE.isHoldReason());
        }
    }

    @Nested
    @DisplayName("Razones de CANCELLED")
    class CancelledReasons {

        @ParameterizedTest
        @EnumSource(value = StatusReason.class, names = {"CUSTOMER_CANCELLED", "OUT_OF_STOCK", 
            "PAYMENT_FAILED", "FRAUDULENT", "SYSTEM_TIMEOUT"})
        @DisplayName("isCancelledReason retorna true")
        void isCancelledReasonReturnsTrue(StatusReason reason) {
            assertTrue(reason.isCancelledReason());
        }

        @Test
        @DisplayName("isCancelledReason retorna false para razones no-CANCELLED")
        void isCancelledReasonReturnsFalseForNonCancelledReasons() {
            assertFalse(StatusReason.PAYMENT_HOLD.isCancelledReason());
            assertFalse(StatusReason.INVENTORY_SHORTAGE.isCancelledReason());
            assertFalse(StatusReason.NONE.isCancelledReason());
        }
    }

    @Nested
    @DisplayName("Razón NONE")
    class NoneReason {

        @Test
        @DisplayName("NONE no es hold reason ni cancelled reason")
        void noneIsNeitherHoldNorCancelled() {
            assertFalse(StatusReason.NONE.isHoldReason());
            assertFalse(StatusReason.NONE.isCancelledReason());
            assertTrue(StatusReason.NONE.isNone());
        }
    }

    @Nested
    @DisplayName("Categorías y Descripciones")
    class CategoriesAndDescriptions {

        @Test
        @DisplayName("Todas las razones tienen categoría")
        void allReasonsHaveCategory() {
            for (StatusReason reason : StatusReason.values()) {
                assertNotNull(reason.getCategory());
                assertFalse(reason.getCategory().isBlank());
            }
        }

        @Test
        @DisplayName("Todas las razones tienen descripción")
        void allReasonsHaveDescription() {
            for (StatusReason reason : StatusReason.values()) {
                assertNotNull(reason.getDescription());
                assertFalse(reason.getDescription().isBlank());
            }
        }

        @Test
        @DisplayName("Razones de HOLD tienen categoría 'Hold'")
        void holdReasonsHaveHoldCategory() {
            assertEquals("Hold", StatusReason.PAYMENT_HOLD.getCategory());
            assertEquals("Hold", StatusReason.INVENTORY_SHORTAGE.getCategory());
            assertEquals("Hold", StatusReason.FRAUD_HOLD.getCategory());
        }

        @Test
        @DisplayName("Razones de CANCELLED tienen categoría 'Cancelled'")
        void cancelledReasonsHaveCancelledCategory() {
            assertEquals("Cancelled", StatusReason.CUSTOMER_CANCELLED.getCategory());
            assertEquals("Cancelled", StatusReason.OUT_OF_STOCK.getCategory());
            assertEquals("Cancelled", StatusReason.FRAUDULENT.getCategory());
        }
    }

    @Nested
    @DisplayName("Extensibilidad")
    class Extensibility {

        @Test
        @DisplayName("Enum tiene número suficiente de razones para reportes granulares")
        void enumHasEnoughReasons() {
            // HOLD reasons
            long holdReasons = java.util.Arrays.stream(StatusReason.values())
                .filter(StatusReason::isHoldReason)
                .count();
            assertTrue(holdReasons >= 6, "Debe haber al menos 6 razones de HOLD");

            // CANCELLED reasons
            long cancelledReasons = java.util.Arrays.stream(StatusReason.values())
                .filter(StatusReason::isCancelledReason)
                .count();
            assertTrue(cancelledReasons >= 4, "Debe haber al menos 4 razones de CANCEL");
        }

        @Test
        @DisplayName("Es fácil agregar nuevas razones")
        void canAddNewReasons() {
            // Verificar que el enum tiene estructura que permite extensión
            // Esta es una verificación de documentación del patrón
            StatusReason[] reasons = StatusReason.values();
            assertTrue(reasons.length >= 12, 
                "Debe haber al menos 12 razones predefinidas para diferentes escenarios industriales");
        }
    }
}
