# Proposal: Schema-Based Multi-Tenant Isolation for Enterprise SaaS

## Intent

Implementar aislamiento multi-tenant a nivel de base de datos mediante esquemas PostgreSQL separados por tenant para cumplir requisitos de auditoría empresarial SOC2/ISO27001, eliminando los riesgos del soft multi-tenancy actual donde el filtro Hibernate puede ser neutralizado con SQL nativo y un DBA puede acceder a todos los datos de todos los tenants.

## Scope

### In Scope
- Crear un esquema PostgreSQL dedicado por cada tenant (tenant_{id})
- Implementar conexión dinámica que establece el schema en search_path por sesión
- Agregar Row-Level Security (RLS) como segunda capa de defensa
- Migrar datos existentes de tenant_id column a nueva estructura por esquema
- Configurar políticas RLS automáticas en creación de tablas

### Out of Scope
- Multi-database (múltiples bases de datos) - complejo para >50 tenants
- Sharding horizontal - fuera del alcance actual
- Implementación de tenant replication - diferido

## Capabilities

### New Capabilities
- `multi-tenant-schema-isolation`: Aislamiento completo a nivel de schema PostgreSQL
- `dynamic-tenant-schema-connection`: Establecimiento dinámico de search_path por sesión
- `row-level-security-policy`: Políticas RLS automáticas por tabla

### Modified Capabilities
- `tenant-context-management`: Extender para gestionar esquema en lugar de solo tenant_id
- `tenant-data-access`: Cambiar de filter-based a schema-based queries

## Approach

**Enfoque seleccionado**: PostgreSQL search_path por sesión (Approach B)

**Justificación**: 
- El aislamiento a nivel de schema ya previene acceso cross-tenant a nivel de DB
- RLS añade capa adicional de defensa-in-depth para queries dentro del mismo schema
- Simpler que Approach A (dedicated connections) - una única connection pool
- Cada sesión SQL ejecuta SET search_path TO tenant_{id} al inicio
- Queries nativos y bulk updates automáticamente scoped al schema del tenant

**Arquitectura**:
```
Request → TenantContext.getTenantId() → 
JDBC/Session Init: SET search_path TO tenant_{tenantId} →
RLS Policy: (if enabled) WHERE current_setting('app.tenant_id') = table.tenant_id →
Query execution
```

| Riesgo | Mitigación |
|--------|-----------|
| Filter bypass (SQL nativo) | Queries ejecución scoped al schema del tenant via search_path |
| DBA acceso a todos datos | Esquema propio por tenant - solo acceso al schema asignado |
| Soft multi-tenancy gaps | RLS como capa adicional + Schema isolation |

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `TenantContext.java` | Modified | Agregar gestión de schema name |
| `AuditableEntity.java` | Modified | Remover tenant_id column (migrar a schema) |
| `TenantFilterAspect.java` | Modified | Reemplazar filter con schema setup |
| `TenantEntityListener.java` | Modified | Schema-based isolation |
| `application.properties` | Modified | Dynamic schema config |
| `DataSource configuration` | New | Session-init search_path setup |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Migration data loss | Medium | Rollback script, backup pre-migración |
| Performance degradation | Low | Cache search_path per session |
| RLS conflicts with existing queries | Medium | Testing exhaustivo pre-producción |
| Schema creation race conditions | Low | Distributed lock / sequence |

## Rollback Plan

1. Revertir aplicación a versión anterior con soft multi-tenancy
2. Restaurar backup de base de datos pre-migración
3. Recorrer script: migrar cada schema back a tenant_id column
4. Deshabilitar RLS en todas las tablas
5. Tiempo estimado: 4-6 horas con downtime

## Dependencies

- PostgreSQL 14+ con RLS support
- Hibernate 6.x para custom SQL function support
- Liquibase para migration scripts

## Success Criteria

- [ ] Cada tenant tiene su propio schema en PostgreSQL
- [ ] Queries cross-tenant fallen con error (aislamiento verificado)
- [ ] RLS activo en todas las tablas de tenant
- [ ] Migración completada sin pérdida de datos
- [ ] Audit test Passing: DBA externo no puede ver datos de otro tenant
- [ ] Performance: <5% degradación vs. baseline