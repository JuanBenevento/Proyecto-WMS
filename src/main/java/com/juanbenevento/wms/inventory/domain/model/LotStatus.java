package com.juanbenevento.wms.inventory.domain.model;

/**
 * Enumeration representing the lifecycle status of inventory lots.
 * Controls which lots can be issued/shipped based on their state.
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
public enum LotStatus {
    /**
     * Active lot - available for picking and shipping.
     */
    ACTIVE,

    /**
     * Exhausted lot - all quantity has been consumed/shipped.
     */
    EXHAUSTED,

    /**
     * Expired lot - past expiration date, cannot be issued.
     */
    EXPIRED,

    /**
     * Quarantined lot - under quality review, cannot be issued.
     */
    QUARANTINE;

    /**
     * Determines if a lot with this status can be issued/shipped.
     * Only ACTIVE lots are eligible for picking.
     *
     * @return true if the lot can be issued, false otherwise
     */
    public boolean canIssue() {
        return this == ACTIVE;
    }
}