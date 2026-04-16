package com.juanbenevento.wms.orders.infrastructure.out;

import com.juanbenevento.wms.orders.application.mapper.DomainEventMapper;
import com.juanbenevento.wms.orders.domain.event.DomainEvent;

import java.time.Instant;
import java.util.Map;

/**
 * Wrapper de DomainEvent para eventos retrievados de la base de datos.
 * 
 * Contiene los datos serializados-json del evento original.
 * Útil para auditoría y consulta, pero para replay
 * de dominio se necesitaría un EventFactory que
 * reconstruya el tipo concreto de evento.
 * 
 * Esta clase implementa DomainEvent pero sus métodos
 * devuelven los datos almacenados, no el evento reconstruido.
 */
public class StoredDomainEvent extends DomainEvent {

    private final String storedEventId;
    private final String storedEventType;
    private final Instant storedOccurredAt;
    private final String storedPayload;
    private final DomainEventMapper mapper;

    public StoredDomainEvent(
        String storedEventId,
        String storedEventType,
        String aggregateId,
        String aggregateType,
        Instant occurredAt,
        String correlationId,
        Map<String, Object> metadata,
        String storedPayload,
        DomainEventMapper mapper
    ) {
        super(aggregateId, aggregateType, correlationId, metadata);
        this.storedEventId = storedEventId;
        this.storedEventType = storedEventType;
        this.storedOccurredAt = occurredAt;
        this.storedPayload = storedPayload;
        this.mapper = mapper;
    }

    @Override
    public String getEventId() {
        return storedEventId;
    }

    @Override
    public String getEventType() {
        return storedEventType;
    }

    @Override
    public Instant getOccurredAt() {
        return storedOccurredAt;
    }

    /**
     * Obtiene el payload JSON tal como fue almacenado.
     */
    public String getStoredPayload() {
        return storedPayload;
    }

    /**
     * Deserializa el payload a un Map.
     * Útil para debugging o inspección.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getPayloadAsMap() {
        try {
            return mapper.getObjectMapper().readValue(storedPayload, Map.class);
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }

    /**
     * Deserializa el payload al tipo específico de evento.
     * 
     * NOTA: Esto requiere un EventFactory registry
     * que conozca los tipos concretos de evento.
     */
    public Object deserializePayload() {
        return getPayloadAsMap();
    }
}