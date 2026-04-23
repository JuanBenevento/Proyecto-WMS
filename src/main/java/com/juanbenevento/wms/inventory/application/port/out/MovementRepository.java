package com.juanbenevento.wms.inventory.application.port.out;

import com.juanbenevento.wms.inventory.domain.model.InventoryMovement;
import com.juanbenevento.wms.inventory.domain.model.MovementType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (repository interface) for InventoryMovement persistence.
 * Movements are append-only (immutable audit log).
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
public interface MovementRepository {

    /**
     * Saves a movement record.
     * Movements are never updated or deleted.
     *
     * @param movement the movement to save
     * @return the saved movement
     */
    InventoryMovement save(InventoryMovement movement);

    /**
     * Finds a movement by ID.
     *
     * @param movementId the movement UUID
     * @return the movement if found
     */
    Optional<InventoryMovement> findById(UUID movementId);

    /**
     * Finds all movements for a lot.
     *
     * @param lotNumber the lot number
     * @return list of movements ordered by timestamp
     */
    List<InventoryMovement> findByLotNumber(String lotNumber);

    /**
     * Finds movements by type.
     *
     * @param type the movement type
     * @return list of movements
     */
    List<InventoryMovement> findByType(MovementType type);

    /**
     * Finds movements within a time range.
     *
     * @param from start timestamp
     * @param to end timestamp
     * @return list of movements
     */
    List<InventoryMovement> findByTimestampBetween(LocalDateTime from, LocalDateTime to);

    /**
     * Counts movements by lot.
     *
     * @param lotNumber the lot number
     * @return count
     */
    long countByLotNumber(String lotNumber);
}