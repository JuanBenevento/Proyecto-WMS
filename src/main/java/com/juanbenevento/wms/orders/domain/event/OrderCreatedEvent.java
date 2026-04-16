package com.juanbenevento.wms.orders.domain.event;

import com.juanbenevento.wms.orders.domain.model.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Evento publicado cuando se crea una nueva orden.
 * InventoryService se suscribe a este evento para considerar la orden
 * en su proceso de asignación de stock.
 */
public class OrderCreatedEvent extends DomainEvent {
    
    private final String orderNumber;
    private final String customerId;
    private final String customerName;
    private final String priority;
    private final List<OrderLineInfo> lines;
    
    public record OrderLineInfo(
        String lineId,
        String productSku,
        BigDecimal requestedQuantity
    ) {}
    
    public OrderCreatedEvent(Order order, Map<String, Object> metadata) {
        super(order.getOrderId(), "Order", metadata);
        this.orderNumber = order.getOrderNumber();
        this.customerId = order.getCustomerId();
        this.customerName = order.getCustomerName();
        this.priority = order.getPriority();
        this.lines = order.getLines().stream()
            .map(line -> new OrderLineInfo(
                line.getLineId(),
                line.getProductSku(),
                line.getRequestedQuantity()
            ))
            .toList();
    }
    
    // --- Getters ---
    
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public String getPriority() {
        return priority;
    }
    
    public List<OrderLineInfo> getLines() {
        return lines;
    }
    
    public int getLineCount() {
        return lines.size();
    }
    
    public BigDecimal getTotalQuantity() {
        return lines.stream()
            .map(OrderLineInfo::requestedQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
