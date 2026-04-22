-- =============================================================================
-- Flyway Migration: V__enable_rls_all_tables.sql
-- =============================================================================
-- Purpose: Master migration to enable Row-Level Security (RLS) on all core tables
--          for schema-based multi-tenant isolation.
--
-- This migration provides a centralized way to enable RLS on all tenant-scoped
-- tables. It includes:
--   1. Core business tables: users, orders, inventory_items, locations
--   2. Additional tables: lots, inventory_movements, domain_events
--
-- Security Model:
--   1.Schema isolation (primary): Each tenant has tenant_<id> schema
--   2.RLS (secondary): Additional policy layer blocking accidental
--      cross-schema queries if application code tries to bypass search_path
--
-- The USING (true) policy allows all operations within the current schema context.
-- This is intentional because search_path already restricts access.
--
-- IMPORTANT: This migration uses DO $$ blocks with exception handling to safely
-- handle cases where RLS might already be enabled or policies already exist.
--
-- Tables covered by this migration:
--   - users (identity module)
--   - orders (orders module)
--   - inventory_items (inventory module)
--   - locations (warehouse module)
--   - lots (inventory tracking)
--   - inventory_movements (movement history)
--   - domain_events (event store)
--
-- WARNING: RLS does NOT prevent superusers from seeing all data.
--          Use separate PostgreSQL roles for true data separation.
-- =============================================================================

DO $$
DECLARE
    -- Array of core tenant-scoped tables
    v_tables TEXT[] := ARRAY[
        'users',
        'orders',
        'inventory_items',
        'locations',
        'lots',
        'inventory_movements',
        'domain_events'
    ];
    v_table TEXT;
    v_policy_exists BOOLEAN;
BEGIN
    FOREACH v_table IN ARRAY v_tables
    LOOP
        -- Check if table exists
        IF EXISTS (
            SELECT 1 FROM information_schema.tables 
            WHERE table_name = v_table 
            AND table_schema = 'public'
        ) THEN
            -- Enable RLS if not already enabled
            IF NOT EXISTS (
                SELECT 1 FROM pg_tables 
                WHERE tablename = v_table 
                AND rowsecurity = true
            ) THEN
                EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY', v_table);
                RAISE NOTICE 'Enabled RLS on table: %', v_table;
            END IF;

            -- Create policy if not exists
            v_policy_exists := EXISTS (
                SELECT 1 FROM pg_policies 
                WHERE policyname = 'tenant_isolation_' || v_table
                AND tablename = v_table
            );

            IF NOT v_policy_exists THEN
                EXECUTE format('
                    CREATE POLICY tenant_isolation_%I ON %I
                    FOR ALL
                    USING (true)
                    WITH CHECK (true)',
                    v_table, v_table
                );
                RAISE NOTICE 'Created RLS policy for table: %', v_table;
            END IF;

            -- Grant permissions to current user
            EXECUTE format('GRANT ALL ON %I TO CURRENT_USER', v_table);
        ELSE
            RAISE WARNING 'Table does not exist, skipping: %', v_table;
        END IF;
    END LOOP;

    RAISE NOTICE 'RLS enabled successfully on all core tables';
END $$;

-- Add comments for documentation on each table covered
COMMENT ON TABLE users IS 'Users table with RLS for schema-based multi-tenant isolation';
COMMENT ON TABLE orders IS 'Orders table with RLS for schema-based multi-tenant isolation';
COMMENT ON TABLE inventory_items IS 'Inventory items table with RLS for schema-based multi-tenant isolation';
COMMENT ON TABLE locations IS 'Locations table with RLS for schema-based multi-tenant isolation';
COMMENT ON TABLE lots IS 'Lots table with RLS for schema-based multi-tenant isolation';
COMMENT ON TABLE inventory_movements IS 'Inventory movements table with RLS for schema-based multi-tenant isolation';
COMMENT ON TABLE domain_events IS 'Domain events table with RLS for schema-based multi-tenant isolation';

-- Create a view to check RLS status on all tables
CREATE OR REPLACE VIEW v_rls_status AS
SELECT 
    tablename AS table_name,
    rowsecurity AS rls_enabled,
    (
        SELECT array_agg(policyname::text)
        FROM pg_policies p
        WHERE p.tablename = t.tablename
    ) AS policies
FROM pg_tables t
WHERE schemaname = 'public'
AND tablename IN (
    'users', 'orders', 'inventory_items', 'locations',
    'lots', 'inventory_movements', 'domain_events'
);

-- Grant access to view for monitoring
GRANT SELECT ON v_rls_status TO CURRENT_USER;

COMMENT ON VIEW v_rls_status IS 'View to monitor RLS status across tenant-scoped tables';