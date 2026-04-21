package com.juanbenevento.wms.inventory.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Command for registering a receipt of goods.
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
public record RegisterReceiptCommand(
        String lotNumber,
        String productSku,
        BigDecimal quantity,
        String locationCode,
        String batchNumber,
        LocalDate productionDate,
        String origin,
        LocalDate expiryDate,
        BigDecimal minTemperature,
        BigDecimal maxTemperature,
        BigDecimal netWeight,
        BigDecimal grossWeight,
        Map<String, String> metadata,
        BigDecimal temperatureAtReceipt,
        String certificateUrl,
        String performedBy
) {
    /**
     * Factory with required fields only.
     */
    public static RegisterReceiptCommand of(
            String lotNumber, String productSku, BigDecimal quantity,
            String locationCode, String batchNumber, LocalDate productionDate,
            String origin, String performedBy
    ) {
        return new RegisterReceiptCommand(
                lotNumber, productSku, quantity, locationCode,
                batchNumber, productionDate, origin, null,
                null, null, null, null, null, null, null, performedBy
        );
    }
}