package com.juanbenevento.wms.inventory.domain.exception;

import com.juanbenevento.wms.shared.domain.exception.DomainException;

import java.math.BigDecimal;

/**
 * Exception thrown when insufficient stock is available to fulfill an allocation request.
 * Contains details about the requested quantity vs available quantity.
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
public class InsufficientStockException extends DomainException {

    private final String productSku;
    private final BigDecimal requestedQuantity;
    private final BigDecimal availableQuantity;

    /**
     * Constructs an InsufficientStockException with allocation details.
     *
     * @param productSku the product SKU with insufficient stock
     * @param requestedQuantity the quantity requested
     * @param availableQuantity the quantity actually available
     */
    public InsufficientStockException(String productSku,
                                   BigDecimal requestedQuantity,
                                   BigDecimal availableQuantity) {
        super(String.format("Insufficient stock for product '%s': requested %s, available %s",
                productSku, requestedQuantity, availableQuantity));
        this.productSku = productSku;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    /**
     * @return the product SKU with insufficient stock
     */
    public String getProductSku() {
        return productSku;
    }

    /**
     * @return the quantity requested
     */
    public BigDecimal getRequestedQuantity() {
        return requestedQuantity;
    }

    /**
     * @return the quantity actually available
     */
    public BigDecimal getAvailableQuantity() {
        return availableQuantity;
    }
}