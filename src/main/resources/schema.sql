-- =============================================================================
-- WMS - Domain Events Schema
-- =============================================================================
-- Tabla para persistir eventos de dominio del módulo Orders.
-- Usado para auditoría, trazabilidad y replay de eventos.
-- =============================================================================

-- Nota: Esta tabla se crea automáticamente con ddl-auto=update en dev.
-- Para producción, usar Flyway migrations.

CREATE TABLE IF NOT EXISTS domain_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id VARCHAR(50) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    correlation_id VARCHAR(50),
    metadata VARCHAR(500),
    
    -- Índices para consultas frecuentes
    INDEX idx_aggregate_id (aggregate_id),
    INDEX idx_event_type (event_type),
    INDEX idx_occurred_at (occurred_at),
    INDEX idx_correlation_id (correlation_id)
);