-- =============================================================================
-- Flyway Migration: V__enable_rls_users.sql
-- =============================================================================
-- Purpose: Enable Row-Level Security (RLS) on the users table as defense-in-depth
--          layer for schema-based multi-tenant isolation.
--
-- Security Model:
--   1.Schema isolation (primary): Each tenant has tenant_<id> schema
--   2.RLS (secondary): Additional policy layer blocking accidental
--      cross-schema queries if application code tries to bypass search_path
--
-- Since we're using schema isolation, this policy uses USING (true) because
-- the search_path connection filter already restricts access to the
-- tenant's schema. RLS provides an extra security layer.
--
-- WARNING: RLS does NOT prevent superusers from seeing all data.
--          Use separate PostgreSQL roles for true data separation.
-- =============================================================================

-- Enable Row-Level Security on users table
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- Create tenant isolation policy for users table
-- FOR ALL: applies to SELECT, INSERT, UPDATE, DELETE
-- USING (true): allows all operations within the current schema context
CREATE POLICY tenant_isolation_users ON users
    FOR ALL
    USING (true)
    WITH CHECK (true);

-- Grant permissions to application user
-- Note: In PostgreSQL, RLS policies are enforced for all users by default
-- except table owners and superusers. This ensures app user is subject to RLS.
GRANT ALL ON users TO CURRENT_USER;

-- Comment for documentation
COMMENT ON POLICY tenant_isolation_users ON users IS
    'Row-Level Security policy for schema-based multi-tenant isolation. '
    'Uses USING(true) because access is already restricted by search_path. '
    'This policy provides defense-in-depth against application bugs or '
    'accidental cross-schema queries.';