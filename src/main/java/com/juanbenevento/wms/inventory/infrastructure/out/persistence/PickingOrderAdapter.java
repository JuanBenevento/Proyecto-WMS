package com.juanbenevento.wms.inventory.infrastructure.out.persistence;

import com.juanbenevento.wms.inventory.application.port.out.PickingOrderPort;
import com.juanbenevento.wms.inventory.application.port.out.PickingOrderPort.OrderLineForPicking;
import com.juanbenevento.wms.inventory.application.port.out.PickingOrderPort.PickingOrderInfo;
import com.juanbenevento.wms.orders.domain.model.OrderLineStatus;
import com.juanbenevento.wms.orders.infrastructure.out.persistence.OrderEntity;
import com.juanbenevento.wms.orders.infrastructure.out.persistence.OrderLineEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;

/**
 * Adapter que proporciona información de órdenes para el proceso de picking.
 * 
 * Este adapter consulta el módulo de Orders desde Inventory, siguiendo
 * el patrón de arquitectura hexagonal donde los puertos de salida se implementan
 * en la capa de infraestructura.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PickingOrderAdapter implements PickingOrderPort {

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * Obtiene las líneas de una orden que están asignadas a picking.
     * Filtra solo líneas con estado ALLOCATED.
     */
    @Override
    public List<OrderLineForPicking> getOrderLinesForPicking(String orderId) {
        try {
            TypedQuery<OrderLineEntity> query = entityManager.createQuery(
                "SELECT ol FROM OrderLineEntity ol WHERE ol.order.orderId = :orderId " +
                "AND ol.status = :status",
                OrderLineEntity.class
            );
            query.setParameter("orderId", orderId);
            query.setParameter("status", OrderLineStatus.ALLOCATED);
            
            return query.getResultList().stream()
                .map(line -> new OrderLineForPicking(
                    line.getLineId(),
                    line.getProductSku(),
                    line.getAllocatedQuantity(),
                    line.getInventoryItemId(),
                    line.getLocationCode()
                ))
                .toList();
                
        } catch (NoResultException e) {
            log.debug("No se encontraron líneas para picking de la orden: {}", orderId);
            return Collections.emptyList();
        }
    }

    /**
     * Obtiene información de una orden para el proceso de picking.
     */
    @Override
    public PickingOrderInfo getPickingOrderInfo(String orderId) {
        try {
            OrderEntity order = entityManager.find(OrderEntity.class, orderId);
            if (order == null) {
                return null;
            }
            
            return new PickingOrderInfo(
                order.getOrderId(),
                order.getOrderNumber(),
                null, // warehouseId - no existe en la entity
                order.getPriority() != null ? order.getPriority() : "NORMAL"
            );
            
        } catch (NoResultException e) {
            log.debug("Orden no encontrada: {}", orderId);
            return null;
        }
    }
}