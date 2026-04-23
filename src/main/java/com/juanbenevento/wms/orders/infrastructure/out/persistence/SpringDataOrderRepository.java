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

    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.lines WHERE o.orderId = :orderId")
    Optional<OrderEntity> findByIdWithLines(@Param("orderId") String orderId);

    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.lines WHERE o.warehouseId = :warehouseId AND o.status = :status")
    List<OrderEntity> findByWarehouseIdAndStatusWithLines(@Param("warehouseId") String warehouseId, @Param("status") OrderStatus status);

    // ===== Phase 3.3: N+1 Query Optimization =====
    // Added JOIN FETCH to avoid N+1 queries when loading order lines

    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.lines WHERE o.customerId = :customerId")
    List<OrderEntity> findByCustomerIdWithLines(@Param("customerId") String customerId);

    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.lines WHERE o.warehouseId = :warehouseId")
    List<OrderEntity> findByWarehouseIdWithLines(@Param("warehouseId") String warehouseId);

    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.lines WHERE o.status = :status")
    List<OrderEntity> findByStatusWithLines(@Param("status") OrderStatus status);

    @Query("SELECT DISTINCT o FROM OrderEntity o LEFT JOIN FETCH o.lines")
    List<OrderEntity> findAllWithLines();
}
