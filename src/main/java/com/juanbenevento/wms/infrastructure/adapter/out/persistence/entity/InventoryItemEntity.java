package com.juanbenevento.wms.infrastructure.adapter.out.persistence.entity;

import com.juanbenevento.wms.domain.model.InventoryStatus;
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