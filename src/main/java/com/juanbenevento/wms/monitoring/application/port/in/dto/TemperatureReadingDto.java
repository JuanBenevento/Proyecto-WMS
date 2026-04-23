package com.juanbenevento.wms.monitoring.application.port.in.dto;

import java.time.LocalDateTime;

/**
 * Temperature reading DTO.
 */
public record TemperatureReadingDto(
    String locationCode,
    LocalDateTime timestamp,
    Double temperature,
    Double minAllowed,
    Double maxAllowed,
    boolean isWithinRange
) {}