package com.juanbenevento.wms.orders.application.port.in.dto;

import com.juanbenevento.wms.orders.domain.model.OrderStatus;

/**
 * Query para filtrar y paginar lista de pedidos.
 */
public record OrderListQuery(
        String customerId,
        String warehouseId,
        OrderStatus status,
        String priority,
        Integer page,
        Integer size
) {
    public OrderListQuery {
        if (page == null || page < 0) page = 0;
        if (size == null || size < 1) size = 20;
        if (size > 100) size = 100;  // Máximo 100 por página
    }

    public static OrderListQuery of(String customerId, String warehouseId,
                                   OrderStatus status, String priority,
                                   Integer page, Integer size) {
        return new OrderListQuery(customerId, warehouseId, status, priority, page, size);
    }

    public static OrderListQuery empty() {
        return new OrderListQuery(null, null, null, null, 0, 20);
    }
}
