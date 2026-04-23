package com.juanbenevento.wms.inventory.domain.model;

import com.juanbenevento.wms.inventory.domain.exception.ExpiredLotException;
import com.juanbenevento.wms.inventory.domain.exception.LotQuarantineException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Lot Aggregate")
class LotTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("create() with required fields")
        void testCreate_requiredFields() {
            Lot lot = Lot.create("LOT-001", "TOMATE-001", "BATCH-2026", LocalDate.now(), "Fincas El Sol");

            assertThat(lot.getLotNumber()).isEqualTo("LOT-001");
            assertThat(lot.getProductSku()).isEqualTo("TOMATE-001");
            assertThat(lot.getStatus()).isEqualTo(LotStatus.ACTIVE);
        }

        @Test
        @DisplayName("create() with full options")
        void testCreate_fullOptions() {
            TemperatureRange range = TemperatureRange.coldStorage();

            Lot lot = Lot.create(
                    "LOT-002", "VACUNA-001", "BATCH-VAC",
                    LocalDate.of(2026, 3, 15),
                    "Laboratorio ABC",
                    LocalDate.of(2026, 6, 15),
                    range,
                    new BigDecimal("10.500"),
                    new BigDecimal("11.000"),
                    java.util.Map.of("certificado", "SENASA-123")
            );

            assertThat(lot.getBatchNumber()).isEqualTo("BATCH-VAC");
            assertThat(lot.getTemperatureRange()).isNotNull();
            assertThat(lot.getNetWeight()).isEqualByComparingTo(new BigDecimal("10.500"));
            assertThat(lot.getMetadata()).containsEntry("certificado", "SENASA-123");
        }

        @Test
        @DisplayName("create() throws for null lotNumber")
        void testCreate_nullLotNumber() {
            assertThatThrownBy(() -> Lot.create(null, "SKU-001", "BATCH", LocalDate.now(), "Origin"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Lot number is required");
        }
    }

    @Nested
    @DisplayName("Status Transitions")
    class StatusTransitions {

        @Test
        @DisplayName("markAsQuarantined() changes status to QUARANTINE")
        void testMarkAsQuarantined() {
            Lot lot = Lot.create("LOT-Q", "TOMATE-001", "BATCH", LocalDate.now(), "Origin");
            Lot quarantined = lot.markAsQuarantined();

            assertThat(quarantined.getStatus()).isEqualTo(LotStatus.QUARANTINE);
        }

        @Test
        @DisplayName("releaseFromQuarantine() changes status to ACTIVE")
        void testReleaseFromQuarantine() {
            Lot lot = Lot.create("LOT-Q", "TOMATE-001", "BATCH", LocalDate.now(), "Origin");
            Lot quarantined = lot.markAsQuarantined();
            Lot released = quarantined.releaseFromQuarantine();

            assertThat(released.getStatus()).isEqualTo(LotStatus.ACTIVE);
        }

        @Test
        @DisplayName("releaseFromQuarantine() throws if not quarantined")
        void testReleaseFromQuarantine_throws() {
            Lot lot = Lot.create("LOT-001", "SKU", "BATCH", LocalDate.now(), "Origin");

            assertThatThrownBy(lot::releaseFromQuarantine)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only QUARANTINE lots can be released");
        }

        @Test
        @DisplayName("markAsExpired() changes status to EXPIRED")
        void testMarkAsExpired() {
            Lot lot = Lot.create("LOT-E", "TOMATE-001", "BATCH", LocalDate.now(), "Origin");
            Lot expired = lot.markAsExpired();

            assertThat(expired.getStatus()).isEqualTo(LotStatus.EXPIRED);
        }
    }

    @Nested
    @DisplayName("canIssue()")
    class CanIssue {

        @Test
        @DisplayName("canIssue() returns true for ACTIVE lot")
        void testCanIssue_active() {
            Lot lot = Lot.create("LOT-001", "SKU", "BATCH", LocalDate.now(), "Origin");
            assertThat(lot.canIssue()).isTrue();
        }

        @Test
        @DisplayName("canIssue() throws LotQuarantineException for QUARANTINE lot")
        void testCanIssue_quarantine() {
            Lot lot = Lot.create("LOT-Q", "SKU", "BATCH", LocalDate.now(), "Origin").markAsQuarantined();

            assertThatThrownBy(lot::canIssue)
                    .isInstanceOf(LotQuarantineException.class)
                    .hasMessageContaining("LOT-Q");
        }

        @Test
        @DisplayName("canIssue() throws ExpiredLotException for EXPIRED lot")
        void testCanIssue_expired() {
            Lot lot = Lot.create("LOT-E", "SKU", "BATCH", LocalDate.now(), "Origin").markAsExpired();

            assertThatThrownBy(lot::canIssue)
                    .isInstanceOf(ExpiredLotException.class);
        }
    }

    @Nested
    @DisplayName("Temperature Validation")
    class TemperatureValidation {

        @Test
        @DisplayName("isTemperatureWithinRange() returns true when in range")
        void testWithinRange_true() {
            Lot lot = Lot.create("LOT-001", "SKU", "BATCH", LocalDate.now(), "Origin",
                    null, TemperatureRange.coldStorage(), null, null, null);

            assertThat(lot.isTemperatureWithinRange(new BigDecimal("5.0"))).isTrue();
        }

        @Test
        @DisplayName("isTemperatureWithinRange() returns false when out of range")
        void testWithinRange_false() {
            Lot lot = Lot.create("LOT-001", "SKU", "BATCH", LocalDate.now(), "Origin",
                    null, TemperatureRange.coldStorage(), null, null, null);

            assertThat(lot.isTemperatureWithinRange(new BigDecimal("15.0"))).isFalse();
        }

        @Test
        @DisplayName("isTemperatureWithinRange() returns true when no range defined")
        void testWithinRange_noRange() {
            Lot lot = Lot.create("LOT-001", "SKU", "BATCH", LocalDate.now(), "Origin");

            assertThat(lot.isTemperatureWithinRange(new BigDecimal("25.0"))).isTrue();
        }
    }
}