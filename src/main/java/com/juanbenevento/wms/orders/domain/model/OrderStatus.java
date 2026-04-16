package com.juanbenevento.wms.orders.domain.model;

/**
 * Estados del pedido según flujo estándar de WMS.
 * El pedido avanza linealmente: PENDING → ALLOCATED → PICKING → PACKED → SHIPPED → DELIVERED
 * Puede finalizar antes en CANCELLED o quedarse en HOLD.
 */
public enum OrderStatus {

    PENDING("Pendiente - Creado, sin asignar stock"),
    ALLOCATED("Asignado - Stock reservado"),
    PICKING("En picking - Items siendo pickingeados"),
    PACKED("Empacado - Listo para envío"),
    SHIPPED("Enviado - En tránsito al cliente"),
    DELIVERED("Entregado - Confirmación de entrega"),
    CANCELLED("Cancelado - Pedido cancelado"),
    HOLD("En espera - Bloqueado por alguna regla de negocio");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case PENDING -> next == ALLOCATED || next == CANCELLED || next == HOLD;
            case ALLOCATED -> next == PICKING || next == CANCELLED || next == HOLD;
            case PICKING -> next == PACKED || next == CANCELLED;
            case PACKED -> next == SHIPPED || next == CANCELLED;
            case SHIPPED -> next == DELIVERED || next == CANCELLED;
            case DELIVERED, CANCELLED, HOLD -> false; // Estados terminales o de bloqueo
        };
    }
}
