-- =============================================================================
-- WMS - Inventory Movement Schema
-- =============================================================================
-- Lotes para trazabilidad (inventario, frutihortícola, cadena de frío)
-- =============================================================================

CREATE TABLE IF NOT EXISTS lots (
    lot_number VARCHAR(50) PRIMARY KEY,
    product_sku VARCHAR(50) NOT NULL,
    batch_number VARCHAR(50),
    production_date DATE,
    origin VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expiry_date DATE,
    temperature_min DECIMAL(5,2),
    temperature_max DECIMAL(5,2),
    net_weight DECIMAL(10,3),
    gross_weight DECIMAL(10,3),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),

    INDEX idx_lots_product_sku (product_sku),
    INDEX idx_lots_status (status),
    INDEX idx_lots_expiry_date (expiry_date)
);

CREATE TABLE IF NOT EXISTS inventory_movements (
    movement_id VARCHAR(36) PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    lot_number VARCHAR(50) NOT NULL,
    quantity DECIMAL(10,3) NOT NULL,
    from_location VARCHAR(50),
    to_location VARCHAR(50),
    reason VARCHAR(255),
    performed_by VARCHAR(50) NOT NULL,
    temperature_at_movement DECIMAL(5,2),
    weight_at_movement DECIMAL(10,3),
    certificate_url VARCHAR(255),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_movements_lot_number (lot_number),
    INDEX idx_movements_type (type),
    INDEX idx_movements_timestamp (timestamp)
);

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