package com.juanbenevento.wms.orders.domain.model;

import java.util.Set;

/**
 * Estados del pedido según flujo de WMS Inventory Leads.
 * 
 * Flujo principal:
 * CREATED → CONFIRMED → PENDING → (HOLD) → ALLOCATED → PICKING → PACKED → SHIPPED → DELIVERED
 *                          ↓
 *                       CANCELLED
 * 
 * Cada estado tiene transiciones válidas definidas.
 */
public enum OrderStatus {

    /** Orden creada pero aún no validada/procesada */
    CREATED("Creado", "Orden creada, pendiente de validación"),

    /** Orden validada, en cola de procesamiento */
    CONFIRMED("Confirmado", "Orden validada, en cola de asignación"),

    /** Esperando asignación de stock de Inventory */
    PENDING("Pendiente", "Esperando asignación de stock"),

    /** Orden bloqueada por alguna razón (ver StatusReason) */
    HOLD("En Espera", "Bloqueado por alguna regla de negocio"),

    /** Stock asignado, lista para picking */
    ALLOCATED("Asignado", "Stock reservado, lista para picking"),

    /** Picking en progreso */
    PICKING("En Picking", "Items siendo colectados del almacén"),

    /** Verificada y empacada */
    PACKED("Empacado", "Verificado y listo para envío"),

    /** Enviado al cliente */
    SHIPPED("Enviado", "En tránsito al cliente"),

    /** Entregado exitosamente */
    DELIVERED("Entregado", "Entregado al cliente"),

    /** Orden cancelada */
    CANCELLED("Cancelado", "Orden cancelada");

    private final String shortName;
    private final String description;

    OrderStatus(String shortName, String description) {
        this.shortName = shortName;
        this.description = description;
    }

    public String getShortName() {
        return shortName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Verifica si se puede transicionar a un nuevo estado.
     */
    public boolean canTransitionTo(OrderStatus next) {
        return getValidTransitions().contains(next);
    }

    /**
     * Obtiene los estados válidos de transición.
     */
    public Set<OrderStatus> getValidTransitions() {
        return switch (this) {
            case CREATED -> Set.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> Set.of(PENDING, HOLD, CANCELLED);
            case PENDING -> Set.of(ALLOCATED, HOLD, CANCELLED);
            case HOLD -> Set.of(PENDING, ALLOCATED, CANCELLED);
            case ALLOCATED -> Set.of(PICKING, CANCELLED);
            case PICKING -> Set.of(PACKED, CANCELLED);
            case PACKED -> Set.of(SHIPPED, CANCELLED);
            case SHIPPED -> Set.of(DELIVERED, CANCELLED);
            case DELIVERED, CANCELLED -> Set.of(); // Estados terminales
        };
    }

    /**
     * Verifica si es estado terminal (no hay más transiciones).
     */
    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED;
    }

    /**
     * Verifica si es estado activo (puede continuar procesándose).
     */
    public boolean isActive() {
        return !isTerminal() && this != HOLD;
    }

    /**
     * Verifica si requiere intervención manual.
     */
    public boolean requiresIntervention() {
        return this == HOLD;
    }
}
