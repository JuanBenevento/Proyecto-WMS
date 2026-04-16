package com.juanbenevento.wms.orders.domain.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Clase base abstracta para todos los eventos de dominio del módulo Orders.
 * 
 * Los eventos son:
 * - INMUTABLES: una vez creados no se modifican
 * - AUTO-DESCRIPTIVOS: contienen toda la información necesaria
 * - RASTREABLES: tienen correlationId para trazabilidad entre módulos
 * 
 * Esta clase es preparada para ser serializada (Kafka, DB, etc.)
 */
public abstract class DomainEvent {
    
    private final String eventId;
    private final String eventType;
    private final String aggregateId;
    private final String aggregateType;
    private final Instant occurredAt;
    private final String correlationId;
    private final Map<String, Object> metadata;
    
    protected DomainEvent(String aggregateId, String aggregateType, Map<String, Object> metadata) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = this.getClass().getSimpleName();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.occurredAt = Instant.now();
        this.correlationId = UUID.randomUUID().toString();
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }
    
    protected DomainEvent(String aggregateId, String aggregateType, String correlationId, 
                         Map<String, Object> metadata) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = this.getClass().getSimpleName();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.occurredAt = Instant.now();
        this.correlationId = correlationId;
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }
    
    // --- Getters ---
    
    public String getEventId() {
        return eventId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public String getAggregateId() {
        return aggregateId;
    }
    
    public String getAggregateType() {
        return aggregateType;
    }
    
    public Instant getOccurredAt() {
        return occurredAt;
    }
    
    public String getCorrelationId() {
        return correlationId;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public Object getMetadataValue(String key) {
        return metadata.get(key);
    }
    
    // --- Convenience ---
    
    public String getEventTypeShort() {
        // Remueve el sufijo "Event" si existe
        String type = getEventType();
        return type.endsWith("Event") ? type.substring(0, type.length() - 5) : type;
    }
    
    @Override
    public String toString() {
        return "DomainEvent{" +
               "eventId='" + eventId + '\'' +
               ", eventType='" + eventType + '\'' +
               ", aggregateId='" + aggregateId + '\'' +
               ", aggregateType='" + aggregateType + '\'' +
               ", occurredAt=" + occurredAt +
               ", correlationId='" + correlationId + '\'' +
               '}';
    }
}
