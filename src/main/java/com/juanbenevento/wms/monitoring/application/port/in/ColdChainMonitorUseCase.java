package com.juanbenevento.wms.monitoring.application.port.in;

import com.juanbenevento.wms.monitoring.application.port.in.dto.TemperatureAlertDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Use case for cold chain monitoring and alerts.
 */
public interface ColdChainMonitorUseCase {

    /**
     * Record a temperature reading.
     */
    void recordTemperature(String locationCode, LocalDateTime timestamp, Double temperature);

    /**
     * Get all active alerts.
     */
    List<TemperatureAlertDto> getActiveAlerts();

    /**
     * Get alerts for a specific location.
     */
    List<TemperatureAlertDto> getAlertsByLocation(String locationCode);

    /**
     * Get temperature history for a location.
     */
    List<TemperatureReadingDto> getTemperatureHistory(String locationCode, LocalDateTime from, LocalDateTime to);

    /**
     * Acknowledge an alert.
     */
    void acknowledgeAlert(String alertId, String acknowledgedBy);
}