package com.juanbenevento.wms.audit.infrastructure.out.persistence;

import com.juanbenevento.wms.inventory.domain.model.StockMovementType;
import com.juanbenevento.wms.shared.infrastructure.adapter.out.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movement_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class StockMovementLogEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockMovementType type;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private String lpn;

    @Column(nullable = false)
    private BigDecimal quantity;

    private BigDecimal oldQuantity;

    private BigDecimal newQuantity;

    @Column(name = "user_name")
    private String user;

    private String reason;
}

