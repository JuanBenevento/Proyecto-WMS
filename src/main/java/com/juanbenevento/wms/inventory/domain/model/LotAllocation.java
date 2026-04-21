package com.juanbenevento.wms.inventory.domain.model;

import java.math.BigDecimal;

/**
 * Immutable Value Object representing a lot allocation decision.
 * Records which lot was allocated, the quantity, priority, and reason.
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
public record LotAllocation(
        String lotNumber,
        BigDecimal quantity,
        int priority,
        String reason
) {

    /**
     * Compact record constructor with auto-calculation of priority.
     *
     * @param lotNumber the lot identifier
     * @param quantity the allocated quantity
     * @param reason the allocation reason
     */
    public LotAllocation {
        if (lotNumber == null || lotNumber.isBlank()) {
            throw new IllegalArgumentException("Lot number cannot be null or blank");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reason cannot be null or blank");
        }
        // Validate lot number format (alphanumeric with hyphens)
        if (!lotNumber.matches("^[A-Za-z0-9\\-]+$")) {
            throw new IllegalArgumentException("Lot number contains invalid characters");
        }
        priority = 0; // Default priority
    }

    /**
     * Factory method to create a LotAllocation with default priority.
     *
     * @param lotNumber the lot identifier
     * @param quantity the allocated quantity
     * @param reason the allocation reason
     * @return a new LotAllocation instance
     */
    public static LotAllocation of(String lotNumber, BigDecimal quantity, String reason) {
        return new LotAllocation(lotNumber, quantity, 0, reason);
    }

    /**
     * Factory method to create a LotAllocation with explicit priority.
     *
     * @param lotNumber the lot identifier
     * @param quantity the allocated quantity
     * @param priority allocation priority (lower = higher priority)
     * @param reason the allocation reason
     * @return a new LotAllocation instance
     */
    public static LotAllocation of(String lotNumber, BigDecimal quantity, int priority, String reason) {
        return new LotAllocation(lotNumber, quantity, priority, reason);
    }
}