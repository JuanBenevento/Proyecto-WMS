package com.juanbenevento.wms.orders.application.port.in.dto;

import java.util.List;

/**
 * Response DTO paginado para lista de pedidos.
 */
public record OrderListResponse(
        List<OrderResponse> orders,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public static OrderListResponse of(List<OrderResponse> orders, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new OrderListResponse(
                orders,
                page,
                size,
                totalElements,
                totalPages,
                page < totalPages - 1,
                page > 0
        );
    }
}
