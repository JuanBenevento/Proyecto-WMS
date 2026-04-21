package com.juanbenevento.wms.inventory.domain.event;

import java.time.LocalDateTime;

/**
 * Domain event emitted when a new lot is received into the warehouse.
 * Captures the essential details of the receipt for auditing and tracking.
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
public record LotReceivedEvent(
        String lotNumber,
        String productSku,
        String batchNumber,
        LocalDateTime receivedAt,
        String receivedBy
) {}