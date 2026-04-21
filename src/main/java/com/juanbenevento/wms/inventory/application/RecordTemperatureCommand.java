package com.juanbenevento.wms.inventory.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Command for recording a temperature reading.
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
public record RecordTemperatureCommand(
        String lotNumber,
        BigDecimal temperature,
        LocalDateTime timestamp,
        String location,
        String recordedBy
) {
    public static RecordTemperatureCommand of(
            String lotNumber, BigDecimal temperature, String location, String recordedBy
    ) {
        return new RecordTemperatureCommand(lotNumber, temperature, LocalDateTime.now(), location, recordedBy);
    }
}