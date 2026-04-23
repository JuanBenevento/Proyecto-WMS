package com.juanbenevento.wms.inventory.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response for receipt registration.
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
public record ReceiptResponse(
        String lotNumber,
        String productSku,
        BigDecimal quantity,
        String locationCode,
        LocalDateTime receivedAt,
        String receivedBy
) {
}