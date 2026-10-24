-- =============================================================================
-- WMS Enterprise - PostgreSQL Initialization Script
-- =============================================================================
-- Runs only on FIRST container start (empty volume). Ownership contract:
--   - THIS script: database-level setup only (extensions, roles, grants).
--   - Flyway (V1/V2): schema ownership — tablas, tenant schemas, history.
--   - DataInitializer (Java): seed de aplicación (superadmin) al arrancar el backend.
--
-- IMPORTANTE: NO sembrar tenants/users acá. Este script corre ANTES de que
-- Flyway cree esas tablas (chicken-and-egg, rompía el init con exit 3) y el
-- seed duplicaría/conflictuaría con DataInitializer (además con otro password).
-- =============================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Application role (least privilege; el compose local aún conecta como postgres)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'wms_app') THEN
        CREATE ROLE wms_app WITH LOGIN PASSWORD 'wms_app_password';
        GRANT CONNECT ON DATABASE wms_db TO wms_app;
        GRANT USAGE ON SCHEMA public TO wms_app;
        GRANT ALL PRIVILEGES ON DATABASE wms_db TO wms_app;
    END IF;
END
$$;

-- Grant schema permissions to application user
GRANT ALL PRIVILEGES ON SCHEMA public TO wms_app;
