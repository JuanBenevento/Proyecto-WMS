package com.juanbenevento.wms.orders.infrastructure.out.persistence;

import com.juanbenevento.wms.orders.application.port.out.OrderQueryPort;
import com.juanbenevento.wms.orders.domain.model.Order;
import com.juanbenevento.wms.orders.domain.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Implementación del puerto OrderQueryPort.
 * 
 * Permite que Inventory consulte órdenes pendientes para asignarles stock.
 */
@Repository
@RequiredArgsConstructor
public class OrderQueryAdapter implements OrderQueryPort {

    private final SpringDataOrderRepository orderRepository;

    @Override
    public List<String> findPendingOrderIds() {
        return orderRepository.findByStatus(OrderStatus.PENDING)
                .stream()
                .map(Order::getOrderId)
                .toList();
    }

    @Override
    public PendingOrderInfo getPendingOrderInfo(String orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        
        if (orderOpt.isEmpty()) {
            return null;
        }
        
        Order order = orderOpt.get();
        
        // Solo retornar si está en estado PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            return null;
        }
        
        List<PendingOrderInfo.OrderLineInfo> lines = order.getLines().stream()
                .filter(line -> line.getStatus() == com.juanbenevento.wms.orders.domain.model.OrderLineStatus.PENDING)
                .map(line -> new PendingOrderInfo.OrderLineInfo(
                        line.getLineId(),
                        line.getProductSku(),
                        line.getRequestedQuantity()
                ))
                .toList();
        
        return new PendingOrderInfo(
                order.getOrderId(),
                order.getOrderNumber(),
                order.getPriority(),
                lines
        );
    }
}
