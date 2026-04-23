-- =============================================================================
-- Flyway Migration: V__rollback_schema_migration.sql
-- =============================================================================
-- Purpose: Rollback schema-based multi-tenant migration and restore soft multi-tenancy
--          (tenant_id column approach) in case of emergency.
--
-- WARNING: This is an EMERGENCY ROLLBACK script.
--          Use only if the schema migration causes critical issues.
--
-- What this migration does:
--   1. Copies data from tenant schemas back to public schema
--   2. Restores tenant_id column values in public tables
--   3. Updates tenant_schemas registry to mark schemas as ROLLED_BACK
--   4. Does NOT drop tenant schemas (preserved for potential re-migration)
--
-- Prerequisites:
--   1. V__migrate_existing_tenants_to_schemas.sql must have been applied
--   2. Application should be stopped during rollback
--
-- NOTE: This rollback preserves the tenant schemas in database.
--       They can be re-used or dropped manually later.
-- =============================================================================

-- =============================================================================
-- IMPORTANT SAFETY CHECK
-- =============================================================================
-- Uncomment the following line to enable rollback
-- BEGIN;

DO $$
BEGIN
    RAISE EXCEPTION 'IMPORTANT: This is a rollback migration.
    
    To proceed with rollback, you must:
    1. Stop all application instances
    2. Ensure no active connections to the database
    3. Uncomment the "BEGIN;" statement above
    4. Run this migration manually with psql or database tool
    
    If you are sure you want to rollback, edit this file and remove
    the RAISE EXCEPTION statement after verifying the conditions above.
    ';
END $$;
-- =============================================================================

-- START TRANSACTION (uncomment after reading warning above)
-- BEGIN;

-- =============================================================================
-- STEP 1: Create rollback tracking table
-- =============================================================================
CREATE TABLE IF NOT EXISTS schema_rollback_log (
    id BIGSERIAL PRIMARY KEY,
    rollback_type VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(100),
    schema_name VARCHAR(150),
    status VARCHAR(20) NOT NULL DEFAULT 'STARTED',
    message TEXT,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    rows_restored INTEGER DEFAULT 0
);

-- =============================================================================
-- STEP 2: Get list of registered tenant schemas to rollback
-- =============================================================================
DO $$
DECLARE
    v_tenant_record RECORD;
    v_schema_name TEXT;
    v_tenant_id TEXT;
    v_log_id BIGINT;
    v_rows_count INTEGER;
    v_has_data BOOLEAN;
BEGIN
    RAISE NOTICE '=== Starting Schema Migration Rollback ===';
    
    -- Log rollback start
    INSERT INTO schema_rollback_log (rollback_type, tenant_id, status, message)
    VALUES ('ROLLBACK_START', NULL, 'STARTED', 'Starting rollback of schema-based isolation')
    RETURNING id INTO v_log_id;
    
    -- Process each registered tenant schema
    FOR v_tenant_record IN 
        SELECT tenant_id, schema_name 
        FROM tenant_schemas 
        WHERE status = 'ACTIVE'
        ORDER BY tenant_id
    LOOP
        v_tenant_id := v_tenant_record.tenant_id;
        v_schema_name := v_tenant_record.schema_name;
        
        RAISE NOTICE 'Rolling back tenant: % (schema: %)', v_tenant_id, v_schema_name;
        
        -- Log start of tenant rollback
        INSERT INTO schema_rollback_log (rollback_type, tenant_id, schema_name, status, message)
        VALUES ('TENANT_ROLLBACK', v_tenant_id, v_schema_name, 'STARTED', 'Starting data restoration')
        RETURNING id INTO v_log_id;
        
        -- =====================================================================
        -- STEP 2a: Check if tenant schema has data and restore to public schema
        -- =====================================================================
        
        -- Check if users exist in tenant schema
        EXECUTE format('SELECT EXISTS(SELECT 1 FROM %I.users LIMIT 1)', v_schema_name)
        INTO v_has_data;
        
        IF v_has_data THEN
            -- Restore users data to public schema (fill tenant_id column)
            EXECUTE format('INSERT INTO public.users 
                SELECT * FROM %I.users', v_schema_name);
            GET DIAGNOSTICS v_rows_count = ROW_COUNT;
            RAISE NOTICE '  Restored % users for tenant %', v_rows_count, v_tenant_id;
        END IF;
        
        -- Check and restore orders
        EXECUTE format('SELECT EXISTS(SELECT 1 FROM %I.orders LIMIT 1)', v_schema_name)
        INTO v_has_data;
        
        IF v_has_data THEN
            EXECUTE format('INSERT INTO public.orders 
                SELECT * FROM %I.orders', v_schema_name);
            GET DIAGNOSTICS v_rows_count = ROW_COUNT;
            RAISE NOTICE '  Restored % orders for tenant %', v_rows_count, v_tenant_id;
        END IF;
        
        -- Check and restore order_lines
        EXECUTE format('SELECT EXISTS(SELECT 1 FROM %I.order_lines LIMIT 1)', v_schema_name)
        INTO v_has_data;
        
        IF v_has_data THEN
            EXECUTE format('INSERT INTO public.order_lines 
                SELECT * FROM %I.order_lines', v_schema_name);
            GET DIAGNOSTICS v_rows_count = ROW_COUNT;
            RAISE NOTICE '  Restored % order_lines for tenant %', v_rows_count, v_tenant_id;
        END IF;
        
        -- Check and restore inventory_items
        EXECUTE format('SELECT EXISTS(SELECT 1 FROM %I.inventory_items LIMIT 1)', v_schema_name)
        INTO v_has_data;
        
        IF v_has_data THEN
            EXECUTE format('INSERT INTO public.inventory_items 
                SELECT * FROM %I.inventory_items', v_schema_name);
            GET DIAGNOSTICS v_rows_count = ROW_COUNT;
            RAISE NOTICE '  Restored % inventory_items for tenant %', v_rows_count, v_tenant_id;
        END IF;
        
        -- Check and restore locations
        EXECUTE format('SELECT EXISTS(SELECT 1 FROM %I.locations LIMIT 1)', v_schema_name)
        INTO v_has_data;
        
        IF v_has_data THEN
            EXECUTE format('INSERT INTO public.locations 
                SELECT * FROM %I.locations', v_schema_name);
            GET DIAGNOSTICS v_rows_count = ROW_COUNT;
            RAISE NOTICE '  Restored % locations for tenant %', v_rows_count, v_tenant_id;
        END IF;
        
        -- Check and restore products (if exists)
        BEGIN
            EXECUTE format('SELECT EXISTS(SELECT 1 FROM %I.products LIMIT 1)', v_schema_name)
            INTO v_has_data;
            
            IF v_has_data THEN
                EXECUTE format('INSERT INTO public.products 
                    SELECT * FROM %I.products', v_schema_name);
                GET DIAGNOSTICS v_rows_count = ROW_COUNT;
                RAISE NOTICE '  Restored % products for tenant %', v_rows_count, v_tenant_id;
            END IF;
        EXCEPTION WHEN undefined_table THEN
            RAISE NOTICE '  Products table not found in tenant schema, skipping';
        END;
        
        -- =====================================================================
        -- STEP 2b: Update tenant_schemas registry
        -- =====================================================================
        
        UPDATE tenant_schemas 
        SET status = 'ROLLED_BACK', 
            updated_at = CURRENT_TIMESTAMP
        WHERE tenant_id = v_tenant_id;
        
        -- Log successful tenant rollback
        UPDATE schema_rollback_log 
        SET status = 'COMPLETED', 
            completed_at = CURRENT_TIMESTAMP, 
            message = 'Data restored to public schema'
        WHERE id = v_log_id;
        
        RAISE NOTICE 'Successfully rolled back tenant: %', v_tenant_id;
        
    END LOOP;
    
    RAISE NOTICE '=== Rollback completed for all tenants ===';
    
END $$;

-- =============================================================================
-- STEP 3: Verify rollback success
-- =============================================================================
DO $$
DECLARE
    v_tenants_count INTEGER;
    v_rollback_count INTEGER;
    v_public_users INTEGER;
    v_public_orders INTEGER;
    v_public_inventory INTEGER;
    v_public_locations INTEGER;
BEGIN
    RAISE NOTICE '=== Verifying Rollback Results ===';
    
    -- Count rolled back tenants
    SELECT COUNT(*) INTO v_rollback_count 
    FROM tenant_schemas 
    WHERE status = 'ROLLED_BACK';
    
    -- Count total rows in public schema tables
    SELECT COUNT(*) INTO v_public_users FROM public.users;
    SELECT COUNT(*) INTO v_public_orders FROM public.orders;
    SELECT COUNT(*) INTO v_public_inventory FROM public.inventory_items;
    SELECT COUNT(*) INTO v_public_locations FROM public.locations;
    
    RAISE NOTICE 'Tenants rolled back: %', v_rollback_count;
    RAISE NOTICE 'Public schema data after rollback:';
    RAISE NOTICE '  - users: %', v_public_users;
    RAISE NOTICE '  - orders: %', v_public_orders;
    RAISE NOTICE '  - inventory_items: %', v_public_inventory;
    RAISE NOTICE '  - locations: %', v_public_locations;
    
    -- Log rollback completion
    INSERT INTO schema_rollback_log (rollback_type, tenant_id, status, message)
    VALUES ('ROLLBACK_COMPLETE', NULL, 'COMPLETED', 'All schemas rolled back, data restored to public schema');
    
    RAISE NOTICE 'Rollback verification complete';
    
END $$;

-- COMMIT the transaction (uncomment after reading warning above)
-- COMMIT;

-- =============================================================================
-- POST-ROLLBACK NOTES
-- =============================================================================
-- 
-- After running this rollback:
--
-- 1. VERIFY: Check that all data is back in public schema tables
-- 2. TEST: Resume application with soft multi-tenancy (tenant_id column)
-- 3. MONITOR: Watch for any data integrity issues
--
-- NOTES:
-- - The tenant schemas are PRESERVED in database (not dropped)
-- - They can be manually dropped later with: DROP SCHEMA tenant_xxx CASCADE;
-- - The tenant_schemas registry has status = ROLLED_BACK
-- - To re-run migration later:
--   1. Delete data from public schema tables (if any duplicates)
--   2. Update tenant_schemas set status = 'ACTIVE'
--   3. Run V__migrate_existing_tenants_to_schemas.sql again
--
-- WARNING: If the application was running during rollback,
-- data in the public schema may have duplicates or conflicts.
-- Review and clean up data before resuming operations.
-- =============================================================================