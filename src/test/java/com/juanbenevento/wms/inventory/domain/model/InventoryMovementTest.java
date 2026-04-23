package com.juanbenevento.wms.inventory.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("InventoryMovement Entity")
class InventoryMovementTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("receive() creates RECEIPT movement")
        void testReceive() {
            var movement = InventoryMovement.receive("LOT-001", new BigDecimal("100"), "LOC-01", "juan.martinez");

            assertThat(movement.getType()).isEqualTo(MovementType.RECEIPT);
            assertThat(movement.getLotNumber()).isEqualTo("LOT-001");
            assertThat(movement.getQuantity()).isEqualByComparingTo(new BigDecimal("100"));
            assertThat(movement.getToLocation()).isEqualTo("LOC-01");
            assertThat(movement.getFromLocation()).isNull();
            assertThat(movement.getPerformedBy()).isEqualTo("juan.martinez");
        }

        @Test
        @DisplayName("issue() creates ISSUE movement")
        void testIssue() {
            var movement = InventoryMovement.issue("LOT-001", new BigDecimal("50"), "LOC-01", "Order ORD-001", "juan.martinez");

            assertThat(movement.getType()).isEqualTo(MovementType.ISSUE);
            assertThat(movement.getFromLocation()).isEqualTo("LOC-01");
            assertThat(movement.getToLocation()).isNull();
            assertThat(movement.getReason()).isEqualTo("Order ORD-001");
        }

        @Test
        @DisplayName("transfer() creates TRANSFER movement")
        void testTransfer() {
            var movement = InventoryMovement.transfer("LOT-001", new BigDecimal("25"), "LOC-01", "LOC-02", "juan.martinez");

            assertThat(movement.getType()).isEqualTo(MovementType.TRANSFER);
            assertThat(movement.getFromLocation()).isEqualTo("LOC-01");
            assertThat(movement.getToLocation()).isEqualTo("LOC-02");
        }

        @Test
        @DisplayName("adjustment() creates ADJUSTMENT movement")
        void testAdjustment() {
            var movement = InventoryMovement.adjustment("LOT-001", new BigDecimal("-2"), "LOC-01", "Cycle count", "juan.martinez");

            assertThat(movement.getType()).isEqualTo(MovementType.ADJUSTMENT);
            assertThat(movement.getReason()).isEqualTo("Cycle count");
        }
    }

    @Nested
    @DisplayName("Immutable Copy Methods")
    class ImmutableCopies {

        @Test
        @DisplayName("withTemperature() sets temperature")
        void testWithTemperature() {
            var movement = InventoryMovement.receive("LOT-001", new BigDecimal("100"), "LOC-01", "juan");
            var withTemp = movement.withTemperature(new BigDecimal("5.5"));

            assertThat(withTemp.getTemperatureAtMovement()).isEqualByComparingTo(new BigDecimal("5.5"));
            // Original unchanged
            assertThat(movement.getTemperatureAtMovement()).isNull();
        }

        @Test
        @DisplayName("withWeight() sets weight")
        void testWithWeight() {
            var movement = InventoryMovement.receive("LOT-001", new BigDecimal("100"), "LOC-01", "juan");
            var withWeight = movement.withWeight(new BigDecimal("10.500"));

            assertThat(withWeight.getWeightAtMovement()).isEqualByComparingTo(new BigDecimal("10.500"));
            assertThat(movement.getWeightAtMovement()).isNull();
        }

        @Test
        @DisplayName("withCertificate() sets certificate URL")
        void testWithCertificate() {
            var movement = InventoryMovement.receive("LOT-001", new BigDecimal("100"), "LOC-01", "juan");
            var withCert = movement.withCertificate("https://senasa.gob.ar/cert/123");

            assertThat(withCert.getCertificateUrl()).isEqualTo("https://senasa.gob.ar/cert/123");
            assertThat(movement.getCertificateUrl()).isNull();
        }

        @Test
        @DisplayName("fluent chaining works")
        void testFluentChaining() {
            var movement = InventoryMovement.receive("LOT-001", new BigDecimal("100"), "LOC-01", "juan")
                    .withTemperature(new BigDecimal("4.0"))
                    .withWeight(new BigDecimal("10.500"))
                    .withCertificate("https://senasa.gob.ar/cert/456");

            assertThat(movement.getTemperatureAtMovement()).isEqualByComparingTo(new BigDecimal("4.0"));
            assertThat(movement.getWeightAtMovement()).isEqualByComparingTo(new BigDecimal("10.500"));
            assertThat(movement.getCertificateUrl()).isEqualTo("https://senasa.gob.ar/cert/456");
        }
    }
}