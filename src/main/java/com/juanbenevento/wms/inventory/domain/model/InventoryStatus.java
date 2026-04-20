package com.juanbenevento.wms.inventory.domain.model;

public enum InventoryStatus {
    IN_QUALITY_CHECK, // Recién llegado, aún no se puede vender
    AVAILABLE,        // Listo para venta
    RESERVED,         // Asignado a un pedido (nadie más lo puede tocar)
    PICKING,          // En proceso de picking
    PACKED,           // Pickeado y empacado
    DAMAGED,          // Roto / No apto
    EXPIRED,          // Vencido
    SHIPPED           // Despachado
}