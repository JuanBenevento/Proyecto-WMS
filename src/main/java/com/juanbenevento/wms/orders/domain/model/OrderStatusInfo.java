package com.juanbenevento.wms.orders.domain.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Value Object que representa el estado completo de una orden en un momento dado.
 * 
 * Incluye:
 * - Estado principal (PENDING, PICKING, etc.)
 * - Razón/Subtipo (INVENTORY_SHORTAGE, CUSTOMER_CANCELLED, etc.)
 * - Metadata adicional (datos específicos de la razón)
 * - Timestamps y usuario que realizó el cambio
 * 
 * Este patrón permite extensibilidad sin modificar el enum OrderStatus.
 */
public final class OrderStatusInfo {
    
    private final OrderStatus status;
    private final StatusReason reason;
    private final Map<String, Object> metadata;
    private final LocalDateTime changedAt;
    private final String changedBy;
    
    private OrderStatusInfo(OrderStatus status, StatusReason reason,
                           Map<String, Object> metadata, LocalDateTime changedAt,
                           String changedBy) {
        this.status = Objects.requireNonNull(status, "El estado no puede ser null");
        this.reason = reason != null ? reason : StatusReason.NONE;
        this.metadata = metadata != null ? Collections.unmodifiableMap(metadata) : Collections.emptyMap();
        this.changedAt = changedAt != null ? changedAt : LocalDateTime.now();
        this.changedBy = changedBy;
    }
    
    // --- FACTORY METHODS ---
    
    /**
     * Crea un estado simple sin razón especial.
     */
    public static OrderStatusInfo of(OrderStatus status) {
        return new OrderStatusInfo(status, StatusReason.NONE, null, null, null);
    }
    
    /**
     * Crea un estado con razón específica.
     */
    public static OrderStatusInfo of(OrderStatus status, StatusReason reason) {
        return new OrderStatusInfo(status, reason, null, null, null);
    }
    
    /**
     * Crea un estado completo con toda la información.
     */
    public static OrderStatusInfo of(OrderStatus status, StatusReason reason,
                                     Map<String, Object> metadata, String changedBy) {
        return new OrderStatusInfo(status, reason, metadata, LocalDateTime.now(), changedBy);
    }
    
    /**
     * Crea un estado para transición a HOLD con razón.
     */
    public static OrderStatusInfo hold(StatusReason reason) {
        if (!reason.isHoldReason() && reason != StatusReason.NONE) {
            throw new IllegalArgumentException(
                "La razón " + reason + " no es válida para un estado HOLD");
        }
        return new OrderStatusInfo(OrderStatus.HOLD, reason, null, LocalDateTime.now(), null);
    }
    
    /**
     * Crea un estado para transición a CANCELLED con razón.
     */
    public static OrderStatusInfo cancelled(StatusReason reason) {
        if (!reason.isCancelledReason() && reason != StatusReason.NONE) {
            throw new IllegalArgumentException(
                "La razón " + reason + " no es válida para un estado CANCELLED");
        }
        return new OrderStatusInfo(OrderStatus.CANCELLED, reason, null, LocalDateTime.now(), null);
    }
    
    // --- GETTERS ---
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public StatusReason getReason() {
        return reason;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public LocalDateTime getChangedAt() {
        return changedAt;
    }
    
    public String getChangedBy() {
        return changedBy;
    }
    
    /**
     * Obtiene un valor de metadata por clave.
     */
    public Object getMetadataValue(String key) {
        return metadata.get(key);
    }
    
    /**
     * Obtiene un valor de metadata como String.
     */
    public String getMetadataString(String key) {
        Object value = metadata.get(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * Verifica si hay metadata.
     */
    public boolean hasMetadata() {
        return !metadata.isEmpty();
    }
    
    // --- CONVENIENCE METHODS ---
    
    public boolean isPending() {
        return status == OrderStatus.PENDING;
    }
    
    public boolean isHold() {
        return status == OrderStatus.HOLD;
    }
    
    public boolean isCancelled() {
        return status == OrderStatus.CANCELLED;
    }
    
    public boolean isTerminal() {
        return status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED;
    }
    
    // --- WITHERS (para crear nuevas instancias con cambios) ---
    
    public OrderStatusInfo withStatus(OrderStatus newStatus) {
        return new OrderStatusInfo(newStatus, this.reason, this.metadata, LocalDateTime.now(), this.changedBy);
    }
    
    public OrderStatusInfo withReason(StatusReason newReason) {
        return new OrderStatusInfo(this.status, newReason, this.metadata, LocalDateTime.now(), this.changedBy);
    }
    
    public OrderStatusInfo withMetadata(Map<String, Object> newMetadata) {
        return new OrderStatusInfo(this.status, this.reason, newMetadata, LocalDateTime.now(), this.changedBy);
    }
    
    public OrderStatusInfo withChangedBy(String newChangedBy) {
        return new OrderStatusInfo(this.status, this.reason, this.metadata, LocalDateTime.now(), newChangedBy);
    }
    
    // --- EQUALS & HASHCODE ---
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderStatusInfo that = (OrderStatusInfo) o;
        return status == that.status &&
               reason == that.reason &&
               Objects.equals(metadata, that.metadata);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(status, reason, metadata);
    }
    
    @Override
    public String toString() {
        return "OrderStatusInfo{" +
               "status=" + status +
               ", reason=" + reason +
               ", metadata=" + metadata +
               ", changedAt=" + changedAt +
               ", changedBy='" + changedBy + '\'' +
               '}';
    }
}
