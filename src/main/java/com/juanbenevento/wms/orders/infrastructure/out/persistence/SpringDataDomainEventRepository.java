package com.juanbenevento.wms.orders.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data Repository para DomainEventEntity.
 * 
 * Proporciona métodos de consulta útiles para:
 * - Auditoría por aggregate
 * - Consulta por tipo de evento
 * - Rango de fechas
 */
@Repository
public interface SpringDataDomainEventRepository extends JpaRepository<DomainEventEntity, Long> {

    /**
     * Busca todos los eventos de un aggregate específico.
     */
    List<DomainEventEntity> findByAggregateIdOrderByOccurredAtAsc(String aggregateId);

    /**
     * Busca eventos por tipo.
     */
    List<DomainEventEntity> findByEventTypeOrderByOccurredAtAsc(String eventType);

    /**
     * Busca eventos por tipo y aggregate.
     */
    List<DomainEventEntity> findByEventTypeAndAggregateIdOrderByOccurredAtAsc(String eventType, String aggregateId);

    /**
     * Busca eventos por correlationId (para trazabilidad entre módulos).
     */
    List<DomainEventEntity> findByCorrelationIdOrderByOccurredAtAsc(String correlationId);

    /**
     * Busca eventos ocurridos en un rango de tiempo.
     */
    @Query("SELECT e FROM DomainEventEntity e WHERE e.occurredAt BETWEEN :start AND :end ORDER BY e.occurredAt ASC")
    List<DomainEventEntity> findByOccurredAtBetween(
        @Param("start") Instant start,
        @Param("end") Instant end
    );

    /**
     * Busca el último evento de un aggregate (para replay).
     */
    @Query("SELECT e FROM DomainEventEntity e WHERE e.aggregateId = :aggregateId ORDER BY e.occurredAt DESC LIMIT 1")
    DomainEventEntity findLastEventByAggregateId(@Param("aggregateId") String aggregateId);

    /**
     * Cuenta eventos de un aggregate.
     */
    long countByAggregateId(String aggregateId);
}