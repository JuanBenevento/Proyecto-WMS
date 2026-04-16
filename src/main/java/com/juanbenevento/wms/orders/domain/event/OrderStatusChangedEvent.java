package com.juanbenevento.wms.orders.domain.event;

import com.juanbenevento.wms.orders.domain.model.Order;
import com.juanbenevento.wms.orders.domain.model.OrderStatus;
import com.juanbenevento.wms.orders.domain.model.StatusReason;

import java.util.Map;

/**
 * Evento publicado cuando el estado de una orden cambia.
 * Útil para auditoría, notificaciones, y sincronización entre módulos.
 */
public class OrderStatusChangedEvent extends DomainEvent {
    
    private final String orderNumber;
    private final OrderStatus previousStatus;
    private final StatusReason previousReason;
    private final OrderStatus newStatus;
    private final StatusReason newReason;
    private final String changedBy;
    private final String reason;
    
    public OrderStatusChangedEvent(
            Order order,
            OrderStatus previousStatus,
            StatusReason previousReason,
            StatusReason newReason,
            String changedBy,
            Map<String, Object> metadata) {
        super(order.getOrderId(), "Order", metadata);
        this.orderNumber = order.getOrderNumber();
        this.previousStatus = previousStatus;
        this.previousReason = previousReason;
        this.newStatus = order.getStatus();
        this.newReason = newReason;
        this.changedBy = changedBy;
        this.reason = newReason.getDescription();
    }
    
    // --- Getters ---
    
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public OrderStatus getPreviousStatus() {
        return previousStatus;
    }
    
    public StatusReason getPreviousReason() {
        return previousReason;
    }
    
    public OrderStatus getNewStatus() {
        return newStatus;
    }
    
    public StatusReason getNewReason() {
        return newReason;
    }
    
    public String getChangedBy() {
        return changedBy;
    }
    
    public String getReason() {
        return reason;
    }
    
    public boolean isTransitionToTerminal() {
        return newStatus.isTerminal();
    }
    
    public boolean isTransitionToHold() {
        return newStatus == OrderStatus.HOLD;
    }
}
