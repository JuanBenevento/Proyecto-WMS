package com.juanbenevento.wms.inventory.domain.model;

import com.juanbenevento.wms.inventory.domain.model.MovementType;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable entity representing a single inventory movement operation.
 * Tracks the transfer of stock quantities with full audit trail information.
 *
 * <p>All fields are final to enforce immutability. New instances with
 * additional data (like temperature readings) are created via factory
 * methods that return modified copies.</p>
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
@Value
public class InventoryMovement {

    /**
     * Unique identifier for this movement record.
     */
    private final UUID movementId;

    /**
     * Type of movement (RECEIPT, ISSUE, TRANSFER, ADJUSTMENT, RETURN).
     */
    private final MovementType type;

    /**
     * Lot number this movement is associated with.
     */
    private final String lotNumber;

    /**
     * Quantity moved in the operation.
     */
    private final BigDecimal quantity;

    /**
     * Source location (null for RECEIPT movements).
     */
    private final String fromLocation;

    /**
     * Destination location (required for all movements).
     */
    private final String toLocation;

    /**
     * Reason or reference for the movement.
     */
    private final String reason;

    /**
     * Username of the operator who performed this movement.
     */
    private final String performedBy;

    /**
     * Temperature recorded at the time of movement (nullable).
     * Relevant for cold chain compliance tracking.
     */
    private final BigDecimal temperatureAtMovement;

    /**
     * Weight recorded at the time of movement (nullable).
     */
    private final BigDecimal weightAtMovement;

    /**
     * URL to quality certificate or inspection report (nullable).
     */
    private final String certificateUrl;

    /**
     * Timestamp when the movement was recorded.
     */
    private final LocalDateTime timestamp;

    // ============== FACTORY METHODS ==============

    /**
     * Factory method for recording a receipt of goods.
     *
     * @param lotNumber the received lot identifier
     * @param quantity the quantity received
     * @param toLocation the storage location where goods are placed
     * @param performedBy the operator username
     * @return a new InventoryMovement instance for a receipt
     */
    public static InventoryMovement receive(String lotNumber, BigDecimal quantity,
            String toLocation, String performedBy) {
        return new InventoryMovement(
                UUID.randomUUID(),
                MovementType.RECEIPT,
                lotNumber,
                quantity,
                null,
                toLocation,
                "Goods received",
                performedBy,
                null,
                null,
                null,
                LocalDateTime.now()
        );
    }

    /**
     * Factory method for recording an issue/shipment of goods.
     *
     * @param lotNumber the lot being issued
     * @param quantity the quantity issued
     * @param fromLocation the source storage location
     * @param reason the reason for issue (e.g., customer order, internal use)
     * @param performedBy the operator username
     * @return a new InventoryMovement instance for an issue
     */
    public static InventoryMovement issue(String lotNumber, BigDecimal quantity,
            String fromLocation, String reason, String performedBy) {
        return new InventoryMovement(
                UUID.randomUUID(),
                MovementType.ISSUE,
                lotNumber,
                quantity,
                fromLocation,
                null,
                reason,
                performedBy,
                null,
                null,
                null,
                LocalDateTime.now()
        );
    }

    /**
     * Factory method for recording a transfer between locations.
     *
     * @param lotNumber the lot being transferred
     * @param quantity the quantity transferred
     * @param fromLocation the source location
     * @param toLocation the destination location
     * @param performedBy the operator username
     * @return a new InventoryMovement instance for a transfer
     */
    public static InventoryMovement transfer(String lotNumber, BigDecimal quantity,
            String fromLocation, String toLocation, String performedBy) {
        return new InventoryMovement(
                UUID.randomUUID(),
                MovementType.TRANSFER,
                lotNumber,
                quantity,
                fromLocation,
                toLocation,
                "Location transfer",
                performedBy,
                null,
                null,
                null,
                LocalDateTime.now()
        );
    }

    /**
     * Factory method for recording an inventory adjustment.
     *
     * @param lotNumber the lot being adjusted
     * @param quantity the adjustment quantity (positive for additions, negative for write-offs)
     * @param location the location of the adjustment
     * @param reason the reason for adjustment (e.g., cycle count, damage)
     * @param performedBy the operator username
     * @return a new InventoryMovement instance for an adjustment
     */
    public static InventoryMovement adjustment(String lotNumber, BigDecimal quantity,
            String location, String reason, String performedBy) {
        return new InventoryMovement(
                UUID.randomUUID(),
                MovementType.ADJUSTMENT,
                lotNumber,
                quantity,
                location,
                location,
                reason,
                performedBy,
                null,
                null,
                null,
                LocalDateTime.now()
        );
    }

    /**
     * Factory method for recording a return of goods.
     *
     * @param lotNumber the returned lot identifier
     * @param quantity the quantity returned
     * @param toLocation the return location
     * @param reason the reason for return (e.g., defect, overstock)
     * @param performedBy the operator username
     * @return a new InventoryMovement instance for a return
     */
    public static InventoryMovement returnMovement(String lotNumber, BigDecimal quantity,
            String toLocation, String reason, String performedBy) {
        return new InventoryMovement(
                UUID.randomUUID(),
                MovementType.RETURN,
                lotNumber,
                quantity,
                null,
                toLocation,
                reason,
                performedBy,
                null,
                null,
                null,
                LocalDateTime.now()
        );
    }

    // ============== MUTATOR METHODS (Return new instances) ==============

    /**
     * Creates a new instance with temperature data recorded.
     *
     * @param temperature the temperature at the time of movement
     * @return a new InventoryMovement with temperature set
     */
    public InventoryMovement withTemperature(BigDecimal temperature) {
        return new InventoryMovement(
                this.movementId,
                this.type,
                this.lotNumber,
                this.quantity,
                this.fromLocation,
                this.toLocation,
                this.reason,
                this.performedBy,
                temperature,
                this.weightAtMovement,
                this.certificateUrl,
                this.timestamp
        );
    }

    /**
     * Creates a new instance with weight data recorded.
     *
     * @param weight the weight at the time of movement
     * @return a new InventoryMovement with weight set
     */
    public InventoryMovement withWeight(BigDecimal weight) {
        return new InventoryMovement(
                this.movementId,
                this.type,
                this.lotNumber,
                this.quantity,
                this.fromLocation,
                this.toLocation,
                this.reason,
                this.performedBy,
                this.temperatureAtMovement,
                weight,
                this.certificateUrl,
                this.timestamp
        );
    }

    /**
     * Creates a new instance with a certificate URL attached.
     *
     * @param certificateUrl URL to the inspection/certificate document
     * @return a new InventoryMovement with certificate URL set
     */
    public InventoryMovement withCertificate(String certificateUrl) {
        return new InventoryMovement(
                this.movementId,
                this.type,
                this.lotNumber,
                this.quantity,
                this.fromLocation,
                this.toLocation,
                this.reason,
                this.performedBy,
                this.temperatureAtMovement,
                this.weightAtMovement,
                certificateUrl,
                this.timestamp
        );
    }
}