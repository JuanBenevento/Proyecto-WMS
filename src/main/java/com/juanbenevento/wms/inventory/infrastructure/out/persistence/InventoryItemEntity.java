package com.juanbenevento.wms.inventory.infrastructure.out.persistence;

import com.juanbenevento.wms.inventory.domain.model.InventoryStatus;
import com.juanbenevento.wms.shared.infrastructure.adapter.out.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

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
    @Column(name = "lpn")
    private String lpn;

    @Column(nullable = false)
    private String productSku;

    private Double quantity;

    private String batchNumber;
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    private InventoryStatus status;

    @Column(nullable = false)
    private String locationCode;

}