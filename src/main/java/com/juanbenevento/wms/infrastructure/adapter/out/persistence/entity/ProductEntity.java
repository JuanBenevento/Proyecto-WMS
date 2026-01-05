package com.juanbenevento.wms.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE products SET active = false WHERE id = ?")
@SQLRestriction("active = true")
public class ProductEntity extends AuditableEntity {

    @Id
    private UUID id;

    private String sku;
    private String name;
    private String description;

    private Double width;
    private Double height;
    private Double depth;
    private Double weight;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;
}