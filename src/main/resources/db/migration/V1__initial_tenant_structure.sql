-- =============================================================================
-- Flyway Migration: V1__initial_tenant_structure.sql
-- =============================================================================
-- Purpose: Creates the initial infrastructure for schema-based multi-tenant
--          isolation. This includes the tenant_schemas registry table that
--          tracks all tenant schemas created in the system.
--
-- WARNING: Do not modify this migration after deployment to production.
--          Create a new migration for any schema changes.
-- =============================================================================

-- Create function to safely check if a schema exists (avoids SQL injection)
CREATE OR REPLACE FUNCTION schema_exists(p_schema_name TEXT)
RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM information_schema.schemata 
        WHERE schema_name = p_schema_name
    );
END;
$$;

-- Create function to safely create a tenant schema with proper permissions
CREATE OR REPLACE FUNCTION create_tenant_schema(p_tenant_id TEXT)
RETURNS TEXT
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_schema_name TEXT;
BEGIN
    -- Normalize tenant ID: lowercase, replace spaces/special chars with underscores
    v_schema_name := 'tenant_' || lower(regexp_replace(p_tenant_id, '[^\w]', '_', 'g'));
    
    -- Check if schema already exists
    IF schema_exists(v_schema_name) THEN
        RAISE NOTICE 'Schema % already exists', v_schema_name;
        RETURN v_schema_name;
    END IF;
    
    -- Create the schema
    EXECUTE format('CREATE SCHEMA IF NOT EXISTS %I', v_schema_name);
    
    -- Grant permissions to current user (application user)
    EXECUTE format('GRANT USAGE ON SCHEMA %I TO CURRENT_USER', v_schema_name);
    EXECUTE format('GRANT ALL PRIVILEGES ON SCHEMA %I TO CURRENT_USER', v_schema_name);
    
    RAISE NOTICE 'Schema % created successfully', v_schema_name;
    RETURN v_schema_name;
END;
$$;

-- Create function to drop a tenant schema (with CASCADE to remove all objects)
CREATE OR REPLACE FUNCTION drop_tenant_schema(p_tenant_id TEXT)
RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_schema_name TEXT;
BEGIN
    -- Normalize tenant ID
    v_schema_name := 'tenant_' || lower(regexp_replace(p_tenant_id, '[^\w]', '_', 'g'));
    
    -- Check if schema exists
    IF NOT schema_exists(v_schema_name) THEN
        RAISE NOTICE 'Schema % does not exist', v_schema_name;
        RETURN FALSE;
    END IF;
    
    -- Drop schema with CASCADE (removes all objects)
    EXECUTE format('DROP SCHEMA %I CASCADE', v_schema_name);
    
    RAISE NOTICE 'Schema % dropped successfully', v_schema_name;
    RETURN TRUE;
END;
$$;

-- Create tenant_schemas registry table to track all tenant schemas
CREATE TABLE IF NOT EXISTS tenant_schemas (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL UNIQUE,
    schema_name VARCHAR(150) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    
    -- Constraints
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'DELETED')),
    CONSTRAINT chk_schema_name_format CHECK (schema_name ~ '^tenant_[a-z0-9_]+$')
);

-- Create indexes for efficient lookups
CREATE INDEX IF NOT EXISTS idx_tenant_schemas_tenant_id ON tenant_schemas(tenant_id);
CREATE INDEX IF NOT EXISTS idx_tenant_schemas_status ON tenant_schemas(status);
CREATE INDEX IF NOT EXISTS idx_tenant_schemas_created_at ON tenant_schemas(created_at);

-- Create function to update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_tenant_schema_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically update timestamp
DROP TRIGGER IF EXISTS trg_tenant_schemas_updated_at ON tenant_schemas;
CREATE TRIGGER trg_tenant_schemas_updated_at
    BEFORE UPDATE ON tenant_schemas
    FOR EACH ROW
    EXECUTE FUNCTION update_tenant_schema_timestamp();

-- Create function to register a new tenant schema in the registry
CREATE OR REPLACE FUNCTION register_tenant_schema(p_tenant_id TEXT, p_created_by TEXT DEFAULT 'SYSTEM')
RETURNS BIGINT
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_schema_name TEXT;
    v_id BIGINT;
BEGIN
    -- Create the actual schema
    v_schema_name := create_tenant_schema(p_tenant_id);
    
    -- Register in the tenant_schemas table
    INSERT INTO tenant_schemas (tenant_id, schema_name, status, created_by)
    VALUES (p_tenant_id, v_schema_name, 'ACTIVE', p_created_by)
    ON CONFLICT (tenant_id) DO UPDATE SET
        status = 'ACTIVE',
        updated_at = CURRENT_TIMESTAMP
    RETURNING id INTO v_id;
    
    RETURN v_id;
END;
$$;

-- Grant execute permissions to application user
GRANT EXECUTE ON FUNCTION schema_exists(TEXT) TO CURRENT_USER;
GRANT EXECUTE ON FUNCTION create_tenant_schema(TEXT) TO CURRENT_USER;
GRANT EXECUTE ON FUNCTION drop_tenant_schema(TEXT) TO CURRENT_USER;
GRANT EXECUTE ON FUNCTION register_tenant_schema(TEXT, TEXT) TO CURRENT_USER;

-- Comments for documentation
COMMENT ON TABLE tenant_schemas IS 'Registry of all tenant schemas for schema-based multi-tenant isolation';
COMMENT ON COLUMN tenant_schemas.tenant_id IS 'Unique identifier for the tenant';
COMMENT ON COLUMN tenant_schemas.schema_name IS 'PostgreSQL schema name (tenant_<normalized_id>)';
COMMENT ON COLUMN tenant_schemas.status IS 'Schema status: PENDING, ACTIVE, SUSPENDED, DELETED';