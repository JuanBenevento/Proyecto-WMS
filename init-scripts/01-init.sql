-- =============================================================================
-- WMS Enterprise - PostgreSQL Initialization Script
-- =============================================================================
-- This script runs automatically when PostgreSQL container starts for the first time
-- It sets up initial database configuration and creates seed data for development
-- =============================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create application user (for production use separate users with limited privileges)
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

-- Create Flyway history table schema if not exists (Flyway will manage this)
-- Note: Flyway migrations will create tables and the tenant_schemas registry

-- Create superadmin user for initial setup (password: admin123)
-- This is a SEED DATA for development only - remove in production!
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM tenants WHERE id = 'SYSTEM') THEN
        INSERT INTO tenants (id, name, status, email, created_at, updated_at, version)
        VALUES ('SYSTEM', 'System Tenant', 'ACTIVE', 'admin@wms.local', NOW(), NOW(), 0);

        INSERT INTO users (tenant_id, username, password, role, created_at, version)
        VALUES ('SYSTEM', 'superadmin',
                '$2a$10$N9qo8uLOickgx2ZMRZoMye.IqS8gmlwEeZ0v8.0xT3Q8cR5wVxL6y', -- bcrypt hash of 'admin123'
                'SUPER_ADMIN', NOW(), 0);

        RAISE NOTICE 'Seed data created: superadmin / admin123';
    END IF;
END
$$;

-- Create sample tenant for demo purposes
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM tenants WHERE id = 'DEMO') THEN
        INSERT INTO tenants (id, name, status, email, created_at, updated_at, version)
        VALUES ('DEMO', 'Demo Company', 'ACTIVE', 'demo@wms.local', NOW(), NOW(), 0);

        -- Create demo users
        INSERT INTO users (tenant_id, username, password, role, created_at, version)
        VALUES
            ('DEMO', 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IqS8gmlwEeZ0v8.0xT3Q8cR5wVxL6y', 'ADMIN', NOW(), 0),
            ('DEMO', 'operator', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IqS8gmlwEeZ0v8.0xT3Q8cR5wVxL6y', 'USER', NOW(), 0);

        RAISE NOTICE 'Demo tenant created: admin / admin123, operator / admin123';
    END IF;
END
$$;

-- Log completion
RAISE NOTICE 'WMS Database initialization complete.';