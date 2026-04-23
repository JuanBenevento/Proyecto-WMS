package com.juanbenevento.wms.inventory.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Port (interface) for cold chain temperature monitoring.
 * Records temperature readings and validates compliance.
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
public interface TemperatureMonitoringStrategy {

    /**
     * Records a temperature reading for a lot.
     *
     * @param lotNumber the lot being monitored
     * @param temperature the recorded temperature
     * @param timestamp when the reading was taken
     * @param location where the reading was taken
     */
    void recordTemperature(String lotNumber, BigDecimal temperature, LocalDateTime timestamp, String location);

    /**
     * Checks if a lot's temperature is within its required range.
     *
     * @param lotNumber the lot to check
     * @return true if within range or no requirement
     */
    boolean checkRange(String lotNumber);

    /**
     * Sends an alert if temperature is out of range.
     *
     * @param lotNumber the lot with temperature violation
     * @param currentTemp the recorded temperature
     */
    void alertIfOutOfRange(String lotNumber, BigDecimal currentTemp);

    /**
     * Returns the strategy name.
     *
     * @return strategy name
     */
    String getStrategyName();

    /**
     * Checks if this strategy is enabled.
     *
     * @return true if enabled
     */
    boolean isEnabled();
}