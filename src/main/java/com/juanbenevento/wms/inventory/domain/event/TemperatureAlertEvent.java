package com.juanbenevento.wms.inventory.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain event emitted when a temperature reading falls outside the
 * acceptable range for a lot's storage requirements.
 * Triggers alerts for quality assurance and corrective action.
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
public record TemperatureAlertEvent(
        String lotNumber,
        String location,
        BigDecimal recordedTemp,
        BigDecimal minTemp,
        BigDecimal maxTemp,
        LocalDateTime recordedAt
) {}