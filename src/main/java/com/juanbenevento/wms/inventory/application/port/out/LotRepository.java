package com.juanbenevento.wms.inventory.application.port.out;

import com.juanbenevento.wms.inventory.domain.model.Lot;
import com.juanbenevento.wms.inventory.domain.model.LotStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Port (repository interface) for Lot persistence.
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
public interface LotRepository {

    /**
     * Saves a lot to the repository.
     *
     * @param lot the lot to save
     * @return the saved lot
     */
    Lot save(Lot lot);

    /**
     * Finds a lot by its unique identifier.
     *
     * @param lotNumber the lot number
     * @return the lot if found
     */
    Optional<Lot> findByLotNumber(String lotNumber);

    /**
     * Finds all lots for a product SKU.
     *
     * @param productSku the product SKU
     * @return list of lots
     */
    List<Lot> findByProductSku(String productSku);

    /**
     * Finds lots by status.
     *
     * @param status the status to filter
     * @return list of lots
     */
    List<Lot> findByStatus(LotStatus status);

    /**
     * Finds lots expiring within N days.
     *
     * @param days days threshold
     * @return list of expiring lots
     */
    List<Lot> findExpiringWithin(int days);

    /**
     * Finds active lots for allocation (ordered by strategy).
     *
     * @param productSku the product
     * @return list of available lots
     */
    List<Lot> findActiveLotsForAllocation(String productSku);

    /**
     * Counts lots by status.
     *
     * @param status the status
     * @return count
     */
    long countByStatus(LotStatus status);
}