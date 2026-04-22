# Delta Spec — Schema-Based Multi-Tenant Isolation

> **Change**: schema-based-multi-tenant-isolation
> **Status**: NEW CAPABILITY
> **Artifact Mode**: `openspec`
> **Domain**: tenant-isolation

---

## ADDED Requirements

### Requirement: Tenant Schema Isolation

The system MUST create a dedicated PostgreSQL schema for each tenant to achieve complete data isolation at the database level.

| Attribute | Type | Description |
|-----------|------|-------------|
| `schemaName` | String | Format: `tenant_{tenantId}` (lowercase, underscores) |
| `tenantId` | UUID | Foreign key to tenant registry |
| `createdAt` | Timestamp | Schema creation timestamp |
| `status` | SchemaStatus | ACTIVE, ARCHIVED, DELETED |

`SchemaStatus` enum: `ACTIVE` (fully operational), `ARCHIVED` (soft-deleted, retained for audit), `DELETED` (hard-deleted, unrecoverable)

#### Scenario: Tenant provisioning creates schema

- GIVEN a new tenant "Acme Corp" is being provisioned with tenantId="acme-001"
- WHEN the tenant provisioning process completes
- THEN the system MUST create a PostgreSQL schema named `tenant_acme_001`
- AND set the schema status to `ACTIVE`

#### Scenario: Tenant deletion archives schema

- GIVEN a tenant with schema `tenant_acme_001` is marked for deletion
- WHEN the soft-delete process executes
- THEN the system MUST set schema status to `ARCHIVED`
- AND revoke all access permissions from the schema
- BUT retain the schema for audit compliance

---

### Requirement: Dynamic search_path per Session

The system MUST dynamically set the PostgreSQL search_path to the tenant's schema at session initialization to ensure all queries execute within the tenant's isolated context.

#### Scenario: Session initialization sets tenant schema

- GIVEN a user from tenant "Acme Corp" (tenantId="acme-001") initiates a database session
- WHEN the session is established
- THEN the system MUST execute `SET search_path TO tenant_acme_001`
- AND all subsequent queries MUST execute within that schema context

#### Scenario: Session cleanup clears search_path

- GIVEN an active database session with search_path set to `tenant_acme_001`
- WHEN the session ends (connection close or timeout)
- THEN the system MUST execute `RESET search_path` to clear the tenant context

---

### Requirement: Row-Level Security as Defense Layer

The system MUST enable Row-Level Security (RLS) policies on tenant-specific tables as a second layer of defense to complement schema isolation.

#### Scenario: RLS policy enforces tenant scoping

- GIVEN RLS is enabled on the `inventory_items` table
- WHEN a query attempts to access inventory data
- THEN the RLS policy MUST automatically filter results to only records where `tenant_id` matches `current_setting('app.tenant_id')`

#### Scenario: RLS blocks superuser bypass

- GIVEN a PostgreSQL superuser attempts to query tenant data with RLS enabled
- WHEN the superuser executes `SELECT * FROM inventory_items`
- THEN RLS policies MUST still apply and filter to the current tenant context

---

### Requirement: Tenant Provisioning API Integration

The system MUST integrate schema creation into the tenant provisioning API to automatically create the tenant schema when a new tenant is registered.

#### Scenario: API creates tenant with schema

- GIVEN a `POST /api/tenants` request with tenant details
- WHEN the request is validated and processed
- THEN the system MUST create the tenant record in the tenant registry
- AND create the corresponding PostgreSQL schema in a single transaction

#### Scenario: API rolls back on schema creation failure

- GIVEN a `POST /api/tenants` request is being processed
- WHEN schema creation fails (e.g., disk space, permissions)
- THEN the system MUST roll back the tenant record creation
- AND return an appropriate error response

---

### Requirement: Migration from tenant_id Column

The system MUST provide a migration mechanism to move data from the legacy tenant_id column approach to the new schema-based isolation model, preserving backward compatibility.

#### Scenario: Data migration to tenant schema

- GIVEN existing records with tenant_id="acme-001" in the legacy schema
- WHEN the migration script is executed
- THEN the system MUST create the `tenant_acme_001` schema
- AND migrate all tenant records to the new schema
- AND keep tenant_id column for backward compatibility during transition

#### Scenario: Rollback migration restores tenant_id

- GIVEN a migration needs to be rolled back
- WHEN the rollback script is executed
- THEN the system MUST restore all data to the public schema
- AND restore the tenant_id column values

---

### Requirement: Tenant Deletion API

The system MUST provide a deletion API that soft-deletes (archives) the tenant schema with configurable retention period before hard deletion.

#### Scenario: API soft-deletes tenant schema

- GIVEN a `DELETE /api/tenants/{id}` request
- WHEN the request is processed
- THEN the system MUST archive the tenant schema (status=ARCHIVED)
- AND retain data for configured retention period
- AND return success response

#### Scenario: Hard delete after retention period

- GIVEN a soft-deleted tenant past its retention period
- WHEN the scheduled cleanup job executes
- THEN the system MUST permanently drop the tenant schema
- AND remove the tenant from the registry

---

### Requirement: Cross-Tenant Query Prevention

The system MUST prevent queries from accessing data outside the tenant's schema, even when using explicit schema prefixes.

#### Scenario: Blocked cross-schema query

- GIVEN a user connected to `tenant_acme_001`
- WHEN they attempt to query `tenant_other_001.inventory_items`
- THEN the system MUST deny the query with a permission error

---

### Requirement: Connection Pool Configuration

The system MUST configure the connection pool to handle tenant schema switching correctly without causing cross-tenant data leakage.

#### Scenario: Connection recycled for different tenant

- GIVEN a connection from the pool was previously used for tenant "Acme Corp"
- WHEN the connection is recycled to serve tenant "Beta Inc"
- THEN the system MUST reset search_path to the new tenant's schema

---

## Non-Functional Requirements

| NFR | Requirement |
|-----|-------------|
| **Isolation** | Tenant schemas MUST be completely isolated — no cross-tenant data access |
| **Performance** | Schema switching via search_path MUST add < 5ms latency |
| **Audit** | All schema operations MUST be logged with timestamp and actor |
| **RLS** | RLS policies MUST be enabled on all tenant-specific tables |
| **Migration** | Migration MUST complete with zero data loss |
| **Rollback** | Migration rollback MUST be possible within 30 minutes |

---

## Acceptance Criteria

- [ ] Each tenant has isolated PostgreSQL schema (tenant_{tenantId})
- [ ] search_path is set dynamically per session
- [ ] RLS policies block unauthorized cross-tenant access
- [ ] Native SQL queries cannot bypass schema isolation
- [ ] Migration script successfully migrates existing tenant data
- [ ] Tenant creation API automatically creates schema
- [ ] Tenant deletion soft-deletes schema with retention
- [ ] Connection pool properly resets tenant context
- [ ] Cross-schema queries are blocked at database level

---

## Scope Summary

| Capability | Type | Requirements | Scenarios |
|-----------|------|-------------|-----------|
| Tenant Schema Isolation | ADDED | 2 | 2 |
| Dynamic search_path per Session | ADDED | 2 | 2 |
| Row-Level Security as Defense | ADDED | 2 | 2 |
| Tenant Provisioning API | ADDED | 2 | 2 |
| Migration from tenant_id Column | ADDED | 2 | 2 |
| Tenant Deletion API | ADDED | 2 | 2 |
| Cross-Tenant Query Prevention | ADDED | 1 | 1 |
| Connection Pool Configuration | ADDED | 1 | 1 |

**Total**: 14 requirements, 14 scenarios