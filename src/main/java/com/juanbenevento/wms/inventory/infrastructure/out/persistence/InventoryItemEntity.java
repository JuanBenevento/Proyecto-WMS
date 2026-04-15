package com.juanbenevento.wms.inventory.infrastructure.out.persistence;

import com.juanbenevento.wms.inventory.domain.model.InventoryStatus;
import com.juanbenevento.wms.shared.domain.valueobject.BatchNumber;
import com.juanbenevento.wms.shared.domain.valueobject.Lpn;
import com.juanbenevento.wms.shared.infrastructure.persistence.BatchNumberConverter;
import com.juanbenevento.wms.shared.infrastructure.persistence.LpnConverter;
import com.juanbenevento.wms.shared.infrastructure.adapter.out.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "inventory_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class InventoryItemEntity extends AuditableEntity {
    
    @Id
    @Column(name = "lpn", length = 50)
    @Convert(converter = LpnConverter.class)
    private Lpn lpn;

    @Column(nullable = false)
    private String productSku;

    private BigDecimal quantity;

    @Column(name = "batch_number", length = 50)
    @Convert(converter = BatchNumberConverter.class)
    private BatchNumber batchNumber;
    
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    private InventoryStatus status;

    @Column(nullable = false)
    private String locationCode;
}
