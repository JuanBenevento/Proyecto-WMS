package com.juanbenevento.wms.orders.infrastructure.out.persistence;

import com.juanbenevento.wms.orders.domain.model.OrderLineStatus;
import com.juanbenevento.wms.shared.infrastructure.adapter.out.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA entity representing an order line in the database.
 */
@Entity
@Table(name = "order_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderLineEntity extends AuditableEntity {

    @Id
    @Column(name = "line_id", length = 50)
    private String lineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "product_sku", nullable = false)
    private String productSku;

    @Column(name = "requested_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal requestedQuantity;

    @Column(name = "allocated_quantity", precision = 19, scale = 4)
    private BigDecimal allocatedQuantity;

    @Column(name = "picked_quantity", precision = 19, scale = 4)
    private BigDecimal pickedQuantity;

    @Column(name = "shipped_quantity", precision = 19, scale = 4)
    private BigDecimal shippedQuantity;

    @Column(name = "delivered_quantity", precision = 19, scale = 4)
    private BigDecimal deliveredQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderLineStatus status;

    @Column(name = "inventory_item_id", length = 50)
    private String inventoryItemId;

    @Column(name = "location_code", length = 50)
    private String locationCode;

    @Column(name = "promised_delivery_date")
    private LocalDate promisedDeliveryDate;

    @Column(length = 500)
    private String notes;
}
