package com.juanbenevento.wms.orders.domain.model;

/**
 * Estados de cada línea dentro de un pedido.
 * Permite tracking granular del picking por item.
 */
public enum OrderLineStatus {

    PENDING("Pendiente - Sin asignación"),
    ALLOCATED("Asignado - Stock reservado para esta línea"),
    PICKED("Pickeado - Item seleccionado del stock"),
    PACKED("Empacado - Item embolsado/cajado"),
    SHIPPED("Enviado - Item en tránsito"),
    DELIVERED("Entregado - Item entregado"),
    CANCELLED("Cancelado - Línea cancelada"),
    SHORT_PICKED("Picking incompleto - No había stock suficiente");

    private final String description;

    OrderLineStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canTransitionTo(OrderLineStatus next) {
        return switch (this) {
            case PENDING -> next == ALLOCATED || next == CANCELLED;
            case ALLOCATED -> next == PICKED || next == SHORT_PICKED || next == CANCELLED;
            case PICKED -> next == PACKED || next == CANCELLED;
            case PACKED -> next == SHIPPED || next == CANCELLED;
            case SHIPPED -> next == DELIVERED || next == CANCELLED;
            case DELIVERED, CANCELLED, SHORT_PICKED -> false; // Estados terminales
        };
    }
}
