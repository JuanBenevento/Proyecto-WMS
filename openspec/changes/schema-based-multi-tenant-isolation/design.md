# Technical Design — Schema-Based Multi-Tenant Isolation

## Technical Approach

This design documents the implementation of multi-tenant isolation using PostgreSQL schemas. Each tenant gets its own schema (`tenant_{tenantId}`), and the database session's `search_path` is set per-request to route all queries to the tenant's schema. Row-Level Security (RLS) provides a defense-in-depth second layer.

This approach replaces the current Hibernate filter-based soft multi-tenancy with hard database-level isolation, preventing SQL native query bypass and DBA access to cross-tenant data.

---

## Architecture Decisions

### ADR-1: Connection Approach — search_path vs Dedicated Connections

| Aspect | search_path (Chosen) | Dedicated Connections |
|-------|---------------------|----------------------|
| Connection pool | Single pool, shared | One pool per tenant |
| Schema routing | `SET search_path` per session | Connection bound to schema |
| Complexity | Low | High |
| Connection overhead | Minimal | N × pools |

**Choice**: search_path per session  
**Rationale**: Simpler architecture with a single connection pool. PostgreSQL's `search_path` is session-scoped, automatically scoping ALL queries (including native SQL) to the tenant schema without code changes. The current codebase already has TenantContext via ThreadLocal, making integration straightforward.

---

### ADR-2: Schema Creation Strategy — Manual vs Auto-on-Tenant-Create

| Aspect | Manual | Auto-on-create |
|--------|--------|----------------|
| Control | Explicit, DBA-driven | Automatic |
| Error handling | Manual | Exception handling needed |
| Rollback | Simple DROP | Requires cleanup script |

**Choice**: Manual + helper utility  
**Rationale**: Tenant schema creation is a privileged operation that should be explicitly triggered during tenant provisioning by the system (not during HTTP requests). A `TenantSchemaManager` service provides the `createSchema(tenantId)` method to be called from the tenant creation workflow. This prevents accidental schema proliferation and makes migrations explicit.

---

### ADR-3: RLS Strategy — Enabled vs Optional

| Aspect | RLS Enabled | RLS Optional |
|--------|-------------|--------------|
| Security | Defense-in-depth | Single layer |
| Query overhead | Slight (policy check) | None |
| Debugging | Harder ( veiled) | Simpler |

**Choice**: RLS enabled as second layer  
**Rationale**: Schema isolation alone is sufficient for 99% of cases, but RLS provides defense-in-depth against misconfiguration and accidental cross-schema access within the same connection pool. Creates audit compliance for SOC2/ISO27001 requirements.

---

## Component Architecture

```
Request Flow
============

HTTP Request
     │
     ▼
JwtAuthenticationFilter ──► TenantContext.set()
     │
     ▼
SearchPathConnectionInterceptor (JDBC)
     │
    SET search_path TO tenant_abc123
     │
     ▼
Repository / Service ──► Queries scoped to
     │           tenant_abc123 schema
     ▼
Response
```

### New Components

| Component | Responsibility | Location |
|-----------|---------------|----------|
| `TenantSchemaManager` | Creates/drops tenant PostgreSQL schemas | `src/main/java/.../shared/infrastructure/tenant/TenantSchemaManager.java` |
| `SearchPathConnectionInterceptor` | Sets `search_path` on JDBC connection | `src/main/java/.../shared/infrastructure/tenant/SearchPathConnectionInterceptor.java` |
| `TenantContext` (extend) | Add `getSchemaName()` | Existing — modify |
| `AuditableEntity` (modify) | Remove `tenantId` column, tenant filter | Existing — modify |
| `TenantFilterAspect` (modify) | Replace with schema setup | Existing — modify |
| `TenantEntityListener` (modify) | Schema-aware entity handling | Existing — modify |
| `V__schema_creation.sql` | Initial schema migrations | `src/main/resources/db/migration/` |

---

## Database Design

### Schema Creation DDL

```sql
-- Create tenant schema (authorized by current db user)
CREATE SCHEMA AUTHORIZATION current_user;

-- Grant schema usage to application role
GRANT USAGE ON SCHEMA tenant_abc123 TO app_user;

-- Grant table permissions within schema
GRANT ALL ON TABLES IN SCHEMA tenant_abc123 TO app_user;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA tenant_abc123
GRANT ALL ON TABLES TO app_user;
```

### RLS Policies

```sql
-- Enable RLS on tenant tables
ALTER TABLE tenant_abc123.inventory_item ENABLE ROW LEVEL SECURITY;

-- Policy: all operations scoped to current schema
CREATE POLICY tenant_isolation_policy ON tenant_abc123.inventory_item
FOR ALL
USING (true);  -- Already scoped by search_path
```

---

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `src/main/java/.../shared/infrastructure/tenant/TenantSchemaManager.java` | Create | Service to create/drop tenant schemas |
| `src/main/java/.../shared/infrastructure/tenant/SearchPathConnectionInterceptor.java` | Create | JDBC interceptor to set search_path |
| `src/main/java/.../shared/infrastructure/tenant/TenantContext.java` | Modify | Add `getSchemaName()` returning `tenant_{id}` |
| `src/main/java/.../shared/infrastructure/tenant/TenantFilterAspect.java` | Modify | Replace filter with schema validation |
| `src/main/java/.../shared/infrastructure/adapter/out/persistence/AuditableEntity.java` | Modify | Remove `@Filter`, optional tenant_id column for shared tables |
| `src/main/java/.../shared/infrastructure/adapter/out/persistence/TenantEntityListener.java` | Modify | Adapt to schema-first pattern |
| `src/main/resources/db/migration/V1__initial_tenant_structure.sql` | Create | Flyway migration for schema setup |
| `src/test/java/.../MultiTenantSchemaIsolationTest.java` | Create | Integration test for isolation |

---

## Data Flow

```
TenantContext.getTenantId() → "abc123"
         │
         ▼
SearchPathConnectionInterceptor
         │
    SET search_path TO tenant_abc123
         │
         ▼
Hibernate generates:
  SELECT * FROM inventory_item   -- resolves to tenant_abc123.inventory_item
         │
         ▼
RLS (if enabled): additional WHERE clause applied by PostgreSQL
```

---

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | `TenantSchemaManager.createSchema()` | Mock JDBC, verify SQL |
| Unit | `TenantContext.getSchemaName()` | Assert `tenant_{id}` format |
| Integration | Isolation: query tenant A cannot see tenant B data | Testcontainers multi-schema |
| Integration | Native SQL bypass protection | Execute raw SQL, verify isolation |

---

## Migration / Rollout

### Phased Implementation Order

1. **Phase 1**: Create `TenantSchemaManager` + Flyway migration for initial schema infrastructure
2. **Phase 2**: Implement `SearchPathConnectionInterceptor` with JDBC interceptor
3. **Phase 3**: Modify `TenantContext` + update `TenantFilterAspect` to schema validation
4. **Phase 4**: Add RLS policies to key tables (inventory, movement, lot)
5. **Phase 5**: Run data migration (create schemas for existing tenants, migrate data)
6. **Phase 6**: Integration tests + staging validation
7. **Phase 7**: Production rollout with feature flag

### Rollout Checklist

- [ ] Backup database pre-migration
- [ ] Test rollback script
- [ ] Enable RLS incrementally (staging first)
- [ ] Validate performance: <5% degradation

---

## Open Questions

- [ ] **Should `tenantId` column be retained only for shared tables?** Yes — retain for cross-tenant tables (lookups, constants). Schema-scoped tables drop the column.
- [ ] **How to handle migrations across schemas?** Flyway runs in public schema only. Tenant-specific migrations require `TenantSchemaManager.executeMigration(schema, sql)`.
- [ ] **RLS performance impact expected?** Minimal (sub-millisecond). Can disable per-table if issues arise.