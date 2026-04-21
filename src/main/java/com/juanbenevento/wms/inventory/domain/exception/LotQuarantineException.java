package com.juanbenevento.wms.inventory.domain.exception;

import com.juanbenevento.wms.shared.domain.exception.DomainException;

/**
 * Exception thrown when attempting to allocate stock from a quarantined lot.
 * Indicates the lot is under quality review and cannot be used until cleared.
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
public class LotQuarantineException extends DomainException {

    private final String lotNumber;

    /**
     * Constructs a LotQuarantineException with lot details.
     *
     * @param lotNumber the quarantined lot identifier
     */
    public LotQuarantineException(String lotNumber) {
        super(String.format("Lot '%s' is under quarantine and cannot be allocated until quality review is complete", lotNumber));
        this.lotNumber = lotNumber;
    }

    /**
     * @return the quarantined lot identifier
     */
    public String getLotNumber() {
        return lotNumber;
    }
}