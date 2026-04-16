package com.juanbenevento.wms.orders.domain.event;

import com.juanbenevento.wms.orders.domain.model.Order;
import com.juanbenevento.wms.orders.domain.model.StatusReason;

import java.util.List;
import java.util.Map;

/**
 * Evento publicado cuando una orden es cancelada.
 * InventoryService se suscribe para liberar el stock reservado.
 */
public class OrderCancelledEvent extends DomainEvent {
    
    private final String orderNumber;
    private final StatusReason cancellationReason;
    private final String cancelledBy;
    private final List<CancelledLineInfo> lines;
    
    public record CancelledLineInfo(
        String lineId,
        String productSku,
        String inventoryItemId  // LPN que estaba reservado
    ) {}
    
    public OrderCancelledEvent(Order order, StatusReason reason, String cancelledBy, 
                               Map<String, Object> metadata) {
        super(order.getOrderId(), "Order", metadata);
        this.orderNumber = order.getOrderNumber();
        this.cancellationReason = reason;
        this.cancelledBy = cancelledBy;
        this.lines = order.getLines().stream()
            .filter(line -> line.getInventoryItemId() != null)
            .map(line -> new CancelledLineInfo(
                line.getLineId(),
                line.getProductSku(),
                line.getInventoryItemId()
            ))
            .toList();
    }
    
    // --- Getters ---
    
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public StatusReason getCancellationReason() {
        return cancellationReason;
    }
    
    public String getCancelledBy() {
        return cancelledBy;
    }
    
    public List<CancelledLineInfo> getLines() {
        return lines;
    }
    
    public boolean hasReservedStock() {
        return !lines.isEmpty();
    }
}
