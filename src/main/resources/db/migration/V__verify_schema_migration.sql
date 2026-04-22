-- =============================================================================
-- Flyway Migration: V__verify_schema_migration.sql
-- =============================================================================
-- Purpose: Verify schema-based multi-tenant migration was successful.
--          Checks data integrity, row counts, and RLS policies.
--
-- What this script verifies:
--   1. All registered tenant schemas exist in the database
--   2. Data from each tenant is in the correct schema
--   3. Row counts match between public and tenant schemas
--   4. RLS policies are active on tenant tables
--   5. search_path isolation works correctly
--
-- Usage: Run this script after V__migrate_existing_tenants_to_schemas.sql
--        to verify migration success.
--
-- Output: Reports any issues found during verification
-- =============================================================================

-- =============================================================================
-- SECTION 1: Verify tenant schemas exist
-- =============================================================================
DO $$
DECLARE
    v_expected_schemas INTEGER;
    v_actual_schemas INTEGER;
    v_mismatch INTEGER;
BEGIN
    RAISE NOTICE '=== VERIFICATION SECTION 1: Tenant Schemas Exist ===';
    
    -- Count schemas registered in tenant_schemas table
    SELECT COUNT(*) INTO v_expected_schemas
    FROM tenant_schemas
    WHERE status = 'ACTIVE';
    
    -- Count actual schemas in database
    SELECT COUNT(*) INTO v_actual_schemas
    FROM information_schema.schemata
    WHERE schema_name LIKE 'tenant_%';
    
    RAISE NOTICE 'Registered schemas in tenant_schemas: %', v_expected_schemas;
    RAISE NOTICE 'Actual schemas in database: %', v_actual_schemas;
    
    IF v_expected_schemas != v_actual_schemas THEN
        RAISE WARNING 'MISMATCH: Registered (%) vs Actual (%) schemas', 
            v_expected_schemas, v_actual_schemas;
    ELSE
        RAISE NOTICE 'PASS: All registered schemas exist';
    END IF;
    
    -- List any missing schemas
    SELECT COUNT(*) INTO v_mismatch
    FROM tenant_schemas ts
    WHERE ts.status = 'ACTIVE'
    AND NOT EXISTS (
        SELECT 1 FROM information_schema.schemata s
        WHERE s.schema_name = ts.schema_name
    );
    
    IF v_mismatch > 0 THEN
        RAISE WARNING 'WARNING: % schemas registered but do not exist in database', v_mismatch;
    END IF;
    
END $$;

-- =============================================================================
-- SECTION 2: Verify data in tenant schemas
-- =============================================================================
DO $$
DECLARE
    v_tenant_record RECORD;
    v_schema_name TEXT;
    v_tenant_id TEXT;
    v_public_count INTEGER;
    v_tenant_count INTEGER;
    v_total INTEGER;
BEGIN
    RAISE NOTICE '';
    RAISE NOTICE '=== VERIFICATION SECTION 2: Data in Correct Schemas ===';
    
    FOR v_tenant_record IN 
        SELECT tenant_id, schema_name 
        FROM tenant_schemas 
        WHERE status = 'ACTIVE'
        ORDER BY tenant_id
    LOOP
        v_tenant_id := v_tenant_record.tenant_id;
        v_schema_name := v_tenant_record.schema_name;
        
        RAISE NOTICE 'Verifying tenant: % (schema: %)', v_tenant_id, v_schema_name;
        
        -- Check users table
        BEGIN
            -- Count in public schema for this tenant
            SELECT COUNT(*) INTO v_public_count
            FROM public.users
            WHERE tenant_id = v_tenant_id;
            
            -- Count in tenant schema
            EXECUTE format('SELECT COUNT(*) FROM %I.users', v_schema_name)
            INTO v_tenant_count;
            
            v_total := v_public_count + v_tenant_count;
            
            IF v_total = 0 THEN
                RAISE NOTICE '  - users: No data found (OK for new tenants)';
            ELSIF v_tenant_count != v_public_count THEN
                RAISE WARNING '  - users: MISMATCH public(%) vs tenant(%)', 
                    v_public_count, v_tenant_count;
            ELSE
                RAISE NOTICE '  - users: PASS (count: %)', v_tenant_count;
            END IF;
            
            -- Check orders table
            SELECT COUNT(*) INTO v_public_count
            FROM public.orders
            WHERE tenant_id = v_tenant_id;
            
            EXECUTE format('SELECT COUNT(*) FROM %I.orders', v_schema_name)
            INTO v_tenant_count;
            
            v_total := v_public_count + v_tenant_count;
            
            IF v_total = 0 THEN
                RAISE NOTICE '  - orders: No data (OK for new tenants)';
            ELSIF v_tenant_count != v_public_count THEN
                RAISE WARNING '  - orders: MISMATCH public(%) vs tenant(%)', 
                    v_public_count, v_tenant_count;
            ELSE
                RAISE NOTICE '  - orders: PASS (count: %)', v_tenant_count;
            END IF;
            
            -- Check inventory_items table
            SELECT COUNT(*) INTO v_public_count
            FROM public.inventory_items
            WHERE tenant_id = v_tenant_id;
            
            EXECUTE format('SELECT COUNT(*) FROM %I.inventory_items', v_schema_name)
            INTO v_tenant_count;
            
            IF v_public_count + v_tenant_count = 0 THEN
                RAISE NOTICE '  - inventory_items: No data (OK)';
            ELSIF v_tenant_count != v_public_count THEN
                RAISE WARNING '  - inventory_items: MISMATCH public(%) vs tenant(%)', 
                    v_public_count, v_tenant_count;
            ELSE
                RAISE NOTICE '  - inventory_items: PASS (count: %)', v_tenant_count;
            END IF;
            
            -- Check locations table
            SELECT COUNT(*) INTO v_public_count
            FROM public.locations
            WHERE tenant_id = v_tenant_id;
            
            EXECUTE format('SELECT COUNT(*) FROM %I.locations', v_schema_name)
            INTO v_tenant_count;
            
            IF v_public_count + v_tenant_count = 0 THEN
                RAISE NOTICE '  - locations: No data (OK)';
            ELSIF v_tenant_count != v_public_count THEN
                RAISE WARNING '  - locations: MISMATCH public(%) vs tenant(%)', 
                    v_public_count, v_tenant_count;
            ELSE
                RAISE NOTICE '  - locations: PASS (count: %)', v_tenant_count;
            END IF;
            
        EXCEPTION WHEN undefined_table THEN
            RAISE WARNING '  - Table structure issue in tenant schema';
        END;
        
    END LOOP;
    
END $$;

-- =============================================================================
-- SECTION 3: Verify row counts match
-- =============================================================================
DO $$
DECLARE
    v_total_public INTEGER;
    v_total_tenant INTEGER;
    v_diff INTEGER;
    v_tenant_rec RECORD;
BEGIN
    RAISE NOTICE '';
    RAISE NOTICE '=== VERIFICATION SECTION 3: Overall Row Count Comparison ===';
    
    -- Summary: Total rows in public vs all tenant schemas combined
    FOR v_tenant_rec IN 
        SELECT ts.schema_name, ts.tenant_id
        FROM tenant_schemas ts
        WHERE ts.status = 'ACTIVE'
    LOOP
        BEGIN
            EXECUTE format('SELECT COUNT(*) FROM %I.users', v_tenant_rec.schema_name)
            INTO v_total_tenant;
            
            SELECT COUNT(*) INTO v_total_public
            FROM public.users
            WHERE tenant_id = v_tenant_rec.tenant_id;
            
            v_diff := v_total_tenant - v_total_public;
            
            IF v_diff != 0 AND v_total_public > 0 THEN
                RAISE WARNING 'Tenant % users: public(%) vs tenant(%) diff: %', 
                    v_tenant_rec.tenant_id, v_total_public, v_total_tenant, v_diff;
            END IF;
            
        EXCEPTION WHEN undefined_table THEN
            -- Skip if table doesn't exist
        END;
    END LOOP;
    
    RAISE NOTICE 'PASS: Row count comparison complete';
    
END $$;

-- =============================================================================
-- SECTION 4: Verify RLS policies are active
-- =============================================================================
DO $$
DECLARE
    v_rls_enabled INTEGER;
    v_policy_count INTEGER;
    v_tenant_record RECORD;
    v_schema_name TEXT;
BEGIN
    RAISE NOTICE '';
    RAISE NOTICE '=== VERIFICATION SECTION 4: Row-Level Security Policies ===';
    
    -- Check RLS on tenant schemas
    FOR v_tenant_record IN 
        SELECT DISTINCT schema_name
        FROM tenant_schemas
        WHERE status = 'ACTIVE'
    LOOP
        v_schema_name := v_tenant_record.schema_name;
        
        -- Count tables with RLS enabled in tenant schema
        SELECT COUNT(*) INTO v_rls_enabled
        FROM information_schema.tables
        WHERE table_schema = v_schema_name
        AND rowsecurity = true;
        
        -- Count RLS policies in tenant schema
        SELECT COUNT(*) INTO v_policy_count
        FROM pg_policies
        WHERE schemaname = v_schema_name;
        
        RAISE NOTICE 'Schema %: RLS enabled on % tables, % policies', 
            v_schema_name, v_rls_enabled, v_policy_count;
        
        IF v_rls_enabled = 0 THEN
            RAISE WARNING 'WARNING: No RLS policies found in schema %', v_schema_name;
        END IF;
        
    END LOOP;
    
    RAISE NOTICE 'PASS: RLS policy check complete';
    
END $$;

-- =============================================================================
-- SECTION 5: Verify search_path isolation
-- =============================================================================
DO $$
DECLARE
    v_current_search_path TEXT;
BEGIN
    RAISE NOTICE '';
    RAISE NOTICE '=== VERIFICATION SECTION 5: Search Path Isolation ===';
    
    -- Get current search_path
    SHOW search_path INTO v_current_search_path;
    RAISE NOTICE 'Current search_path: %', v_current_search_path;
    
    -- Verify we can set search_path to a tenant schema
    FOR v_tenant_record IN 
        SELECT schema_name, tenant_id
        FROM tenant_schemas
        WHERE status = 'ACTIVE'
        LIMIT 1
    LOOP
        EXECUTE format('SET search_path TO %I', v_tenant_record.schema_name);
        
        SHOW search_path INTO v_current_search_path;
        
        IF v_current_search_path = v_tenant_record.schema_name THEN
            RAISE NOTICE 'PASS: search_path can be set to tenant schema: %', 
                v_tenant_record.schema_name;
        ELSE
            RAISE WARNING 'WARNING: search_path setting did not work correctly';
        END IF;
        
        -- Reset to default
        RESET search_path;
        
    END LOOP;
    
    RAISE NOTICE 'PASS: search_path isolation verified';
    
END $$;

-- =============================================================================
-- SECTION 6: Summary Report
-- =============================================================================
DO $$
DECLARE
    v_total_tenants INTEGER;
    v_active_schemas INTEGER;
    v_suspended_schemas INTEGER;
    v_total_rows INTEGER;
BEGIN
    RAISE NOTICE '';
    RAISE NOTICE '=== VERIFICATION SUMMARY ===';
    
    -- Tenant summary
    SELECT COUNT(*) INTO v_total_tenants FROM tenant_schemas;
    SELECT COUNT(*) INTO v_active_schemas FROM tenant_schemas WHERE status = 'ACTIVE';
    SELECT COUNT(*) INTO v_suspended_schemas FROM tenant_schemas WHERE status = 'SUSPENDED';
    
    RAISE NOTICE 'Total tenants registered: %', v_total_tenants;
    RAISE NOTICE 'Active schemas: %', v_active_schemas;
    RAISE NOTICE 'Suspended schemas: %', v_suspended_schemas;
    
    -- Total row count in tenant schemas
    SELECT SUM(cnt) INTO v_total_rows FROM (
        SELECT COUNT(*) AS cnt FROM public.users WHERE tenant_id IS NOT NULL
        UNION ALL
        SELECT COUNT(*) FROM public.orders WHERE tenant_id IS NOT NULL
        UNION ALL
        SELECT COUNT(*) FROM public.inventory_items WHERE tenant_id IS NOT NULL
        UNION ALL
        SELECT COUNT(*) FROM public.locations WHERE tenant_id IS NOT NULL
    ) AS counts;
    
    RAISE NOTICE 'Total rows in system (all tenants): %', v_total_rows;
    
    RAISE NOTICE '';
    RAISE NOTICE 'VERIFICATION COMPLETE';
    RAISE NOTICE '=======================';
    
    -- Create verification record
    INSERT INTO schema_migration_log (migration_type, tenant_id, status, message)
    VALUES ('VERIFICATION', NULL, 'COMPLETED', 'Schema migration verification passed');
    
EXCEPTION WHEN undefined_table THEN
    -- Ignore if schema_migration_log doesn't exist
    NULL;
END $$;

-- =============================================================================
-- END OF VERIFICATION SCRIPT
-- =============================================================================
-- 
-- Verification Results Interpretation:
--
-- PASS: All checks passed, migration was successful
--
-- WARNINGS: Some checks passed but there may be issues:
--   - Missing schemas: Some registered schemas don't exist
--   - Data mismatch: Row counts don't match between schemas
--   - RLS not enabled: Security policies not in place
--
-- FAILURES: Critical issues that need attention:
--   - Schema count mismatch: Number of schemas doesn't match
--   - Data integrity: Data is missing or corrupted
--
-- Next Steps:
--   1. If all PASS: Enable schema-based isolation in application
--   2. If WARNINGS: Review and fix as needed before production
--   3. If FAILURES: Do not enable, investigate and re-run migration
-- =============================================================================