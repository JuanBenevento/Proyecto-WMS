package com.juanbenevento.wms.orders.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a line item within an Order.
 * Contains the product reference, quantity requested, and fulfillment status.
 */
@Getter
public class OrderLine {

    private final String lineId;
    private final String productSku;
    private BigDecimal requestedQuantity;
    private BigDecimal allocatedQuantity;
    private BigDecimal pickedQuantity;
    private BigDecimal shippedQuantity;
    private BigDecimal deliveredQuantity;
    private OrderLineStatus status;
    private String inventoryItemId; // LPN que se le asignó
    private String locationCode;   // Ubicación del pick
    private LocalDate promisedDeliveryDate;
    private String notes;
    private Long version;

    private OrderLine(String lineId, String productSku, BigDecimal requestedQuantity,
                      BigDecimal allocatedQuantity, BigDecimal pickedQuantity,
                      BigDecimal shippedQuantity, BigDecimal deliveredQuantity,
                      OrderLineStatus status, String inventoryItemId, String locationCode,
                      LocalDate promisedDeliveryDate, String notes, Long version) {
        this.lineId = lineId;
        this.productSku = productSku;
        this.requestedQuantity = requestedQuantity;
        this.allocatedQuantity = allocatedQuantity;
        this.pickedQuantity = pickedQuantity;
        this.shippedQuantity = shippedQuantity;
        this.deliveredQuantity = deliveredQuantity;
        this.status = status;
        this.inventoryItemId = inventoryItemId;
        this.locationCode = locationCode;
        this.promisedDeliveryDate = promisedDeliveryDate;
        this.notes = notes;
        this.version = version;
    }

    // --- FACTORY METHODS ---

    /**
     * Creates a new OrderLine when placing an order.
     */
    public static OrderLine create(String lineId, String productSku, BigDecimal requestedQuantity,
                                    LocalDate promisedDeliveryDate, String notes) {
        validateLineId(lineId);
        validateProductSku(productSku);
        validateQuantity(requestedQuantity);

        return new OrderLine(
                lineId,
                productSku,
                requestedQuantity,
                BigDecimal.ZERO,      // allocatedQuantity
                BigDecimal.ZERO,      // pickedQuantity
                BigDecimal.ZERO,      // shippedQuantity
                BigDecimal.ZERO,      // deliveredQuantity
                OrderLineStatus.PENDING,
                null,                  // inventoryItemId
                null,                  // locationCode
                promisedDeliveryDate,
                notes,
                null                   // version (for new entities)
        );
    }

    /**
     * Reconstructs OrderLine from repository (without domain objects).
     */
    public static OrderLine fromRepository(String lineId, String productSku,
                                           BigDecimal requestedQuantity, BigDecimal allocatedQuantity,
                                           BigDecimal pickedQuantity, BigDecimal shippedQuantity,
                                           BigDecimal deliveredQuantity, OrderLineStatus status,
                                           String inventoryItemId, String locationCode,
                                           LocalDate promisedDeliveryDate, String notes,
                                           Long version) {
        return new OrderLine(lineId, productSku, requestedQuantity, allocatedQuantity,
                pickedQuantity, shippedQuantity, deliveredQuantity, status,
                inventoryItemId, locationCode, promisedDeliveryDate, notes, version);
    }

    // --- VALIDATION ---

    private static void validateLineId(String lineId) {
        if (lineId == null || lineId.isBlank()) {
            throw new IllegalArgumentException("El ID de línea es obligatorio");
        }
    }

    private static void validateProductSku(String productSku) {
        if (productSku == null || productSku.isBlank()) {
            throw new IllegalArgumentException("El SKU del producto es obligatorio");
        }
    }

    private static void validateQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad solicitada debe ser positiva");
        }
    }

    // --- BUSINESS LOGIC ---

    /**
     * Allocates stock to this line (called by allocation service).
     */
    public void allocate(BigDecimal quantity, String inventoryItemId, String locationCode) {
        if (!status.canTransitionTo(OrderLineStatus.ALLOCATED)) {
            throw new IllegalStateException("No se puede asignar stock cuando el estado es " + status);
        }
        // Allow zero for shortage scenarios (no stock available)
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad a asignar no puede ser negativa");
        }
        if (quantity.compareTo(requestedQuantity) > 0) {
            throw new IllegalArgumentException("La cantidad asignada no puede exceder la solicitada");
        }

        this.allocatedQuantity = quantity;
        this.inventoryItemId = inventoryItemId;
        this.locationCode = locationCode;
        this.status = OrderLineStatus.ALLOCATED;
    }

    /**
     * Marks this line as picked (called by picking service).
     */
    public void pick(BigDecimal quantityPicked) {
        if (!status.canTransitionTo(OrderLineStatus.PICKED) &&
            !status.canTransitionTo(OrderLineStatus.SHORT_PICKED)) {
            throw new IllegalStateException("No se puede marcar como pickeado cuando el estado es " + status);
        }
        if (quantityPicked == null || quantityPicked.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad pickeada no puede ser negativa");
        }

        this.pickedQuantity = quantityPicked;

        if (quantityPicked.compareTo(allocatedQuantity) < 0) {
            this.status = OrderLineStatus.SHORT_PICKED;
        } else {
            this.status = OrderLineStatus.PICKED;
        }
    }

    /**
     * Marks this line as packed.
     */
    public void pack() {
        if (!status.canTransitionTo(OrderLineStatus.PACKED)) {
            throw new IllegalStateException("No se puede empacar cuando el estado es " + status);
        }
        this.status = OrderLineStatus.PACKED;
    }

    /**
     * Marks this line as shipped.
     */
    public void ship(BigDecimal quantityShipped) {
        if (!status.canTransitionTo(OrderLineStatus.SHIPPED)) {
            throw new IllegalStateException("No se puede marcar como enviado cuando el estado es " + status);
        }
        if (quantityShipped == null || quantityShipped.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad enviada no puede ser negativa");
        }

        this.shippedQuantity = quantityShipped;
        this.status = OrderLineStatus.SHIPPED;
    }

    /**
     * Marks this line as delivered.
     */
    public void deliver(BigDecimal quantityDelivered) {
        if (!status.canTransitionTo(OrderLineStatus.DELIVERED)) {
            throw new IllegalStateException("No se puede marcar como entregado cuando el estado es " + status);
        }
        if (quantityDelivered == null || quantityDelivered.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad entregada no puede ser negativa");
        }

        this.deliveredQuantity = quantityDelivered;
        this.status = OrderLineStatus.DELIVERED;
    }

    /**
     * Cancels this line.
     */
    public void cancel() {
        if (!status.canTransitionTo(OrderLineStatus.CANCELLED)) {
            throw new IllegalStateException("No se puede cancelar cuando el estado es " + status);
        }
        this.status = OrderLineStatus.CANCELLED;
    }

    /**
     * Updates the promised delivery date.
     */
    public void updatePromisedDeliveryDate(LocalDate newDate) {
        this.promisedDeliveryDate = newDate;
    }

    /**
     * Adds notes to this line.
     */
    public void addNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Checks if the line is fully fulfilled.
     */
    public boolean isFulfilled() {
        return status == OrderLineStatus.DELIVERED &&
               deliveredQuantity != null &&
               deliveredQuantity.compareTo(requestedQuantity) >= 0;
    }

    /**
     * Checks if there's a shortage on this line.
     */
    public boolean hasShortage() {
        return status == OrderLineStatus.SHORT_PICKED ||
               (allocatedQuantity != null && allocatedQuantity.compareTo(requestedQuantity) < 0);
    }

    // --- EQUALS & HASHCODE (based on lineId only) ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderLine orderLine = (OrderLine) o;
        return Objects.equals(lineId, orderLine.lineId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineId);
    }
}
