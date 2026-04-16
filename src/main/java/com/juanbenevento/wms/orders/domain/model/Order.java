package com.juanbenevento.wms.orders.domain.model;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Order aggregate root - representa un pedido de un cliente.
 * Contiene las líneas del pedido y su estado general.
 */
@Getter
public class Order {

    private final String orderId;
    private final String orderNumber;      // Human-readable order number (e.g., "ORD-2024-00001")
    private final String customerId;
    private final String customerName;
    private final String customerEmail;
    private final String shippingAddress;
    private final String priority;          // HIGH, MEDIUM, LOW
    private OrderStatus status;
    private final LocalDate promisedShipDate;
    private final LocalDate promisedDeliveryDate;
    private final String warehouseId;
    private String carrierId;
    private String trackingNumber;
    private String notes;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String cancelledBy;
    private String cancellationReason;
    private Long version;

    // Líneas del pedido (pertenecen a esta orden)
    private final List<OrderLine> lines;

    private Order(String orderId, String orderNumber, String customerId, String customerName,
                  String customerEmail, String shippingAddress, String priority,
                  OrderStatus status, LocalDate promisedShipDate, LocalDate promisedDeliveryDate,
                  String warehouseId, String carrierId, String trackingNumber, String notes,
                  LocalDateTime createdAt, LocalDateTime updatedAt, String cancelledBy,
                  String cancellationReason, Long version, List<OrderLine> lines) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.shippingAddress = shippingAddress;
        this.priority = priority;
        this.status = status;
        this.promisedShipDate = promisedShipDate;
        this.promisedDeliveryDate = promisedDeliveryDate;
        this.warehouseId = warehouseId;
        this.carrierId = carrierId;
        this.trackingNumber = trackingNumber;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.cancelledBy = cancelledBy;
        this.cancellationReason = cancellationReason;
        this.version = version;
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }

    // --- FACTORY METHODS ---

    /**
     * Creates a new Order from the customer-facing API.
     */
    public static Order create(String customerId, String customerName, String customerEmail,
                                String shippingAddress, String priority,
                                LocalDate promisedShipDate, LocalDate promisedDeliveryDate,
                                String warehouseId, String notes) {
        validateCustomerId(customerId);
        validateShippingAddress(shippingAddress);

        String orderId = UUID.randomUUID().toString();
        String orderNumber = generateOrderNumber();
        LocalDateTime now = LocalDateTime.now();

        return new Order(
                orderId,
                orderNumber,
                customerId,
                customerName,
                customerEmail,
                shippingAddress,
                priority != null ? priority : "MEDIUM",
                OrderStatus.PENDING,
                promisedShipDate,
                promisedDeliveryDate,
                warehouseId,
                null,   // carrierId
                null,   // trackingNumber
                notes,
                now,
                now,
                null,   // cancelledBy
                null,   // cancellationReason
                null,   // version
                new ArrayList<>()
        );
    }

    /**
     * Reconstructs Order from repository.
     */
    public static Order fromRepository(String orderId, String orderNumber, String customerId,
                                        String customerName, String customerEmail,
                                        String shippingAddress, String priority, OrderStatus status,
                                        LocalDate promisedShipDate, LocalDate promisedDeliveryDate,
                                        String warehouseId, String carrierId, String trackingNumber,
                                        String notes, LocalDateTime createdAt, LocalDateTime updatedAt,
                                        String cancelledBy, String cancellationReason,
                                        Long version, List<OrderLine> lines) {
        return new Order(orderId, orderNumber, customerId, customerName, customerEmail,
                shippingAddress, priority, status, promisedShipDate, promisedDeliveryDate,
                warehouseId, carrierId, trackingNumber, notes, createdAt, updatedAt,
                cancelledBy, cancellationReason, version,
                lines != null ? new ArrayList<>(lines) : new ArrayList<>());
    }

    // --- VALIDATION ---

    private static void validateCustomerId(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("El ID del cliente es obligatorio");
        }
    }

    private static void validateShippingAddress(String shippingAddress) {
        if (shippingAddress == null || shippingAddress.isBlank()) {
            throw new IllegalArgumentException("La dirección de envío es obligatoria");
        }
    }

    private static String generateOrderNumber() {
        int year = LocalDate.now().getYear();
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("ORD-%d-%s", year, uuid);
    }

    // --- ORDER LINES MANAGEMENT ---

    /**
     * Adds a line to this order.
     */
    public void addLine(OrderLine line) {
        if (line == null) {
            throw new IllegalArgumentException("La línea no puede ser nula");
        }
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden agregar líneas a pedidos en estado PENDING");
        }
        this.lines.add(line);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Removes a line from this order.
     */
    public void removeLine(String lineId) {
        if (lineId == null || lineId.isBlank()) {
            throw new IllegalArgumentException("El ID de línea es obligatorio");
        }
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden eliminar líneas de pedidos en estado PENDING");
        }
        this.lines.removeIf(line -> line.getLineId().equals(lineId));
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Returns an unmodifiable view of the lines.
     */
    public List<OrderLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    /**
     * Returns the number of lines in this order.
     */
    public int getLineCount() {
        return lines.size();
    }

    // --- ORDER STATUS TRANSITIONS ---

    /**
     * Allocates stock to all lines (prepares the order for picking).
     */
    public void allocate() {
        if (!status.canTransitionTo(OrderStatus.ALLOCATED)) {
            throw new IllegalStateException("No se puede asignar stock cuando el estado es " + status);
        }
        if (lines.isEmpty()) {
            throw new IllegalStateException("No se pueden asignar pedidos sin líneas");
        }

        this.status = OrderStatus.ALLOCATED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Starts the picking process.
     */
    public void startPicking() {
        if (!status.canTransitionTo(OrderStatus.PICKING)) {
            throw new IllegalStateException("No se puede iniciar picking cuando el estado es " + status);
        }
        if (lines.isEmpty()) {
            throw new IllegalStateException("No se pueden pickear pedidos sin líneas");
        }

        this.status = OrderStatus.PICKING;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Packs the order (all lines packed).
     */
    public void pack() {
        if (!status.canTransitionTo(OrderStatus.PACKED)) {
            throw new IllegalStateException("No se puede empacar cuando el estado es " + status);
        }
        if (lines.isEmpty()) {
            throw new IllegalStateException("No se pueden empacar pedidos sin líneas");
        }

        // Verificar que todas las líneas estén en estado PACKED o anterior (si short picked)
        for (OrderLine line : lines) {
            if (line.getStatus() != OrderLineStatus.PACKED &&
                line.getStatus() != OrderLineStatus.SHORT_PICKED) {
                throw new IllegalStateException("No se puede empacar la orden: línea " + line.getLineId() +
                        " está en estado " + line.getStatus());
            }
        }

        this.status = OrderStatus.PACKED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Ships the order with carrier and tracking info.
     */
    public void ship(String carrierId, String trackingNumber) {
        if (!status.canTransitionTo(OrderStatus.SHIPPED)) {
            throw new IllegalStateException("No se puede enviar cuando el estado es " + status);
        }
        if (carrierId == null || carrierId.isBlank()) {
            throw new IllegalArgumentException("El ID del transportista es obligatorio");
        }

        this.carrierId = carrierId;
        this.trackingNumber = trackingNumber;
        this.status = OrderStatus.SHIPPED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marks the order as delivered.
     */
    public void deliver() {
        if (!status.canTransitionTo(OrderStatus.DELIVERED)) {
            throw new IllegalStateException("No se puede marcar como entregado cuando el estado es " + status);
        }

        this.status = OrderStatus.DELIVERED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Cancels the order.
     */
    public void cancel(String cancelledBy, String reason) {
        if (!status.canTransitionTo(OrderStatus.CANCELLED)) {
            throw new IllegalStateException("No se puede cancelar cuando el estado es " + status);
        }

        this.cancelledBy = cancelledBy;
        this.cancellationReason = reason;
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();

        // Cancelar todas las líneas
        for (OrderLine line : lines) {
            try {
                line.cancel();
            } catch (IllegalStateException e) {
                // Si no se puede cancelar una línea, continuar con las demás
            }
        }
    }

    /**
     * Puts the order on hold.
     */
    public void hold() {
        if (!status.canTransitionTo(OrderStatus.HOLD)) {
            throw new IllegalStateException("No se puede poner en espera cuando el estado es " + status);
        }

        this.status = OrderStatus.HOLD;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Releases the order from hold back to PENDING.
     */
    public void releaseFromHold() {
        if (status != OrderStatus.HOLD) {
            throw new IllegalStateException("Solo se pueden liberar pedidos en estado HOLD");
        }

        this.status = OrderStatus.PENDING;
        this.updatedAt = LocalDateTime.now();
    }

    // --- CALCULATIONS ---

    /**
     * Returns the total quantity requested across all lines.
     */
    public BigDecimal getTotalRequestedQuantity() {
        return lines.stream()
                .map(OrderLine::getRequestedQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Returns the total quantity allocated across all lines.
     */
    public BigDecimal getTotalAllocatedQuantity() {
        return lines.stream()
                .map(OrderLine::getAllocatedQuantity)
                .filter(q -> q != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Returns the total quantity picked across all lines.
     */
    public BigDecimal getTotalPickedQuantity() {
        return lines.stream()
                .map(OrderLine::getPickedQuantity)
                .filter(q -> q != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Checks if all lines are fulfilled.
     */
    public boolean isFullyFulfilled() {
        if (lines.isEmpty()) return false;
        return lines.stream().allMatch(OrderLine::isFulfilled);
    }

    /**
     * Checks if there's any shortage in the order.
     */
    public boolean hasShortage() {
        return lines.stream().anyMatch(OrderLine::hasShortage);
    }

    /**
     * Returns the count of lines with shortages.
     */
    public int getShortedLineCount() {
        return (int) lines.stream().filter(OrderLine::hasShortage).count();
    }

    // --- EQUALS & HASHCODE (based on orderId only) ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }
}
