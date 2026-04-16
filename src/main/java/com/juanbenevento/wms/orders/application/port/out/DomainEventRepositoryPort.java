package com.juanbenevento.wms.orders.application.port.out;

import com.juanbenevento.wms.orders.domain.event.DomainEvent;

import java.time.Instant;
import java.util.List;

/**
 * Puerto de salida para la persistencia de DomainEvents.
 * 
 * Define las operaciones que el dominio necesita para:
 * - Persistir eventos de dominio
 * - Consultar eventos por aggregate, tipo, o rango de tiempo
 * - Reconstruir estado mediante replay de eventos
 */
public interface DomainEventRepositoryPort {

    /**
     * Persiste un evento de dominio.
     * 
     * @param event el evento a persistir
     */
    void save(DomainEvent event);

    /**
     * Persiste eventos de forma masiva.
     * Útil para replay o migración.
     */
    void saveAll(List<DomainEvent> events);

    /**
     * Busca todos los eventos de un aggregate específico.
     * Ordenados por occurredAt ASC.
     */
    List<DomainEvent> findByAggregateId(String aggregateId);

    /**
     * Busca eventos por tipo.
     */
    List<DomainEvent> findByEventType(String eventType);

    /**
     * Busca eventos por tipo y aggregate.
     */
    List<DomainEvent> findByEventTypeAndAggregateId(String eventType, String aggregateId);

    /**
     * Busca eventos por correlationId (trazabilidad entre módulos).
     */
    List<DomainEvent> findByCorrelationId(String correlationId);

    /**
     * Busca eventos ocurridos en un rango de tiempo.
     */
    List<DomainEvent> findByOccurredAtBetween(Instant start, Instant end);

    /**
     * Obtiene el último evento de un aggregate.
     * Útil para replay.
     */
    DomainEvent findLastEventByAggregateId(String aggregateId);

    /**
     * Cuenta eventos de un aggregate.
     */
    long countByAggregateId(String aggregateId);
}