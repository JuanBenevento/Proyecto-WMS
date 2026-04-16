package com.juanbenevento.wms.inventory.application.port.out;

import java.util.List;

/**
 * Puerto para consultar información de órdenes durante el picking.
 */
public interface PickingOrderPort {
    
    /**
     * Obtiene las líneas de una orden asignadas a un almacén.
     */
    List<OrderLineForPicking> getOrderLinesForPicking(String orderId);
    
    /**
     * Obtiene información de una orden para picking.
     */
    PickingOrderInfo getPickingOrderInfo(String orderId);
    
    record OrderLineForPicking(
        String lineId,
        String sku,
        java.math.BigDecimal allocatedQuantity,
        String inventoryItemId,
        String locationCode
    ) {}
    
    record PickingOrderInfo(
        String orderId,
        String orderNumber,
        String warehouseId,
        String priority
    ) {}
}
