package com.juanbenevento.wms.inventory.domain.model;

public enum InventoryStatus {
    IN_QUALITY_CHECK, // Recién llegado, aún no se puede vender
    AVAILABLE,        // Listo para venta
    RESERVED,         // Asignado a un pedido (nadie más lo puede tocar)
    DAMAGED,          // Roto / No apto
    EXPIRED,
    SHIPPED// Vencido
}