-- =============================================================================
-- Flyway Migration: V__migrate_existing_tenants_to_schemas.sql
-- =============================================================================
-- Purpose: Migrate existing tenants from soft multi-tenancy (tenant_id column)
--          to schema-based isolation (separate schemas per tenant).
--
-- WARNING: This is a ONE-TIME migration script for existing tenants.
--          Execute with CAUTION and after proper backup.
--
-- Prerequisites:
--   1. Full database backup before execution
--   2. No active write operations during migration
--   3. All Phase 1-5 migrations must be applied first
--
-- What this migration does:
--   1. Identifies all unique tenant_ids from existing tables in public schema
--   2. Creates a dedicated schema for each tenant (tenant_{normalized_tenant_id})
--   3. Creates core tables in each tenant schema with the same structure
--   4. Copies data from public schema tables to tenant-specific schemas
--   5. Registers each tenant schema in tenant_schemas table
--
-- Rollback: Use V__rollback_schema_migration.sql in case of emergency
-- =============================================================================

-- START TRANSACTION to ensure atomicity
BEGIN;

-- =============================================================================
-- STEP 0: Create logging table for migration tracking
-- =============================================================================
CREATE TABLE IF NOT EXISTS schema_migration_log (
    id BIGSERIAL PRIMARY KEY,
    migration_type VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(100),
    schema_name VARCHAR(150),
    status VARCHAR(20) NOT NULL DEFAULT 'STARTED',
    message TEXT,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    rows_migrated INTEGER DEFAULT 0
);

-- =============================================================================
-- STEP 1: Identify all unique tenant_ids from existing tables
-- =============================================================================
DO $$
DECLARE
    v_tenant_record RECORD;
    v_tenant_id VARCHAR(100);
    v_schema_name VARCHAR(150);
    v_count INTEGER := 0;
    v_log_id BIGINT;
BEGIN
    RAISE NOTICE '=== Starting Schema Migration for Existing Tenants ===';
    
    -- Create temporary table to collect all unique tenant IDs
    CREATE TEMP TABLE IF NOT EXISTS temp_existing_tenants (
        tenant_id VARCHAR(100) PRIMARY KEY
    );
    
    -- Collect tenant_ids from all tenant-aware tables in public schema
    -- Add each table that has tenant_id column
    
    -- From users table
    INSERT INTO temp_existing_tenants (tenant_id)
    SELECT DISTINCT tenant_id FROM users WHERE tenant_id IS NOT NULL
    ON CONFLICT (tenant_id) DO NOTHING;
    
    -- From orders table
    INSERT INTO temp_existing_tenants (tenant_id)
    SELECT DISTINCT tenant_id FROM orders WHERE tenant_id IS NOT NULL
    ON CONFLICT (tenant_id) DO NOTHING;
    
    -- From inventory_items table
    INSERT INTO temp_existing_tenants (tenant_id)
    SELECT DISTINCT tenant_id FROM inventory_items WHERE tenant_id IS NOT NULL
    ON CONFLICT (tenant_id) DO NOTHING;
    
    -- From locations table
    INSERT INTO temp_existing_tenants (tenant_id)
    SELECT DISTINCT tenant_id FROM locations WHERE tenant_id IS NOT NULL
    ON CONFLICT (tenant_id) DO NOTHING;
    
    -- From products table (if exists with tenant_id)
    INSERT INTO temp_existing_tenants (tenant_id)
    SELECT DISTINCT tenant_id FROM products WHERE tenant_id IS NOT NULL
    ON CONFLICT (tenant_id) DO NOTHING
    ON CONFLICT DO NOTHING;
    
    -- Get count of unique tenants
    SELECT COUNT(*) INTO v_count FROM temp_existing_tenants;
    RAISE NOTICE 'Found % unique tenants to migrate', v_count;
    
    -- Log migration start
    INSERT INTO schema_migration_log (migration_type, tenant_id, status, message)
    VALUES ('TENANT_DISCOVERY', NULL, 'COMPLETED', 'Found ' || v_count || ' tenants to migrate')
    RETURNING id INTO v_log_id;
    
END $$;

-- =============================================================================
-- STEP 2: Create schemas for each existing tenant and migrate data
-- =============================================================================
DO $$
DECLARE
    v_tenant_record RECORD;
    v_schema_name TEXT;
    v_tenant_id TEXT;
    v_log_id BIGINT;
    v_rows_count INTEGER;
BEGIN
    RAISE NOTICE '=== Creating schemas and migrating data for each tenant ===';
    
    FOR v_tenant_record IN SELECT tenant_id FROM temp_existing_tenants LOOP
        v_tenant_id := v_tenant_record.tenant_id;
        
        -- Log start of tenant migration
        INSERT INTO schema_migration_log (migration_type, tenant_id, status, message)
        VALUES ('TENANT_MIGRATION', v_tenant_id, 'STARTED', 'Starting schema creation and data migration')
        RETURNING id INTO v_log_id;
        
        -- Build schema name: tenant_{normalized_tenant_id}
        v_schema_name := 'tenant_' || lower(regexp_replace(v_tenant_id, '[^\w]', '_', 'g'));
        
        RAISE NOTICE 'Processing tenant: % -> schema: %', v_tenant_id, v_schema_name;
        
        -- Create the schema
        PERFORM create_tenant_schema(v_tenant_id);
        
        -- Grant permissions
        EXECUTE format('GRANT USAGE ON SCHEMA %I TO CURRENT_USER', v_schema_name);
        EXECUTE format('GRANT ALL PRIVILEGES ON SCHEMA %I TO CURRENT_USER', v_schema_name);
        
        -- =====================================================================
        -- STEP 2a: Recreate tables in tenant schema (structure only, no data yet)
        -- =====================================================================
        
        -- Create users table in tenant schema
        CREATE TABLE IF NOT EXISTS v_schema_name.users (
            LIKE public.users INCLUDING ALL
        );
        
        -- Create orders table in tenant schema
        CREATE TABLE IF NOT EXISTS v_schema_name.orders (
            LIKE public.orders INCLUDING ALL
        );
        
        -- Create order_lines table in tenant schema
        CREATE TABLE IF NOT EXISTS v_schema_name.order_lines (
            LIKE public.order_lines INCLUDING ALL
        );
        
        -- Create inventory_items table in tenant schema
        CREATE TABLE IF NOT EXISTS v_schema_name.inventory_items (
            LIKE public.inventory_items INCLUDING ALL
        );
        
        -- Create locations table in tenant schema
        CREATE TABLE IF NOT EXISTS v_schema_name.locations (
            LIKE public.locations INCLUDING ALL
        );
        
        -- Create products table in tenant schema (if exists in public)
        CREATE TABLE IF NOT EXISTS v_schema_name.products (
            LIKE public.products INCLUDING ALL
        );
        
        -- =====================================================================
        -- STEP 2b: Copy data from public schema to tenant schema
        -- =====================================================================
        
        -- Copy users data
        EXECUTE format('INSERT INTO %I.users SELECT * FROM public.users WHERE tenant_id = $1', v_schema_name)
        USING v_tenant_id;
        GET DIAGNOSTICS v_rows_count = ROW_COUNT;
        RAISE NOTICE '  Migrated % users for tenant %', v_rows_count, v_tenant_id;
        
        -- Copy orders data
        EXECUTE format('INSERT INTO %I.orders SELECT * FROM public.orders WHERE tenant_id = $1', v_schema_name)
        USING v_tenant_id;
        GET DIAGNOSTICS v_rows_count = ROW_COUNT;
        RAISE NOTICE '  Migrated % orders for tenant %', v_rows_count, v_tenant_id;
        
        -- Copy order_lines data (via orders)
        EXECUTE format('INSERT INTO %I.order_lines 
            SELECT ol.* FROM public.order_lines ol
            INNER JOIN public.orders o ON ol.order_id = o.id
            WHERE o.tenant_id = $1', v_schema_name)
        USING v_tenant_id;
        
        -- Copy inventory_items data
        EXECUTE format('INSERT INTO %I.inventory_items SELECT * FROM public.inventory_items WHERE tenant_id = $1', v_schema_name)
        USING v_tenant_id;
        GET DIAGNOSTICS v_rows_count = ROW_COUNT;
        RAISE NOTICE '  Migrated % inventory_items for tenant %', v_rows_count, v_tenant_id;
        
        -- Copy locations data
        EXECUTE format('INSERT INTO %I.locations SELECT * FROM public.locations WHERE tenant_id = $1', v_schema_name)
        USING v_tenant_id;
        GET DIAGNOSTICS v_rows_count = ROW_COUNT;
        RAISE NOTICE '  Migrated % locations for tenant %', v_rows_count, v_tenant_id;
        
        -- Copy products data (if table exists)
        BEGIN
            EXECUTE format('INSERT INTO %I.products SELECT * FROM public.products WHERE tenant_id = $1', v_schema_name)
            USING v_tenant_id;
        EXCEPTION WHEN undefined_table THEN
            RAISE NOTICE '  Products table not found in public schema, skipping';
        END;
        
        -- =====================================================================
        -- STEP 2c: Register tenant schema in registry
        -- =====================================================================
        
        INSERT INTO tenant_schemas (tenant_id, schema_name, status, created_by)
        VALUES (v_tenant_id, v_schema_name, 'ACTIVE', 'MIGRATION_SCRIPT')
        ON CONFLICT (tenant_id) DO UPDATE SET
            schema_name = v_schema_name,
            status = 'ACTIVE',
            updated_at = CURRENT_TIMESTAMP;
        
        -- Log successful completion
        UPDATE schema_migration_log 
        SET status = 'COMPLETED', completed_at = CURRENT_TIMESTAMP, message = 'Schema created and data migrated successfully'
        WHERE id = v_log_id;
        
        RAISE NOTICE 'Successfully migrated tenant: %', v_tenant_id;
        
    END LOOP;
    
    RAISE NOTICE '=== Schema migration completed for all tenants ===';
    
END $$;

-- =============================================================================
-- STEP 3: Verify migration success
-- =============================================================================
DO $$
DECLARE
    v_total_tenants INTEGER;
    v_registered_tenants INTEGER;
    v_schema_count INTEGER;
BEGIN
    RAISE NOTICE '=== Verifying Migration Results ===';
    
    -- Count unique tenants in original data
    SELECT COUNT(DISTINCT tenant_id) INTO v_total_tenants FROM (
        SELECT tenant_id FROM users WHERE tenant_id IS NOT NULL
        UNION
        SELECT tenant_id FROM orders WHERE tenant_id IS NOT NULL
        UNION
        SELECT tenant_id FROM inventory_items WHERE tenant_id IS NOT NULL
        UNION
        SELECT tenant_id FROM locations WHERE tenant_id IS NOT NULL
    ) AS all_tenants;
    
    -- Count registered schemas
    SELECT COUNT(*) INTO v_registered_tenants FROM tenant_schemas WHERE status = 'ACTIVE';
    
    -- Count actual schemas created
    SELECT COUNT(*) INTO v_schema_count FROM information_schema.schemata 
    WHERE schema_name LIKE 'tenant_%';
    
    RAISE NOTICE 'Original tenants found: %', v_total_tenants;
    RAISE NOTICE 'Registered schemas: %', v_registered_tenants;
    RAISE NOTICE 'Actual schemas in database: %', v_schema_count;
    
    IF v_total_tenants != v_registered_tenants THEN
        RAISE WARNING 'Mismatch between original tenants and registered schemas!';
    ELSE
        RAISE NOTICE 'Migration verification PASSED: All tenants registered successfully';
    END IF;
    
END $$;

-- =============================================================================
-- STEP 4: Create migration completion record
-- =============================================================================
INSERT INTO schema_migration_log (migration_type, tenant_id, status, message)
VALUES ('MIGRATION_COMPLETE', NULL, 'COMPLETED', 'All existing tenants migrated to schema-based isolation');

-- Commit the transaction
COMMIT;

-- =============================================================================
-- POST-MIGRATION NOTES
-- =============================================================================
-- 
-- After running this migration:
--
-- 1. VERIFY: Run V__verify_schema_migration.sql to ensure data integrity
-- 2. TEST: Test the application with schema-based isolation enabled
-- 3. BACKUP: Keep the database backup from before migration
-- 4. MONITOR: Watch logs for any tenant isolation issues
--
-- IMPORTANT NOTES:
-- - The tenant_id column is preserved in public schema tables for rollback
-- - To switch application to schema-based mode, enable the feature flag
-- - RLS policies are already in place from Phase 5 migrations
-- - New tenants created after this will automatically get their schemas
--
-- ROLLBACK INSTRUCTIONS (Emergency Only):
-- - Run V__rollback_schema_migration.sql to revert
-- - WARNING: This will not restore deleted data if application was running
-- =============================================================================
