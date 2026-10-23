-- =============================================================================
-- Flyway Migration: V3__inventory_lots_and_movements.sql
-- =============================================================================
-- Purpose: Persistent tables for the Lot aggregate and the InventoryMovement
--          audit log (feature real-persistence / B1). Replaces the in-memory
--          HashMap storage inside LotJpaAdapter / MovementJpaAdapter.
--
-- Notes:
--   * No FK from movements to lots: movements are an append-only audit log
--     that references lots by number (same style as stock_movement_logs).
--   * metadata as JSONB: Hibernate maps Map<String,String> with
--     @JdbcTypeCode(SqlTypes.JSON).
--   * CHECK constraints mirror the LotStatus / MovementType enums.
-- =============================================================================

CREATE TABLE inventory_lots (
    lot_number       VARCHAR(50)  NOT NULL PRIMARY KEY,
    product_sku      VARCHAR(255) NOT NULL,
    batch_number     VARCHAR(100),
    production_date  DATE,
    origin           VARCHAR(255),
    status           VARCHAR(20)  NOT NULL CHECK (status IN ('ACTIVE','EXHAUSTED','EXPIRED','QUARANTINE')),
    expiry_date      DATE,
    min_temperature  NUMERIC(5,2),
    max_temperature  NUMERIC(5,2),
    net_weight       NUMERIC(12,2),
    gross_weight     NUMERIC(12,2),
    metadata         JSONB,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL,
    created_by       VARCHAR(255)
);

CREATE INDEX idx_inventory_lots_product_sku ON inventory_lots (product_sku);
CREATE INDEX idx_inventory_lots_status      ON inventory_lots (status);
CREATE INDEX idx_inventory_lots_expiry_date ON inventory_lots (expiry_date);

CREATE TABLE inventory_movements (
    movement_id              UUID         NOT NULL PRIMARY KEY,
    type                     VARCHAR(20)  NOT NULL CHECK (type IN ('RECEIPT','ISSUE','TRANSFER','ADJUSTMENT','RETURN')),
    lot_number               VARCHAR(50)  NOT NULL,
    quantity                 NUMERIC(19,4) NOT NULL,
    from_location            VARCHAR(255),
    to_location              VARCHAR(255),
    reason                   VARCHAR(500),
    performed_by             VARCHAR(255),
    temperature_at_movement  NUMERIC(5,2),
    weight_at_movement       NUMERIC(12,2),
    certificate_url          VARCHAR(500),
    timestamp                TIMESTAMP    NOT NULL
);

CREATE INDEX idx_inventory_movements_lot_ts ON inventory_movements (lot_number, timestamp);
CREATE INDEX idx_inventory_movements_type   ON inventory_movements (type);
CREATE INDEX idx_inventory_movements_ts     ON inventory_movements (timestamp);
