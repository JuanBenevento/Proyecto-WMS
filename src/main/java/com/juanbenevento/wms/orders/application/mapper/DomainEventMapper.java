package com.juanbenevento.wms.orders.application.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.juanbenevento.wms.orders.domain.event.DomainEvent;
import com.juanbenevento.wms.orders.infrastructure.out.persistence.DomainEventEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapper para convertir entre DomainEvent y DomainEventEntity.
 * 
 * Usa ObjectMapper para serializar el payload a JSON.
 */
@Component
@Slf4j
public class DomainEventMapper {

    private final ObjectMapper objectMapper;

    public DomainEventMapper() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Convierte un DomainEvent a DomainEventEntity.
     */
    public DomainEventEntity toEntity(DomainEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            String metadata = event.getMetadata().isEmpty() 
                ? null 
                : objectMapper.writeValueAsString(event.getMetadata());

            return DomainEventEntity.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType())
                .aggregateId(event.getAggregateId())
                .aggregateType(event.getAggregateType())
                .payload(payload)
                .occurredAt(event.getOccurredAt())
                .correlationId(event.getCorrelationId())
                .metadata(metadata)
                .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing domain event to JSON", e);
        }
    }

    /**
     * Convierte un DomainEventEntity a DomainEvent (genérico).
     * 
     * Nota: En la práctica, se necesita el tipo concreto del evento para
     * reconstruir el objeto del dominio. Este método devuelve un Map
     * con los datos serializables.
     */
    public Map<String, Object> toDomain(DomainEventEntity entity) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("eventId", entity.getEventId());
            data.put("eventType", entity.getEventType());
            data.put("aggregateId", entity.getAggregateId());
            data.put("aggregateType", entity.getAggregateType());
            data.put("payload", objectMapper.readValue(entity.getPayload(), Object.class));
            data.put("occurredAt", entity.getOccurredAt());
            data.put("correlationId", entity.getCorrelationId());
            
            if (entity.getMetadata() != null && !entity.getMetadata().isEmpty()) {
                data.put("metadata", objectMapper.readValue(entity.getMetadata(), Map.class));
            }
            
            return data;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing domain event from JSON", e);
        }
    }

    /**
     * Convierte una lista de entidades a dominio.
     */
    public List<Map<String, Object>> toDomainList(List<DomainEventEntity> entities) {
        return entities.stream()
            .map(this::toDomain)
            .toList();
    }

    /**
     * Convierte el campo metadata (JSON string) a Map.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> toMetadataMap(String metadataJson) {
        if (metadataJson == null || metadataJson.isEmpty()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(metadataJson, Map.class);
        } catch (JsonProcessingException e) {
            log.warn("Error deserializing metadata: {}", e.getMessage());
            return Map.of();
        }
    }

    /**
     * Obtiene el ObjectMapper para uso directo en el adapter.
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}