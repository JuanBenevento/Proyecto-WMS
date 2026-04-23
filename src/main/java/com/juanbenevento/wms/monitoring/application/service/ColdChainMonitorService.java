package com.juanbenevento.wms.monitoring.application.service;

import com.juanbenevento.wms.monitoring.application.port.in.ColdChainMonitorUseCase;
import com.juanbenevento.wms.monitoring.application.port.in.dto.TemperatureAlertDto;
import com.juanbenevento.wms.monitoring.application.port.in.dto.TemperatureReadingDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cold chain monitoring service implementation.
 * Monitors temperature sensitive inventory and triggers alerts.
 */
@Service
public class ColdChainMonitorService implements ColdChainMonitorUseCase {

    // In-memory storage for demo (use database in production)
    private final ConcurrentHashMap<String, TemperatureAlertDto> alerts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<TemperatureReadingDto>> readings = new ConcurrentHashMap<>();

    // Configurable thresholds per zone
    private static final double DEFAULT_MIN_TEMP = 2.0;
    private static final double DEFAULT_MAX_TEMP = 10.0;
    private static final double ALERT_THRESHOLD = 2.0; // Degrees beyond allowed

    @Override
    public void recordTemperature(String locationCode, LocalDateTime timestamp, Double temperature) {
        // Get thresholds for location (simplified - would query location config)
        double minTemp = DEFAULT_MIN_TEMP;
        double maxTemp = DEFAULT_MAX_TEMP;

        // Store reading
        readings.computeIfAbsent(locationCode, (String k) -> new ArrayList<>())
            .add(new TemperatureReadingDto(locationCode, timestamp, temperature, minTemp, maxTemp,
                temperature >= minTemp && temperature <= maxTemp));

        // Check for alert conditions
        if (temperature > maxTemp + ALERT_THRESHOLD) {
            createAlert(locationCode, "HIGH_TEMPERATURE", temperature, minTemp, maxTemp);
        } else if (temperature < minTemp - ALERT_THRESHOLD) {
            createAlert(locationCode, "LOW_TEMPERATURE", temperature, minTemp, maxTemp);
        }
    }

    private void createAlert(String locationCode, String alertType, double temp, double min, double max) {
        String alertKey = locationCode + "-" + alertType;

        if (!alerts.containsKey(alertKey)) {
            TemperatureAlertDto alert = new TemperatureAlertDto(
                alertKey,
                locationCode,
                "Zone-" + locationCode.charAt(0),
                alertType,
                temp,
                min,
                max,
                LocalDateTime.now(),
                null,
                null,
                "ACTIVE"
            );
            alerts.put(alertKey, alert);
        }
    }

    @Override
    public List<TemperatureAlertDto> getActiveAlerts() {
        return alerts.values().stream()
            .filter(a -> "ACTIVE".equals(a.status()))
            .toList();
    }

    @Override
    public List<TemperatureAlertDto> getAlertsByLocation(String locationCode) {
        return alerts.values().stream()
            .filter(a -> a.locationCode().equals(locationCode))
            .toList();
    }

    @Override
    public List<TemperatureReadingDto> getTemperatureHistory(String locationCode, LocalDateTime from, LocalDateTime to) {
        List<TemperatureReadingDto> result = readings.getOrDefault(locationCode, new ArrayList<>());
        return result.stream()
            .filter(r -> r.timestamp().isAfter(from) && r.timestamp().isBefore(to))
            .toList();
    }

    @Override
    public void acknowledgeAlert(String alertId, String acknowledgedBy) {
        TemperatureAlertDto existing = alerts.get(alertId);
        if (existing != null) {
            TemperatureAlertDto updated = new TemperatureAlertDto(
                existing.alertId(),
                existing.locationCode(),
                existing.zoneName(),
                existing.alertType(),
                existing.currentTemperature(),
                existing.minAllowed(),
                existing.maxAllowed(),
                existing.triggeredAt(),
                LocalDateTime.now(),
                acknowledgedBy,
                "ACKNOWLEDGED"
            );
            alerts.put(alertId, updated);
        }
    }
}