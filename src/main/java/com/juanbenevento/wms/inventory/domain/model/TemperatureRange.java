package com.juanbenevento.wms.inventory.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Value Object representing a temperature range for storage facilities.
 * Validates that min temperature is less than or equal to max temperature
 * and that both values are within reasonable storage limits (-50 to +50 Celsius).
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
@Getter
@EqualsAndHashCode
public final class TemperatureRange {

    private static final BigDecimal MIN_VALID = new BigDecimal("-50");
    private static final BigDecimal MAX_VALID = new BigDecimal("50");
    private static final BigDecimal COLD_MAX = new BigDecimal("10");
    private static final BigDecimal FROZEN_MAX = new BigDecimal("-18");

    private final BigDecimal minTemperature;
    private final BigDecimal maxTemperature;

    private TemperatureRange(BigDecimal minTemperature, BigDecimal maxTemperature) {
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
    }

    /**
     * Factory method to create a validated TemperatureRange.
     *
     * @param minTemperature the minimum temperature
     * @param maxTemperature the maximum temperature
     * @return a new TemperatureRange instance
     * @throws IllegalArgumentException if min > max or values are outside valid range
     */
    public static TemperatureRange of(BigDecimal minTemperature, BigDecimal maxTemperature) {
        if (minTemperature == null || maxTemperature == null) {
            throw new IllegalArgumentException("Temperature values cannot be null");
        }
        if (minTemperature.compareTo(maxTemperature) > 0) {
            throw new IllegalArgumentException(
                    String.format("Minimum temperature (%s) cannot be greater than maximum temperature (%s)",
                            minTemperature, maxTemperature));
        }
        if (minTemperature.compareTo(MIN_VALID) < 0 || maxTemperature.compareTo(MAX_VALID) > 0) {
            throw new IllegalArgumentException(
                    String.format("Temperature values must be between %s and %s Celsius",
                            MIN_VALID, MAX_VALID));
        }
        return new TemperatureRange(minTemperature, maxTemperature);
    }

    /**
     * Factory method for cold storage temperature range (typically 2-10 Celsius).
     * Common for dairy, fresh produce, and vaccines.
     *
     * @return a pre-configured cold storage TemperatureRange
     */
    public static TemperatureRange coldStorage() {
        return new TemperatureRange(new BigDecimal("2"), COLD_MAX);
    }

    /**
     * Factory method for frozen storage temperature range (typically -25 to -18 Celsius).
     * Common for frozen foods, biological samples, and ice cream.
     *
     * @return a pre-configured frozen storage TemperatureRange
     */
    public static TemperatureRange frozenStorage() {
        return new TemperatureRange(new BigDecimal("-25"), FROZEN_MAX);
    }

    /**
     * Checks if a given temperature falls within this range (inclusive).
     *
     * @param temperature the temperature to check
     * @return true if temperature is within min-max range, false otherwise
     */
    public boolean isWithinRange(BigDecimal temperature) {
        if (temperature == null) {
            return false;
        }
        return temperature.compareTo(minTemperature) >= 0
                && temperature.compareTo(maxTemperature) <= 0;
    }
}