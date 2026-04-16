package com.juanbenevento.wms.orders.domain.model;

/**
 * Razones/Subtipos para los estados HOLD y CANCELLED.
 * 
 * Este enum es EXTENSIBLE - para agregar nuevas razones solo se agrega
 * una línea al enum, sin romper código existente.
 * 
 * Las razones están organizadas por categoría para facilitar reportes.
 */
public enum StatusReason {

    // ============================================
    // RAZONES PARA HOLD
    // ============================================
    
    /** Espera de pago o verificación de método de pago */
    PAYMENT_HOLD("Hold", "Espera de pago"),
    
    /** Stock insuficiente o en espera de reabastecimiento */
    INVENTORY_SHORTAGE("Hold", "Stock insuficiente"),
    
    /** Verificación manual por sospecha de fraude */
    FRAUD_HOLD("Hold", "Sospecha de fraude"),
    
    /** El cliente solicitó pausar el pedido */
    CUSTOMER_REQUEST("Hold", "Solicitud del cliente"),
    
    /** Verificación de calidad requerida */
    QUALITY_HOLD("Hold", "Verificación de calidad"),
    
    /** Espera de que Inventory asigne stock */
    ALLOCATION_PENDING("Hold", "Esperando asignación de stock"),
    
    /** Revisión manual requerida por alguna regla de negocio */
    MANUAL_REVIEW("Hold", "Revisión manual requerida"),

    // ============================================
    // RAZONES PARA CANCELLED
    // ============================================
    
    /** Cancelado a petición del cliente */
    CUSTOMER_CANCELLED("Cancelled", "Cancelado por cliente"),
    
    /** Cancelado por falta de stock después de intentos */
    OUT_OF_STOCK("Cancelled", "Stock agotado"),
    
    /** Fallo en el procesamiento del pago */
    PAYMENT_FAILED("Cancelled", "Pago fallido"),
    
    /** Detectado como fraudulento */
    FRAUDULENT("Cancelled", "Fraude detectado"),
    
    /** Cancelado por el sistema por timeout */
    SYSTEM_TIMEOUT("Cancelled", "Timeout del sistema"),

    // ============================================
    // RAZONES GENÉRICAS
    // ============================================
    
    /** Sin razón especial - transición normal */
    NONE("None", "Sin razón especial");
    
    private final String category;
    private final String description;
    
    StatusReason(String category, String description) {
        this.category = category;
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Verifica si esta razón corresponde a un estado de HOLD.
     */
    public boolean isHoldReason() {
        return this == PAYMENT_HOLD || 
               this == INVENTORY_SHORTAGE ||
               this == FRAUD_HOLD || 
               this == CUSTOMER_REQUEST ||
               this == QUALITY_HOLD ||
               this == ALLOCATION_PENDING ||
               this == MANUAL_REVIEW;
    }
    
    /**
     * Verifica si esta razón corresponde a un estado CANCELLED.
     */
    public boolean isCancelledReason() {
        return this == CUSTOMER_CANCELLED || 
               this == OUT_OF_STOCK ||
               this == PAYMENT_FAILED || 
               this == FRAUDULENT ||
               this == SYSTEM_TIMEOUT;
    }
    
    /**
     * Verifica si esta razón es genérica (NONE).
     */
    public boolean isNone() {
        return this == NONE;
    }
}
