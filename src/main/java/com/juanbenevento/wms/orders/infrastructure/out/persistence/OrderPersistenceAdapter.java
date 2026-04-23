package com.juanbenevento.wms.orders.infrastructure.out.persistence;

import com.juanbenevento.wms.orders.application.mapper.OrderMapper;
import com.juanbenevento.wms.orders.application.port.out.OrderRepositoryPort;
import com.juanbenevento.wms.orders.domain.model.Order;
import com.juanbenevento.wms.orders.domain.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter que implementa OrderRepositoryPort usando JPA/Spring Data.
 */
@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderRepositoryPort {

    private final SpringDataOrderRepository jpaRepository;
    private final OrderMapper mapper;

    @Override
    public Order save(Order order) {
        OrderEntity entity = mapper.toOrderEntityWithLines(order);
        OrderEntity saved = jpaRepository.save(entity);
        return mapper.toOrderDomain(saved);
    }

    @Override
    public Optional<Order> findById(String orderId) {
        return jpaRepository.findByIdWithLines(orderId)
                .map(mapper::toOrderDomain);
    }

    @Override
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return jpaRepository.findByOrderNumber(orderNumber)
                .map(mapper::toOrderDomain);
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        return jpaRepository.findByCustomerIdWithLines(customerId)
                .stream()
                .map(mapper::toOrderDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByWarehouseId(String warehouseId) {
        return jpaRepository.findByWarehouseIdWithLines(warehouseId)
                .stream()
                .map(mapper::toOrderDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return jpaRepository.findByStatusWithLines(status)
                .stream()
                .map(mapper::toOrderDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByWarehouseIdAndStatus(String warehouseId, OrderStatus status) {
        return jpaRepository.findByWarehouseIdAndStatusWithLines(warehouseId, status)
                .stream()
                .map(mapper::toOrderDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findAll() {
        return jpaRepository.findAllWithLines()
                .stream()
                .map(mapper::toOrderDomain)
                .collect(Collectors.toList());
    }
}