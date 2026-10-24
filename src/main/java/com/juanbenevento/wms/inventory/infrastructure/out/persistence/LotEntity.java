package com.juanbenevento.wms.inventory.infrastructure.out.persistence;

import com.juanbenevento.wms.inventory.domain.model.Lot;
import com.juanbenevento.wms.inventory.domain.model.LotStatus;
import com.juanbenevento.wms.inventory.domain.model.TemperatureRange;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * JPA entity for the Lot aggregate (feature real-persistence / B1).
 * Mapping only — the domain {@link Lot} stays framework-free.
 */
@Entity
@Table(name = "inventory_lots")
@Getter
@Setter
public class LotEntity {

    @Id
    @Column(name = "lot_number", length = 50)
    private String lotNumber;

    @Column(name = "product_sku", nullable = false)
    private String productSku;

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(name = "production_date")
    private LocalDate productionDate;

    @Column(name = "origin")
    private String origin;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private LotStatus status;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "min_temperature", precision = 5, scale = 2)
    private BigDecimal minTemperature;

    @Column(name = "max_temperature", precision = 5, scale = 2)
    private BigDecimal maxTemperature;

    @Column(name = "net_weight", precision = 12, scale = 2)
    private BigDecimal netWeight;

    @Column(name = "gross_weight", precision = 12, scale = 2)
    private BigDecimal grossWeight;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private Map<String, String> metadata;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    public static LotEntity from(Lot lot) {
        LotEntity entity = new LotEntity();
        entity.setLotNumber(lot.getLotNumber());
        entity.setProductSku(lot.getProductSku());
        entity.setBatchNumber(lot.getBatchNumber());
        entity.setProductionDate(lot.getProductionDate());
        entity.setOrigin(lot.getOrigin());
        entity.setStatus(lot.getStatus());
        entity.setExpiryDate(lot.getExpiryDate());
        TemperatureRange range = lot.getTemperatureRange();
        if (range != null) {
            entity.setMinTemperature(range.getMinTemperature());
            entity.setMaxTemperature(range.getMaxTemperature());
        }
        entity.setNetWeight(lot.getNetWeight());
        entity.setGrossWeight(lot.getGrossWeight());
        entity.setMetadata(lot.getMetadata());
        entity.setCreatedAt(lot.getCreatedAt());
        entity.setUpdatedAt(lot.getUpdatedAt());
        entity.setCreatedBy(lot.getCreatedBy());
        return entity;
    }

    public Lot toDomain() {
        TemperatureRange range = (minTemperature == null && maxTemperature == null)
                ? null
                : TemperatureRange.of(minTemperature, maxTemperature);
        return Lot.reconstitute(lotNumber, productSku, batchNumber, productionDate, origin,
                status, expiryDate, range, netWeight, grossWeight, metadata,
                createdAt, updatedAt, createdBy);
    }
}
