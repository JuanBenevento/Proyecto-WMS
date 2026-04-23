package com.juanbenevento.wms.inventory.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TemperatureRange Value Object")
class TemperatureRangeTest {

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("of() creates valid range")
        void testOf_valid() {
            TemperatureRange range = TemperatureRange.of(new BigDecimal("2"), new BigDecimal("8"));

            assertThat(range.getMinTemperature()).isEqualByComparingTo(new BigDecimal("2"));
            assertThat(range.getMaxTemperature()).isEqualByComparingTo(new BigDecimal("8"));
        }

        @Test
        @DisplayName("of() throws for null values")
        void testOf_null() {
            assertThatThrownBy(() -> TemperatureRange.of(null, new BigDecimal("8")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be null");

            assertThatThrownBy(() -> TemperatureRange.of(new BigDecimal("2"), null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("of() throws when min > max")
        void testOf_minGreaterThanMax() {
            assertThatThrownBy(() -> TemperatureRange.of(new BigDecimal("10"), new BigDecimal("2")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cannot be greater");
        }

        @Test
        @DisplayName("of() throws for out of range values")
        void testOf_outOfRange() {
            assertThatThrownBy(() -> TemperatureRange.of(new BigDecimal("-60"), new BigDecimal("-50")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("between");
        }
    }

    @Nested
    @DisplayName("Preset Factory Methods")
    class PresetFactories {

        @Test
        @DisplayName("coldStorage() returns 2-10 range")
        void testColdStorage() {
            TemperatureRange range = TemperatureRange.coldStorage();

            assertThat(range.getMinTemperature()).isEqualByComparingTo(new BigDecimal("2"));
            assertThat(range.getMaxTemperature()).isEqualByComparingTo(new BigDecimal("10"));
        }

        @Test
        @DisplayName("frozenStorage() returns -25 to -18 range")
        void testFrozenStorage() {
            TemperatureRange range = TemperatureRange.frozenStorage();

            assertThat(range.getMinTemperature()).isEqualByComparingTo(new BigDecimal("-25"));
            assertThat(range.getMaxTemperature()).isEqualByComparingTo(new BigDecimal("-18"));
        }
    }

    @Nested
    @DisplayName("isWithinRange()")
    class IsWithinRange {

        @Test
        @DisplayName("returns true for temperature within range")
        void testTrue() {
            TemperatureRange range = TemperatureRange.coldStorage();

            assertThat(range.isWithinRange(new BigDecimal("5"))).isTrue();
            assertThat(range.isWithinRange(new BigDecimal("2"))).isTrue();  // inclusive
            assertThat(range.isWithinRange(new BigDecimal("10"))).isTrue(); // inclusive
        }

        @Test
        @DisplayName("returns false for temperature out of range")
        void testFalse() {
            TemperatureRange range = TemperatureRange.coldStorage();

            assertThat(range.isWithinRange(new BigDecimal("15"))).isFalse();
            assertThat(range.isWithinRange(new BigDecimal("-5"))).isFalse();
        }

        @Test
        @DisplayName("returns false for null temperature")
        void testNull() {
            TemperatureRange range = TemperatureRange.coldStorage();

            assertThat(range.isWithinRange(null)).isFalse();
        }
    }
}