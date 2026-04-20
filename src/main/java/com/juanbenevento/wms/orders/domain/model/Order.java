package com.juanbenevento.wms.orders.domain.model;

import com.juanbenevento.wms.orders.domain.event.DomainEvent;

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
 * 
 * Usa el patrón Inventory Leads: Inventory detecta pedidos en estado PENDING
 * y les asigna stock automáticamente. Este módulo recibe eventos de Inventory
 * y actualiza su estado.
 * 
 * Sistema de estados:
 * - El estado actual se representa con OrderStatusInfo (status + reason + metadata)
 * - Las transiciones válidas están definidas en OrderStatus.canTransitionTo()
 * - Los estados HOLD y CANCELLED tienen razones específicas (StatusReason)
 */
public class Order {

    private final String orderId;
    private final String orderNumber;      // Human-readable (e.g., "ORD-2024-00001")
    private final String customerId;
    private final String customerName;
    private final String customerEmail;
    private final String shippingAddress;
    private final String priority;          // HIGH, MEDIUM, LOW
    private OrderStatusInfo statusInfo;     // Estado actual con razón
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
                  OrderStatusInfo statusInfo, LocalDate promisedShipDate, LocalDate promisedDeliveryDate,
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
        this.statusInfo = statusInfo;
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
     * Crea una nueva orden en estado CREATED.
     * La orden aún no está validada.
     * 
     * Para usar el patrón Inventory Leads, llamar:
     * 1. order.confirm() → CONFIRMED
     * 2. order.markAsPending() → PENDING (Inventory detectará y asignará stock)
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
                OrderStatusInfo.of(OrderStatus.CREATED),
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
     * Reconstruye una orden desde el repositorio.
     */
    public static Order fromRepository(String orderId, String orderNumber, String customerId,
                                        String customerName, String customerEmail,
                                        String shippingAddress, String priority, OrderStatus status,
                                        LocalDate promisedShipDate, LocalDate promisedDeliveryDate,
                                        String warehouseId, String carrierId, String trackingNumber,
                                        String notes, LocalDateTime createdAt, LocalDateTime updatedAt,
                                        String cancelledBy, String cancellationReason,
                                        Long version, List<OrderLine> lines) {
        OrderStatusInfo statusInfo = OrderStatusInfo.of(status);
        return new Order(orderId, orderNumber, customerId, customerName, customerEmail,
                shippingAddress, priority, statusInfo, promisedShipDate, promisedDeliveryDate,
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

    // --- GETTERS (delegados al statusInfo) ---

    public String getOrderId() { return orderId; }
    public String getOrderNumber() { return orderNumber; }
    public String getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getShippingAddress() { return shippingAddress; }
    public String getPriority() { return priority; }
    public LocalDate getPromisedShipDate() { return promisedShipDate; }
    public LocalDate getPromisedDeliveryDate() { return promisedDeliveryDate; }
    public String getWarehouseId() { return warehouseId; }
    public String getCarrierId() { return carrierId; }
    public String getTrackingNumber() { return trackingNumber; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getCancelledBy() { return cancelledBy; }
    public String getCancellationReason() { return cancellationReason; }
    public Long getVersion() { return version; }
    public List<OrderLine> getLines() { return Collections.unmodifiableList(lines); }
    public int getLineCount() { return lines.size(); }

    /**
     * Obtiene el estado actual (delegado a statusInfo).
     */
    public OrderStatus getStatus() {
        return statusInfo != null ? statusInfo.getStatus() : null;
    }

    /**
     * Obtiene la información completa del estado.
     */
    public OrderStatusInfo getStatusInfo() {
        return statusInfo;
    }

    /**
     * Obtiene la razón del estado actual.
     */
    public StatusReason getStatusReason() {
        return statusInfo != null ? statusInfo.getReason() : StatusReason.NONE;
    }

    // --- ORDER LINES MANAGEMENT ---

    public void addLine(OrderLine line) {
        if (line == null) {
            throw new IllegalArgumentException("La línea no puede ser nula");
        }
        if (!canAddLines()) {
            throw new IllegalStateException("Solo se pueden agregar líneas en estado CREATED o PENDING");
        }
        this.lines.add(line);
        this.updatedAt = LocalDateTime.now();
    }

    public void removeLine(String lineId) {
        if (lineId == null || lineId.isBlank()) {
            throw new IllegalArgumentException("El ID de línea es obligatorio");
        }
        if (!canAddLines()) {
            throw new IllegalStateException("Solo se pueden eliminar líneas en estado CREATED o PENDING");
        }
        this.lines.removeIf(line -> line.getLineId().equals(lineId));
        this.updatedAt = LocalDateTime.now();
    }

    private boolean canAddLines() {
        OrderStatus status = getStatus();
        return status == OrderStatus.CREATED || status == OrderStatus.PENDING;
    }

    // --- STATUS TRANSITIONS ---

    /**
     * Confirma la orden (CREATED → CONFIRMED).
     */
    public void confirm() {
        transitionTo(OrderStatus.CONFIRMED, StatusReason.NONE, null);
    }

    /**
     * Marca la orden como pendiente de asignación de stock (CONFIRMED/PENDING).
     */
    public void markAsPending() {
        OrderStatus current = getStatus();
        if (current == OrderStatus.CONFIRMED || current == OrderStatus.HOLD) {
            transitionTo(OrderStatus.PENDING, StatusReason.NONE, null);
        }
    }

    /**
     * Asigna stock a una línea (llamado por InventoryService).
     * No cambia el estado general de la orden.
     */
    public void assignStockToLine(String lineId, BigDecimal allocatedQuantity, 
                                  String inventoryItemId, String locationCode) {
        OrderLine line = findLine(lineId);
        line.allocate(allocatedQuantity, inventoryItemId, locationCode);
        this.updatedAt = LocalDateTime.now();
        
        // Si todas las líneas tienen stock, transicionar a ALLOCATED
        if (allLinesAllocated()) {
            transitionTo(OrderStatus.ALLOCATED, StatusReason.NONE, null);
        }
    }

    /**
     * Reporta faltante de stock para una línea (llamado por InventoryService).
     */
    public void reportShortageForLine(String lineId, BigDecimal allocatedQuantity) {
        OrderLine line = findLine(lineId);
        line.allocate(allocatedQuantity, null, null); // Stock parcial
        
        // Si no todas las líneas están full, ir a HOLD
        if (!allLinesFullyAllocated()) {
            transitionTo(OrderStatus.HOLD, StatusReason.INVENTORY_SHORTAGE, null);
        }
    }

    /**
     * Inicia el proceso de picking (ALLOCATED → PICKING).
     */
    public void startPicking() {
        transitionTo(OrderStatus.PICKING, StatusReason.NONE, null);
    }

    /**
     * Completa el picking y empaca la orden (PICKING → PACKED).
     */
    public void pack() {
        // Verificar que todas las líneas estén en estado válido
        for (OrderLine line : lines) {
            if (line.getStatus() != com.juanbenevento.wms.orders.domain.model.OrderLineStatus.PICKED &&
                line.getStatus() != com.juanbenevento.wms.orders.domain.model.OrderLineStatus.SHORT_PICKED) {
                throw new IllegalStateException("No se puede empacar: línea " + line.getLineId() +
                        " está en estado " + line.getStatus());
            }
        }
        transitionTo(OrderStatus.PACKED, StatusReason.NONE, null);
    }

    /**
     * Envía la orden (PACKED → SHIPPED).
     */
    public void ship(String carrierId, String trackingNumber) {
        if (carrierId == null || carrierId.isBlank()) {
            throw new IllegalArgumentException("El ID del transportista es obligatorio");
        }
        this.carrierId = carrierId;
        this.trackingNumber = trackingNumber;
        transitionTo(OrderStatus.SHIPPED, StatusReason.NONE, null);
    }

    /**
     * Marca como entregado (SHIPPED → DELIVERED).
     */
    public void deliver() {
        transitionTo(OrderStatus.DELIVERED, StatusReason.NONE, null);
    }

    /**
     * Cancela la orden.
     */
    public void cancel(StatusReason reason, String cancelledBy, String cancellationReason) {
        if (reason == null || !reason.isCancelledReason()) {
            reason = StatusReason.CUSTOMER_CANCELLED; // Default
        }
        this.cancelledBy = cancelledBy;
        this.cancellationReason = cancellationReason;
        transitionTo(OrderStatus.CANCELLED, reason, null);
        
        // Cancelar todas las líneas reservadas
        for (OrderLine line : lines) {
            try {
                line.cancel();
            } catch (IllegalStateException e) {
                // Si no se puede cancelar, continuar
            }
        }
    }

    /**
     * Poné la orden en espera con una razón específica.
     */
    public void hold(StatusReason reason) {
        if (reason == null || !reason.isHoldReason()) {
            throw new IllegalArgumentException("La razón debe ser una de HOLD: " + StatusReason.class.getEnumConstants());
        }
        transitionTo(OrderStatus.HOLD, reason, null);
    }

    /**
     * Libera la orden de espera (HOLD → PENDING).
     */
    public void releaseFromHold() {
        if (getStatus() != OrderStatus.HOLD) {
            throw new IllegalStateException("Solo se pueden liberar pedidos en estado HOLD");
        }
        transitionTo(OrderStatus.PENDING, StatusReason.NONE, null);
    }

    /**
     * Transición interna de estado.
     */
    private void transitionTo(OrderStatus newStatus, StatusReason reason, String changedBy) {
        OrderStatus currentStatus = getStatus();
        
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("No se puede transicionar de %s a %s", currentStatus, newStatus));
        }

        this.statusInfo = OrderStatusInfo.of(newStatus, reason).withChangedBy(changedBy);
        this.updatedAt = LocalDateTime.now();
    }

    // --- LINE HELPERS ---

    private OrderLine findLine(String lineId) {
        return lines.stream()
                .filter(line -> line.getLineId().equals(lineId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró línea con ID: " + lineId));
    }

    private boolean allLinesAllocated() {
        return !lines.isEmpty() && lines.stream()
                .allMatch(line -> line.getStatus() != com.juanbenevento.wms.orders.domain.model.OrderLineStatus.PENDING);
    }

    private boolean allLinesFullyAllocated() {
        return !lines.isEmpty() && lines.stream()
                .allMatch(line -> {
                    if (line.getStatus() == com.juanbenevento.wms.orders.domain.model.OrderLineStatus.PENDING) {
                        return false;
                    }
                    return line.getAllocatedQuantity() != null && 
                           line.getAllocatedQuantity().compareTo(line.getRequestedQuantity()) >= 0;
                });
    }

    // --- CALCULATIONS ---

    public BigDecimal getTotalRequestedQuantity() {
        return lines.stream()
                .map(OrderLine::getRequestedQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalAllocatedQuantity() {
        return lines.stream()
                .map(OrderLine::getAllocatedQuantity)
                .filter(q -> q != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalPickedQuantity() {
        return lines.stream()
                .map(OrderLine::getPickedQuantity)
                .filter(q -> q != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isFullyFulfilled() {
        if (lines.isEmpty()) return false;
        return lines.stream().allMatch(OrderLine::isFulfilled);
    }

    public boolean hasShortage() {
        return lines.stream().anyMatch(OrderLine::hasShortage);
    }

    public int getShortedLineCount() {
        return (int) lines.stream().filter(OrderLine::hasShortage).count();
    }

    // --- EQUALS & HASHCODE ---

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
