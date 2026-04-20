package com.juanbenevento.wms.orders.infrastructure.out;

import com.juanbenevento.wms.orders.application.mapper.DomainEventMapper;
import com.juanbenevento.wms.orders.application.port.out.DomainEventRepositoryPort;
import com.juanbenevento.wms.orders.domain.event.DomainEvent;
import com.juanbenevento.wms.orders.infrastructure.out.persistence.DomainEventEntity;
import com.juanbenevento.wms.orders.infrastructure.out.persistence.SpringDataDomainEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Adapter que implementa DomainEventRepositoryPort usando JPA/Spring Data.
 * 
 * Convierte DomainEvent a DomainEventEntity y persiste.
 * Usa ObjectMapper para serializar el payload a JSON.
 * 
 * IMPORTANTE: La persistencia de eventos es CRÍTICA para auditoría
 * y trazabilidad, por lo que usamos REQUIRED para que se
 * ejecute dentro de la transacción del caller.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventPersistenceAdapter implements DomainEventRepositoryPort {

    private final SpringDataDomainEventRepository jpaRepository;
    private final DomainEventMapper mapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void save(DomainEvent event) {
        try {
            DomainEventEntity entity = mapper.toEntity(event);
            jpaRepository.save(entity);
            log.debug("DomainEvent persisted: {} for aggregate {}", 
                event.getEventType(), event.getAggregateId());
        } catch (Exception e) {
            // Loguear error pero no bloquear - la auditoría es importante
            // pero el procesamiento del evento es crítico
            log.error("Error persisting DomainEvent {}: {}", 
                event.getEventId(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveAll(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        
        try {
            List<DomainEventEntity> entities = events.stream()
                .map(mapper::toEntity)
                .toList();
            jpaRepository.saveAll(entities);
            log.debug("Persisted {} DomainEvents", entities.size());
        } catch (Exception e) {
            log.error("Error persisting DomainEvents batch: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainEvent> findByAggregateId(String aggregateId) {
        List<DomainEventEntity> entities = jpaRepository.findByAggregateIdOrderByOccurredAtAsc(aggregateId);
        return mapToStoredEvents(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainEvent> findByEventType(String eventType) {
        List<DomainEventEntity> entities = jpaRepository.findByEventTypeOrderByOccurredAtAsc(eventType);
        return mapToStoredEvents(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainEvent> findByEventTypeAndAggregateId(String eventType, String aggregateId) {
        List<DomainEventEntity> entities = jpaRepository.findByEventTypeAndAggregateIdOrderByOccurredAtAsc(eventType, aggregateId);
        return mapToStoredEvents(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainEvent> findByCorrelationId(String correlationId) {
        List<DomainEventEntity> entities = jpaRepository.findByCorrelationIdOrderByOccurredAtAsc(correlationId);
        return mapToStoredEvents(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainEvent> findByOccurredAtBetween(Instant start, Instant end) {
        List<DomainEventEntity> entities = jpaRepository.findByOccurredAtBetween(start, end);
        return mapToStoredEvents(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public DomainEvent findLastEventByAggregateId(String aggregateId) {
        DomainEventEntity entity = jpaRepository.findLastEventByAggregateId(aggregateId);
        return entity != null ? mapToStoredEvent(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public long countByAggregateId(String aggregateId) {
        return jpaRepository.countByAggregateId(aggregateId);
    }

    /**
     * Convierte entidades a StoredDomainEvent wrappers.
     */
    private List<DomainEvent> mapToStoredEvents(List<DomainEventEntity> entities) {
        return entities.stream()
            .map(this::mapToStoredEvent)
            .toList();
    }

    /**
     * Convierte una entidad a un StoredDomainEvent.
     * 
     * StoredDomainEvent es un wrapper que contiene los datos
     * serializados del evento original. Para reconstrucción
     * del tipo concreto se necesitaría un factory.
     */
    private DomainEvent mapToStoredEvent(DomainEventEntity entity) {
        Map<String, Object> metadata = entity.getMetadata() != null 
            ? mapper.toMetadataMap(entity.getMetadata())
            : Map.of();
        
        // Crear un evento placeholder con los datos del storage
        return new StoredDomainEvent(
            entity.getEventId(),
            entity.getEventType(),
            entity.getAggregateId(),
            entity.getAggregateType(),
            entity.getOccurredAt(),
            entity.getCorrelationId(),
            metadata,
            entity.getPayload(),
            mapper
        );
    }
}