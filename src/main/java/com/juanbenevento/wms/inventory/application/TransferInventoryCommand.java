package com.juanbenevento.wms.inventory.application;

import java.math.BigDecimal;

/**
 * Command for transferring inventory between locations.
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
public record TransferInventoryCommand(
        String lotNumber,
        BigDecimal quantity,
        String fromLocation,
        String toLocation,
        String reason,
        String performedBy
) {
    public static TransferInventoryCommand of(
            String lotNumber, BigDecimal quantity,
            String fromLocation, String toLocation, String performedBy
    ) {
        return new TransferInventoryCommand(lotNumber, quantity, fromLocation, toLocation, "Transfer", performedBy);
    }
}