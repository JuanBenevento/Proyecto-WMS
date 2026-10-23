package com.juanbenevento.wms.inventory.infrastructure.out.persistence;

import com.juanbenevento.wms.inventory.domain.model.InventoryMovement;
import com.juanbenevento.wms.inventory.domain.model.MovementType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for the append-only InventoryMovement audit log
 * (feature real-persistence / B1). Mapping only — the domain value object
 * {@link InventoryMovement} stays framework-free.
 */
@Entity
@Table(name = "inventory_movements")
@Getter
@Setter
public class InventoryMovementEntity {

    @Id
    @Column(name = "movement_id")
    private UUID movementId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private MovementType type;

    @Column(name = "lot_number", length = 50, nullable = false)
    private String lotNumber;

    @Column(name = "quantity", precision = 19, scale = 4, nullable = false)
    private BigDecimal quantity;

    @Column(name = "from_location")
    private String fromLocation;

    @Column(name = "to_location")
    private String toLocation;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "performed_by")
    private String performedBy;

    @Column(name = "temperature_at_movement", precision = 5, scale = 2)
    private BigDecimal temperatureAtMovement;

    @Column(name = "weight_at_movement", precision = 12, scale = 2)
    private BigDecimal weightAtMovement;

    @Column(name = "certificate_url", length = 500)
    private String certificateUrl;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    public static InventoryMovementEntity from(InventoryMovement movement) {
        InventoryMovementEntity entity = new InventoryMovementEntity();
        entity.setMovementId(movement.getMovementId());
        entity.setType(movement.getType());
        entity.setLotNumber(movement.getLotNumber());
        entity.setQuantity(movement.getQuantity());
        entity.setFromLocation(movement.getFromLocation());
        entity.setToLocation(movement.getToLocation());
        entity.setReason(movement.getReason());
        entity.setPerformedBy(movement.getPerformedBy());
        entity.setTemperatureAtMovement(movement.getTemperatureAtMovement());
        entity.setWeightAtMovement(movement.getWeightAtMovement());
        entity.setCertificateUrl(movement.getCertificateUrl());
        entity.setTimestamp(movement.getTimestamp());
        return entity;
    }

    public InventoryMovement toDomain() {
        return new InventoryMovement(
                movementId, type, lotNumber, quantity, fromLocation, toLocation, reason,
                performedBy, temperatureAtMovement, weightAtMovement, certificateUrl, timestamp);
    }
}
