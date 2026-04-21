package com.juanbenevento.wms.inventory.domain.model;

/**
 * Enumeration of inventory movement types in the warehouse system.
 * Represents the different ways stock can enter, leave, or move within the system.
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
public enum MovementType {
    /**
     * Receipt of goods from suppliers or customers (returns).
     */
    RECEIPT,

    /**
     * Issue/shipment of goods to customers or internal consumption.
     */
    ISSUE,

    /**
     * Transfer of goods between locations within the warehouse.
     */
    TRANSFER,

    /**
     * Inventory adjustment (cycle count corrections, damage write-offs).
     */
    ADJUSTMENT,

    /**
     * Return of goods from customers (defects, overstock).
     */
    RETURN
}