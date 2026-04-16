package com.juanbenevento.wms.orders.infrastructure.out.persistence;

import com.juanbenevento.wms.orders.domain.model.OrderStatus;
import com.juanbenevento.wms.shared.infrastructure.adapter.out.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing an order in the database.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderEntity extends AuditableEntity {

    @Id
    @Column(name = "order_id", length = 50)
    private String orderId;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "shipping_address", nullable = false, length = 500)
    private String shippingAddress;

    @Column(length = 20)
    private String priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "promised_ship_date")
    private LocalDate promisedShipDate;

    @Column(name = "promised_delivery_date")
    private LocalDate promisedDeliveryDate;

    @Column(name = "warehouse_id", nullable = false)
    private String warehouseId;

    @Column(name = "carrier_id")
    private String carrierId;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(length = 1000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "cancelled_by")
    private String cancelledBy;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderLineEntity> lines = new ArrayList<>();

    /**
     * Helper method to add a line to this order.
     */
    public void addLine(OrderLineEntity line) {
        lines.add(line);
        line.setOrder(this);
    }

    /**
     * Helper method to remove a line from this order.
     */
    public void removeLine(OrderLineEntity line) {
        lines.remove(line);
        line.setOrder(null);
    }
}
