package com.juanbenevento.wms.warehouse.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_layouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseLayoutEntity {
    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String tenantId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String layoutJson;

    @Column(nullable = false)
    private Integer version;

    private LocalDateTime updateAt;
}
