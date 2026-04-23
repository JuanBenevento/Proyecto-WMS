package com.juanbenevento.wms.monitoring.application.port.in.dto;

import java.time.LocalDateTime;

/**
 * Temperature alert DTO.
 */
public record TemperatureAlertDto(
    String alertId,
    String locationCode,
    String zoneName,
    String alertType,  // HIGH_TEMPERATURE, LOW_TEMPERATURE, OUT_OF_RANGE
    Double currentTemperature,
    Double minAllowed,
    Double maxAllowed,
    LocalDateTime triggeredAt,
    LocalDateTime acknowledgedAt,
    String acknowledgedBy,
    String status  // ACTIVE, ACKNOWLEDGED, RESOLVED
) {}