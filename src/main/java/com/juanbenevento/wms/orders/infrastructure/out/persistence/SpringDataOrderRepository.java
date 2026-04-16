package com.juanbenevento.wms.orders.infrastructure.out.persistence;

import com.juanbenevento.wms.orders.domain.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para OrderEntity.
 */
@Repository
public interface SpringDataOrderRepository extends JpaRepository<OrderEntity, String> {

    Optional<OrderEntity> findByOrderNumber(String orderNumber);

    List<OrderEntity> findByCustomerId(String customerId);

    List<OrderEntity> findByWarehouseId(String warehouseId);

    List<OrderEntity> findByStatus(OrderStatus status);

    List<OrderEntity> findByWarehouseIdAndStatus(String warehouseId, OrderStatus status);

    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.lines WHERE o.orderId = :orderId")
    Optional<OrderEntity> findByIdWithLines(@Param("orderId") String orderId);

    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.lines WHERE o.warehouseId = :warehouseId AND o.status = :status")
    List<OrderEntity> findByWarehouseIdAndStatusWithLines(@Param("warehouseId") String warehouseId, @Param("status") OrderStatus status);
}
