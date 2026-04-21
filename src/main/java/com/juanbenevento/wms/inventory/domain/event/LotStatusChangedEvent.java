package com.juanbenevento.wms.inventory.domain.event;

import com.juanbenevento.wms.inventory.domain.model.LotStatus;

import java.time.LocalDateTime;

/**
 * Domain event emitted when a lot's status changes during its lifecycle.
 * Used for audit trails, notifications, and triggering subsequent processes.
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
public record LotStatusChangedEvent(
        String lotNumber,
        LotStatus oldStatus,
        LotStatus newStatus,
        LocalDateTime changedAt
) {}