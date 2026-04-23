package com.juanbenevento.wms.inventory.domain.model;

import com.juanbenevento.wms.inventory.domain.event.LotReceivedEvent;
import com.juanbenevento.wms.inventory.domain.event.LotStatusChangedEvent;
import com.juanbenevento.wms.inventory.domain.exception.ExpiredLotException;
import com.juanbenevento.wms.inventory.domain.exception.LotQuarantineException;
import lombok.Getter;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Aggregate Root representing a Lot (batch) of inventory items.
 * Manages the full lifecycle of a lot from receipt to exhaustion.
 *
 * <p>A Lot groups inventory items by batch origin, tracking production date,
 * temperature requirements, weights, and status transitions.</p>
 *
 * <p>Key invariants:</p>
 * <ul>
 *   <li>Only ACTIVE lots can be issued</li>
 *   <li>QUARANTINE lots require explicit release</li>
 *   <li>EXPIRED lots cannot be moved</li>
 * </ul>
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
@Getter
public class Lot {

    // ============== IDENTITY ==============

    /**
     * Unique lot identifier (assigned by system on creation).
     */
    private final String lotNumber;

    /**
     * Product SKU this lot belongs to.
     */
    private final String productSku;

    // ============== BATCH INFO ==============

    /**
     * Supplier batch reference (external identifier).
     */
    private final String batchNumber;

    /**
     * Manufacturing/production date.
     */
    private final LocalDate productionDate;

    /**
     * Supplier/farm origin identifier.
     */
    private final String origin;

    // ============== STATUS ==============

    /**
     * Current lifecycle status of the lot.
     */
    private LotStatus status;

    /**
     * Expiry date (optional, for perishable goods).
     */
    private final LocalDate expiryDate;

    // ============== TEMPERATURE ==============

    /**
     * Required storage temperature range (nullable, for cold chain).
     */
    private final TemperatureRange temperatureRange;

    // ============== WEIGHTS ==============

    /**
     * Net weight of the batch (without packaging).
     */
    private final BigDecimal netWeight;

    /**
     * Gross weight of the batch (with packaging).
     */
    private final BigDecimal grossWeight;

    // ============== METADATA ==============

    /**
     * Flexible key-value storage for industry-specific attributes.
     */
    private final Map<String, String> metadata;

    // ============== AUDIT ==============

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String createdBy;

    // ============== CONSTRUCTOR ==============

    private Lot(String lotNumber, String productSku, String batchNumber,
            LocalDate productionDate, String origin, LotStatus status, LocalDate expiryDate,
            TemperatureRange temperatureRange, BigDecimal netWeight, BigDecimal grossWeight,
            Map<String, String> metadata, LocalDateTime createdAt, LocalDateTime updatedAt, String createdBy) {
        this.lotNumber = lotNumber;
        this.productSku = productSku;
        this.batchNumber = batchNumber;
        this.productionDate = productionDate;
        this.origin = origin;
        this.status = status;
        this.expiryDate = expiryDate;
        this.temperatureRange = temperatureRange;
        this.netWeight = netWeight;
        this.grossWeight = grossWeight;
        this.metadata = metadata;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
    }

    // ============== FACTORY METHODS ==============

    /**
     * Factory method to create a new Lot on receipt.
     *
     * @param lotNumber unique identifier (generated or provided)
     * @param productSku product this lot belongs to
     * @param batchNumber supplier batch reference
     * @param productionDate manufacturing date
     * @param origin supplier/farm identifier
     * @return a new Lot with status ACTIVE
     * @throws IllegalArgumentException if required fields are null
     */
    public static Lot create(String lotNumber, String productSku, String batchNumber,
            LocalDate productionDate, String origin) {
        return create(lotNumber, productSku, batchNumber, productionDate, origin, null, null, null, null, Map.of());
    }

    /**
     * Factory method with full options for Lot creation.
     */
    public static Lot create(String lotNumber, String productSku, String batchNumber,
            LocalDate productionDate, String origin, LocalDate expiryDate,
            TemperatureRange temperatureRange, BigDecimal netWeight, BigDecimal grossWeight,
            Map<String, String> metadata) {
        if (lotNumber == null || lotNumber.isBlank()) {
            throw new IllegalArgumentException("Lot number is required");
        }
        if (productSku == null || productSku.isBlank()) {
            throw new IllegalArgumentException("Product SKU is required");
        }

        LocalDateTime now = LocalDateTime.now();
        return new Lot(
                lotNumber,
                productSku,
                batchNumber,
                productionDate,
                origin,
                LotStatus.ACTIVE,
                expiryDate,
                temperatureRange,
                netWeight,
                grossWeight,
                metadata,
                now,
                now,
                "SYSTEM"
        );
    }

    // ============== STATUS TRANSITIONS ==============

    /**
     * Validates if this lot can be issued (has available quantity).
     *
     * @return true if status is ACTIVE
     * @throws LotQuarantineException if status is QUARANTINE
     * @throws ExpiredLotException if status is EXPIRED
     */
    public boolean canIssue() {
        if (status == LotStatus.QUARANTINE) {
            throw new LotQuarantineException(lotNumber);
        }
        if (status == LotStatus.EXPIRED) {
            throw new ExpiredLotException(lotNumber, expiryDate);
        }
        return status == LotStatus.ACTIVE;
    }

    /**
     * Marks this lot as quarantined (held for quality inspection).
     *
     * @return a new Lot with status QUARANTINE
     */
    public Lot markAsQuarantined() {
        if (status != LotStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE lots can be quarantined");
        }
        return changeStatus(LotStatus.QUARANTINE);
    }

    /**
     * Releases a quarantined lot back to active status.
     *
     * @return a new Lot with status ACTIVE
     */
    public Lot releaseFromQuarantine() {
        if (status != LotStatus.QUARANTINE) {
            throw new IllegalStateException("Only QUARANTINE lots can be released");
        }
        return changeStatus(LotStatus.ACTIVE);
    }

    /**
     * Marks this lot as expired.
     *
     * @return a new Lot with status EXPIRED
     */
    public Lot markAsExpired() {
        if (status == LotStatus.EXPIRED) {
            return this; // Already expired, no change
        }
        return changeStatus(LotStatus.EXPIRED);
    }

    /**
     * Marks this lot as exhausted (quantity depleted).
     *
     * @return a new Lot with status EXHAUSTED
     */
    public Lot markAsExhausted() {
        if (status == LotStatus.EXHAUSTED) {
            return this; // Already exhausted, no change
        }
        return changeStatus(LotStatus.EXHAUSTED);
    }

    // ============== HELPER METHODS ==============

    /**
     * Checks if this lot is expired based on expiry date.
     *
     * @return true if expiry date has passed
     */
    public boolean isExpired() {
        if (expiryDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(expiryDate);
    }

    /**
     * Checks if a given temperature is within the lot's required range.
     *
     * @param temperature the temperature to check
     * @return true if within range or no range defined
     */
    public boolean isTemperatureWithinRange(BigDecimal temperature) {
        if (temperatureRange == null) {
            return true; // No requirement
        }
        return temperatureRange.isWithinRange(temperature);
    }

    /**
     * Returns the production date in days ago.
     *
     * @return number of days since production
     */
    public long getDaysSinceProduction() {
        if (productionDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(productionDate, LocalDate.now());
    }

    /**
     * Returns days until expiry (negative if already expired).
     *
     * @return days until expiry, or 0 if no expiry date
     */
    public long getDaysUntilExpiry() {
        if (expiryDate == null) {
            return Long.MAX_VALUE; // No expiry
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    // ============== PRIVATE HELPERS ==============

    private Lot changeStatus(LotStatus newStatus) {
        LotStatusChangedEvent event = new LotStatusChangedEvent(
                this.lotNumber,
                this.status,
                newStatus,
                LocalDateTime.now()
        );

        // Emit event via Spring ApplicationEventPublisher
        // This is handled by the domain event dispatcher in application layer

        return new Lot(
                this.lotNumber,
                this.productSku,
                this.batchNumber,
                this.productionDate,
                this.origin,
                newStatus,
                this.expiryDate,
                this.temperatureRange,
                this.netWeight,
                this.grossWeight,
                this.metadata,
                this.createdAt,
                LocalDateTime.now(),
                this.createdBy
        );
    }

    /**
     * Creates the domain event for lot receipt.
     *
     * @param receivedBy username of the receiver
     * @return LotReceivedEvent
     */
    public LotReceivedEvent toReceivedEvent(String receivedBy) {
        return new LotReceivedEvent(
                this.lotNumber,
                this.productSku,
                this.batchNumber,
                LocalDateTime.now(),
                receivedBy
        );
    }
}