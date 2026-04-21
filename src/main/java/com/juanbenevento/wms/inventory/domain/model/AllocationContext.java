package com.juanbenevento.wms.inventory.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Value Object representing the context for stock allocation requests.
 * Encapsulates quantity requirements and location preferences/filters.
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
@Getter
@EqualsAndHashCode
@Builder
public final class AllocationContext {

    private final BigDecimal requestedQuantity;
    private final String preferredLocation;
    private final List<String> excludeLots;
    private final List<String> includeLots;

    private AllocationContext(BigDecimal requestedQuantity,
                              String preferredLocation,
                              List<String> excludeLots,
                              List<String> includeLots) {
        this.requestedQuantity = requestedQuantity;
        this.preferredLocation = preferredLocation;
        this.excludeLots = excludeLots != null ? List.copyOf(excludeLots) : Collections.emptyList();
        this.includeLots = includeLots != null ? List.copyOf(includeLots) : Collections.emptyList();
    }

    /**
     * Factory method to create an AllocationContext with only the required quantity.
     *
     * @param quantity the quantity to allocate
     * @return a new AllocationContext with only quantity set
     * @throws IllegalArgumentException if quantity is null or non-positive
     */
    public static AllocationContext of(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Requested quantity must be positive");
        }
        return new AllocationContext(quantity, null, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Builder factory for optional fields.
     * Ensures immutability by copying lists.
     *
     * @return a new builder instance
     */
    public static AllocationContextBuilder builder() {
        return new AllocationContextBuilder();
    }

    /**
     * Checks if a specific lot should be excluded from allocation.
     *
     * @param lotNumber the lot number to check
     * @return true if lot is in the exclude list, false otherwise
     */
    public boolean shouldExcludeLot(String lotNumber) {
        return excludeLots.contains(lotNumber);
    }

    /**
     * Checks if a specific lot is explicitly included (filter mode).
     * When includeLots is non-empty, only those lots are eligible.
     *
     * @param lotNumber the lot number to check
     * @return true if lot is in the include list, or if include list is empty (no filter)
     */
    public boolean shouldIncludeLot(String lotNumber) {
        if (includeLots.isEmpty()) {
            return true; // No filter, all lots are eligible
        }
        return includeLots.contains(lotNumber);
    }
}