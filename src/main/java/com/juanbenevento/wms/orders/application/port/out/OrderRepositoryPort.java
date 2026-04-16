package com.juanbenevento.wms.orders.application.port.out;

import com.juanbenevento.wms.orders.domain.model.Order;
import com.juanbenevento.wms.orders.domain.model.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para la persistencia de Orders.
 * Define las operaciones que el dominio necesita para persistir y recuperar Orders.
 */
public interface OrderRepositoryPort {

    /**
     * Guarda o actualiza un pedido.
     */
    Order save(Order order);

    /**
     * Busca un pedido por su ID.
     */
    Optional<Order> findById(String orderId);

    /**
     * Busca un pedido por su número legible (e.g., "ORD-2024-A1B2C3D4").
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Lista todos los pedidos de un cliente.
     */
    List<Order> findByCustomerId(String customerId);

    /**
     * Lista todos los pedidos de un almacén.
     */
    List<Order> findByWarehouseId(String warehouseId);

    /**
     * Lista pedidos por estado.
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Lista pedidos por almacén y estado.
     */
    List<Order> findByWarehouseIdAndStatus(String warehouseId, OrderStatus status);

    /**
     * Lista todos los pedidos (para administración).
     */
    List<Order> findAll();
}
