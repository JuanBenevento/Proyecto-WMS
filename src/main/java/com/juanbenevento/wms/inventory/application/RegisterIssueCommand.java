package com.juanbenevento.wms.inventory.application;

import java.math.BigDecimal;

/**
 * Command for registering an issue (picking/shipment) of goods.
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
public record RegisterIssueCommand(
        String productSku,
        BigDecimal quantity,
        String toLocation,
        String reason,
        String allocationStrategy,
        java.util.List<String> preferredLots,
        String performedBy
) {
    public static RegisterIssueCommand of(
            String productSku, BigDecimal quantity,
            String toLocation, String reason, String performedBy
    ) {
        return new RegisterIssueCommand(
                productSku, quantity, toLocation, reason, "FEFO", null, performedBy
        );
    }
}