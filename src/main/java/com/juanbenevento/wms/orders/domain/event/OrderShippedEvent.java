package com.juanbenevento.wms.orders.domain.event;

import com.juanbenevento.wms.orders.domain.model.Order;

import java.util.Map;

/**
 * Evento publicado cuando una orden es enviada.
 * Útil para tracking, notificaciones al cliente, integración con carriers.
 */
public class OrderShippedEvent extends DomainEvent {
    
    private final String orderNumber;
    private final String carrierId;
    private final String carrierName;
    private final String trackingNumber;
    private final String warehouseId;
    
    public OrderShippedEvent(Order order, String carrierId, String carrierName,
                            String trackingNumber, Map<String, Object> metadata) {
        super(order.getOrderId(), "Order", metadata);
        this.orderNumber = order.getOrderNumber();
        this.carrierId = carrierId;
        this.carrierName = carrierName;
        this.trackingNumber = trackingNumber;
        this.warehouseId = order.getWarehouseId();
    }
    
    // --- Getters ---
    
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public String getCarrierId() {
        return carrierId;
    }
    
    public String getCarrierName() {
        return carrierName;
    }
    
    public String getTrackingNumber() {
        return trackingNumber;
    }
    
    public String getWarehouseId() {
        return warehouseId;
    }
}
