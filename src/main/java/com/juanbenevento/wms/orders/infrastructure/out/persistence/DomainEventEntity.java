package com.juanbenevento.wms.orders.infrastructure.out.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * JPA Entity para persistir DomainEvents en la base de datos.
 * 
 * Permite:
 * - Auditoría completa de cambios de estado
 * - Trazabilidad en un WMS (regulatorio)
 * - Capacidad de replay de eventos si se necesita
 * - Debugging más fácil en producción
 */
@Entity
@Table(name = "domain_events", indexes = {
    @Index(name = "idx_aggregate_id", columnList = "aggregate_id"),
    @Index(name = "idx_event_type", columnList = "event_type"),
    @Index(name = "idx_occurred_at", columnList = "occurred_at"),
    @Index(name = "idx_correlation_id", columnList = "correlation_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 50)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 255)
    private String eventType;

    @Column(name = "aggregate_id", nullable = false, length = 255)
    private String aggregateId;

    @Column(name = "aggregate_type", nullable = false, length = 255)
    private String aggregateType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "correlation_id", length = 50)
    private String correlationId;

    @Column(length = 500)
    private String metadata;
}