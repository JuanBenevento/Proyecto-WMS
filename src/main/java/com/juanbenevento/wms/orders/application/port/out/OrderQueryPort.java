package com.juanbenevento.wms.orders.application.port.out;

import com.juanbenevento.wms.orders.domain.model.OrderStatus;

import java.util.List;

/**
 * Puerto de salida para que Inventory consulte información de Orders.
 * 
 * Implementado en el módulo Orders para permitir que Inventory
 * detecte órdenes pendientes y les asigne stock.
 */
public interface OrderQueryPort {

    /**
     * Obtiene los IDs de órdenes en estado PENDING.
     */
    List<String> findPendingOrderIds();

    /**
     * Obtiene la información resumida de una orden para asignación de stock.
     */
    PendingOrderInfo getPendingOrderInfo(String orderId);

    /**
     * Información resumida de una orden para el servicio de Inventory.
     */
    record PendingOrderInfo(
        String orderId,
        String orderNumber,
        String priority,
        List<OrderLineInfo> lines
    ) {
        public record OrderLineInfo(
            String lineId,
            String sku,
            java.math.BigDecimal requestedQuantity
        ) {}
    }
}
