package com.juanbenevento.wms.inventory.domain.exception;

import com.juanbenevento.wms.shared.domain.exception.DomainException;

import java.time.LocalDate;

/**
 * Exception thrown when attempting to allocate stock from an expired lot.
 * Indicates the lot has passed its expiration date and cannot be used.
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
public class ExpiredLotException extends DomainException {

    private final String lotNumber;
    private final LocalDate expiryDate;

    /**
     * Constructs an ExpiredLotException with lot details.
     *
     * @param lotNumber the expired lot identifier
     * @param expiryDate the expiration date of the lot
     */
    public ExpiredLotException(String lotNumber, LocalDate expiryDate) {
        super(String.format("Lot '%s' expired on %s and cannot be allocated", lotNumber, expiryDate));
        this.lotNumber = lotNumber;
        this.expiryDate = expiryDate;
    }

    /**
     * @return the expired lot identifier
     */
    public String getLotNumber() {
        return lotNumber;
    }

    /**
     * @return the expiration date of the lot
     */
    public LocalDate getExpiryDate() {
        return expiryDate;
    }
}