# Deployment Guide — Schema-Based Multi-Tenant Isolation

## Overview

This document describes how to deploy and manage the schema-based multi-tenant isolation feature in production environments.

---

## Prerequisites

- PostgreSQL 14+ with schema support
- WMS application version supporting `wms.tenant.isolation.enabled` flag
- Database admin access for schema creation
- Backup of existing database

---

## Deployment Checklist

### Pre-Deployment

1. **Database Backup**
   ```bash
   # Full database backup before migration
   pg_dump -U postgres -Fc wms_db > wms_db_backup_$(date +%Y%m%d).dump
   ```

2. **Verify Current State**
   - Confirm no active migrations pending
   - Verify `tenant_id` column exists in core tables
   - Record current tenant count

3. **Staging Validation**
   - Deploy to staging with `wms.tenant.isolation.enabled=false`
   - Run integration tests
   - Verify feature flag can be toggled

---

## Deployment Steps

### Step 1: Enable Schema Isolation (Gradual Rollout)

The feature uses a feature flag for gradual rollout:

```properties
# Para habilitar gradualmente:
wms.tenant.isolation.enabled=true
wms.tenant.isolation.rls.enabled=false  # Inicialmente solo search_path
```

### Step 2: Environment Configuration

| Environment | `wms.tenant.isolation.enabled` | `wms.tenant.isolation.rls.enabled` |
|-------------|-------------------------------|-----------------------------------|
| Development | `false` | `false` |
| Staging     | `true` | `false` |
| Production | `true` | `false` (initial) / `true` (after validation) |

### Step 3: Initial Deployment

```bash
# 1. Stop application
./bin/stop-wms.sh

# 2. Run Flyway migrations (create tenant infrastructure)
./mvnw flyway:migrate -Dflyway.configFiles=prod

# 3. Create schemas for existing tenants
# Ejecutar via API REST o manualmente:
curl -X POST http://localhost:8080/api/admin/tenants/migrate \
  -H "Authorization: Bearer <admin-token>"

# 4. Start application con flag
./bin/start-wms.sh --spring.profiles.active=prod

# 5. Verify isolation
curl -X GET http://localhost:8080/api/admin/tenants/verify-isolation \
  -H "Authorization: Bearer <admin-token>"
```

### Step 4: Enable RLS (After Validation)

After confirming schema isolation works correctly:

```properties
wms.tenant.isolation.enabled=true
wms.tenant.isolation.rls.enabled=true  # Habilitar después de validación
```

---

## Monitoring & Alerting

### Key Metrics

| Metric | Description | Alert Threshold |
|--------|-------------|-----------------|
| `tenant.schema.switches` | Schema changes per minute | > 100/min |
| `tenant.connection.errors` | Connection errors | > 5/min |
| `tenant.query.latency` | Query latency by tenant | p95 > 500ms |
| `rls.policy.violations` | RLS policy violations | Any |

### Log Patterns

```properties
# Buscar en logs
grep "search_path" application.log
grep "tenant_" application.log | grep -i error
grep "RLS" application.log
```

### Health Checks

```bash
# Verificar estado de aislamiento
curl http://localhost:8080/api/admin/tenants/health

# Verificar métricas
curl http://localhost:8080/actuator/metrics/tenant.schema.switches
```

---

## Rollback Procedure

### If Issues Detected

1. **Disable Feature Flag**
   ```properties
   # En application-prod.properties o variable de entorno
   wms.tenant.isolation.enabled=false
   wms.tenant.isolation.rls.enabled=false
   ```

2. **Restore Application**
   ```bash
   # Restart con flags originales
   ./bin/stop-wms.sh
   ./bin/start-wms.sh --spring.profiles.active=prod
   ```

3. **Full Rollback (if needed)**
   ```bash
   # Si se requiere rollback completo:
   # 1. Detener aplicación
   ./bin/stop-wms.sh
   
   # 2. Restore database
   pg_restore -U postgres -d wms_db wms_db_backup_YYYYMMDD.dump
   
   # 3. Iniciar aplicación con versión anterior
   ./bin/start-wms-old.sh --spring.profiles.active=prod
   ```

### Emergency Rollback Checklist

- [ ] Backup current database state
- [ ] Set `wms.tenant.isolation.enabled=false`
- [ ] Restart all application instances
- [ ] Verify legacy tenant_id filter works
- [ ] Notify team of rollback
- [ ] Document incident

---

## Troubleshooting

### Common Issues

| Issue | Symptom | Solution |
|-------|--------|----------|
| Schema not found | `ERROR: schema "tenant_xxx" does not exist` | Run tenant migration first |
| Permission denied | `ERROR: permission denied for schema tenant_xxx` | Grant schema usage to app user |
| search_path not set | Queries hit public schema | Check `SearchPathConnectionInterceptor` |
| RLS blocking queries | `ERROR: query would violate row policy` | Review RLS policies or disable temporarily |

### Diagnostic Commands

```sql
-- Verificar esquemas existentes
SELECT schema_name FROM information_schema.schemata 
WHERE schema_name LIKE 'tenant_%';

-- Verificar search_path actual
SHOW search_path;

-- Verificar RLS enabled
SELECT relname, relrowsecurity 
FROM pg_class 
WHERE relname IN ('users', 'orders', 'inventory_items');
```

---

## Security Considerations

### Audit Requirements

- All schema switches must be logged
- RLS policy violations trigger alerts
- Tenant data access is audited

### Access Control

- Database superuser never accesses tenant schemas
- Application uses dedicated app_user role
- Each tenant schema has limited permissions

---

## Version Compatibility

| Version | Feature Support |
|---------|-----------------|
| 1.0.x   | Feature flag available, disabled by default |
| 1.1.x   | Schema isolation GA, RLS optional |
| 1.2.x   | RLS enabled by default in prod |

---

## Contact

- **On-Call**: DevOps Team
- **Escalation**: Tech Lead → Architect
- **Documentation**: /docs/isolation/