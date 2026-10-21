# Feature: Tenant Isolation & Schema (Camino A)

- **Objetivo**: Que el aislamiento multi-tenant funcione de verdad (fail-closed, ligado a la conexión real de cada query) y que la aplicación arranque con un esquema completo creado por Flyway.
- **Problema**: `SET search_path` se ejecuta sobre una conexión del pool que se libera antes de la query; PostgreSQL no valida schemas inexistentes en `search_path`; fallback silencioso a `public` → fuga cross-tenant sin señal. Además, Flyway no crea las tablas de la aplicación → `docker-compose.yml` (`ddl-auto=validate` + Flyway on) no arranca.
- **Por qué**: Producto SaaS multi-tenant con aislamiento roto no es un producto; es una lista de incidentes futuros. Prioridad máxima según análisis `sdd/project-recovery-analysis/explore` (2026-10-21).
- **Alcance**: Backend únicamente — `TenantConnectionFilter`, `SearchPathConnectionInterceptor`, health indicator, flags de configuración, migraciones Flyway, guard de Docker en tests, test de aislamiento.
- **Fuera de alcance**: Persistencia de lotes/movimientos (feature `real-persistence`), CI/Helm/secrets (feature `delivery-hardening`), documentación (feature `honest-project-documentation`).
- **Restricciones**: GitFlow (`fix/tenant-isolation-search-path` desde `dev`), convención de commits, test-first cuando exista test determinista, sin push sin decisión explícita del usuario.

## Specs

### S1 — search_path aplicado a la conexión real de la query

**Cita del análisis (verbatim)**: `TenantConnectionFilter → jdbcTemplate.execute("SET search_path TO tenant_x, public") toma una conexión, la suelta, y el request usa otras.`

- **Criterio de aceptación**: Dada una request autenticada con contexto de tenant X cuyo schema existe, cuando se ejecuta cualquier query del request, entonces la conexión que ejecuta esa query tiene `search_path` que resuelve `tenant_X` primero. Requests concurrentes de tenants distintos no contaminan el `search_path` entre sí. Al finalizar la request, la conexión vuelve a un estado limpio antes de volver al pool.
- **Checks**: `TenantIsolationIntegrationTest` (reactivada) con dos tenants concurrently; test nuevo que lance dos requests interleaved y verifique que cada una lee solo su schema.

### S2 — Fail-closed: sin fallback silencioso a `public`

**Cita del análisis (verbatim)**: `si el schema del tenant no existe, las tablas resuelven contra public sin error. Fuga cross-tenant sin ninguna señal`

- **Criterio de aceptación**: Dada una request para un tenant cuyo schema NO existe, cuando intenta leer datos, entonces la respuesta es un error explícito (4xx/5xx con mensaje "tenant schema missing" o equivalente) y NINGÚN dato es servido desde `public`. El filtro NUNCA debe incluir `public` como fallback silencioso tras el schema del tenant inexistente.
- **Checks**: test nuevo: schema inexistente → excepción/error explícito, cero filas leídas.

### S3 — Flags de configuración con efecto real

**Cita del análisis (verbatim)**: `wms.tenant.isolation.enabled / .rls.enabled: flags inertes (nadie los consume)`

- **Criterio de aceptación**: `wms.tenant.isolation.enabled=false` desactiva el filtro de aislamiento de forma explícita y documentada (solo para entornos que lo justifiquen). `=true` (default) aplica S1+S2. El flag es leído por el componente que filtra, no decorativo.
- **Checks**: test de contexto de Spring: flag false → filtro no intercepta; flag true → intercepta.

### S4 — Health indicator que refleja el estado real

**Cita del análisis (verbatim)**: `TenantIsolationHealthIndicator lee System.getProperty → siempre false`

- **Criterio de aceptación**: El health indicator de aislamiento reporta DOWN/UNKNOWN cuando el aislamiento no puede garantizarse (filtro desactivado, modo degradado) y UP solo cuando S1+S2 están activos y verificados.
- **Checks**: test unitario del indicator con cada estado.

### S5 — SearchPathConnectionInterceptor: implementar o eliminar

**Cita del análisis (verbatim)**: `SearchPathConnectionInterceptor es placeholder sin implementar; su configureConnectionInitSql() nunca se invoca.`

- **Criterio de aceptación**: El interceptor queda implementado y registrado como la mecánica oficial de S1 (recommended: `ConnectionInitSql` / proxy de conexión por checkout de Hikari, o interceptor sobre la conexión real), O eliminado por completo si se resuelve S1 por otra vía. No puede quedar código placeholder invocado por nadie.
- **Checks**: grep de referencia cero al placeholder eliminado; si se implementa, test de la mecánica.

### S6 — Flyway crea todas las tablas de la aplicación

**Cita del análisis (verbatim)**: `V1__initial_tenant_structure.sql solo crea funciones + tenant_schemas; no crea las tablas de app` y `docker-compose.yml: ddl-auto=validate + Flyway on → arranque fallido`

- **Criterio de aceptación**: Con una base PostgreSQL limpia, `docker compose up` migra y la aplicación arranca con `ddl-auto=validate` sin errores de relación inexistente.
- **Checks**: `docker compose up` exitoso; endpoint de salud responde.

### S7 — Migración RLS con versión válida

**Cita del análisis (verbatim)**: `V__enable_rls_all_tables.sql tiene versión Flyway vacía (inválido)`

- **Criterio de aceptación**: La migración RLS tiene un prefijo de versión válido (`V5__...` o similar) y Flyway la aplica sin rechazar el estado de migración. Si RLS no forma parte del alcance real de aislamiento (schema-per-tenant ya aísla), la migración se elimina o se pospone con decisión explícita documentada en el Log.
- **Checks**: `flyway info` / arranque limpio sin errores de versión.

### S8 — Guard de Docker funcional en tests de integración

**Cita del análisis (verbatim)**: `RequiresDocker + RequiresDockerCondition (package-private) + RequiresDockerExtension (copia). Ninguno registrado con @ExtendWith → sin Docker los tests fallan en vez de saltarse`

- **Criterio de aceptación**: Los tests de integración que requieren Docker se SALTAN con mensaje claro cuando el daemon no está disponible, y CORREN cuando lo está. Un solo mecanismo de guard (sin copias duplicadas sin registrar).
- **Checks**: correr suite sin Docker → tests de integración skipped, no failed; con Docker → ejecutan.

### S9 — TenantIsolationIntegrationTest reactivada

**Cita del análisis (verbatim)**: `3 tests @Disabled: ... TenantIsolationIntegrationTest:111`

- **Criterio de aceptación**: El test de aislamiento está habilitado y pasa contra PostgreSQL real (Testcontainers), demostrando S1 y S2.
- **Checks**: `TenantIsolationIntegrationTest` sin `@Disabled`, en verde.

## Tasks

| ID | Spec | Ruta | Commit |
|----|------|------|--------|
| T1 | S6 | baseline | `pendiente` — ✅ COMPLETADO 2026-10-21: `.\mvnw.cmd clean verify` → **307 tests, 0 failures, 0 errors, 5 skipped, BUILD SUCCESS (2:03 min)**. Ver L6. |
| T2 | S8 | inline | ✅ `2c6f33c..` — Unificado: `@RequiresDocker` ahora meta-anota `@ExtendWith(RequiresDockerCondition.class)`; condición simplificada; `RequiresDockerExtension.java` eliminada. Test-first: `RequiresDockerGuardTest` RED→GREEN (2 tests). |
| T3 | S1, S2, S5 | inline | ✅ — `TenantAwareDataSource` (search_path en checkout de la conexión real, fail-closed con `TenantSchemaMissingException`, whitelist `^[a-z][a-z0-9_]{0,62}$` contra inyección de identificadores). `TenantDataSourceConfiguration` (BeanPostProcessor envuelve HikariDataSource; flag `wms.tenant.isolation.enabled` default true). Eliminados: `TenantConnectionFilter` + test (mecanismo roto) y `SearchPathConnectionInterceptor` (placeholder — S5 resuelto por eliminación). Test-first: `TenantAwareDataSourceIntegrationTest` RED (3F+1E) → GREEN (7/7). Suite full: 309/0/0/5-skipped BUILD SUCCESS. |
| T4 | S3, S4 | inline | (pendiente) — Cablear flags `wms.tenant.isolation.enabled` / `.rls.enabled` y reescribir `TenantIsolationHealthIndicator` |
| T5 | S6, S7 | inline | (pendiente) — Corregir migraciones Flyway: tablas de app completas, versión RLS válida o remoción decidida |
| T6 | S9, S1, S2 | inline | (pendiente) — Reactivar `TenantIsolationIntegrationTest` + test fail-closed nuevo; RED→GREEN→REFACTOR |
| T7 | S6 | verify | (pendiente) — `docker compose up` completo: arranque con `ddl-auto=validate` + health endpoint |
| T8 | — | verify | (pendiente) — Suite completa `.\mvnw.cmd verify` con Docker: sin failures, sin errors; revisar que los únicos skipped sean los justificados |

## Log

- **L1** (2026-10-21, solicitud original del usuario, verbatim): `Nesecito que hagamos ODD. Pero como primer paso quiero que analisemos por completo de que va el proyecto, como funciona cada parte para poder determinar como retomar su desarrollo y que camino vamos a tomar para continuar con el mismo`
- **L2** (2026-10-21, decisiones del usuario, verbatim): `1. ODD es organic driven development (propuesto por alaniscaglia en su ecositema gentle-ai). 2. Si tomemos el camino recomendado. 3. Si reemplaza, y si es posible genera una documentacion completa y fehaciente de todo el proyecto.`
- **L3** (2026-10-21, evidencia): Análisis completo verificado contra código — obs Engram #611 (`sdd/project-recovery-analysis/explore`). Todos los hallazgos citados en las Specs salen de ese análisis; fueron verificados por lectura de código, no asumidos.
- **L4** (2026-10-21, entorno): Docker Desktop APAGADO en la máquina del usuario (2026-10-21). Java 21 Temurin disponible. gentle-ai binario en 1.18.4 (sin comando `review`; upgrade a 4.2.0 no se aplicó). T1 queda bloqueado hasta que el usuario levante Docker.
- **L5** (2026-10-21, flujo): ODD adoptado (Engram `wms/workflow-odd`). SDD retirado en gentle-ai ≥4.0.0. RDD no disponible con el binario actual.
- **L6** (2026-10-21, T1 baseline): Docker 29.1.3 arriba. Rama `fix/tenant-isolation-search-path` creada desde `dev` @ 9193c3a. El working tree de `dev` **no compilaba**: el WIP sin commitear de `IdempotencyIntegrationTest.java` había borrado el `import org.junit.jupiter.api.Disabled` dejando la anotación — restaurado el import (conservando la intención del WIP: motivo "Test hangs" y sin properties JWT). Resultado verify: **Tests run: 307, Failures: 0, Errors: 0, Skipped: 5 — BUILD SUCCESS (2:03 min)**. Detalle de skipped: `EventPersistenceIntegrationTest#shouldPersistOrderCancelledEvent` (order workflow redesign), `TenantIsolationIntegrationTest$IsolationTests` ×3 (needs redesign for new schema isolation model — objetivo de S9), `IdempotencyIntegrationTest#shouldHandleConcurrentRequestsIdempotently` (test hangs). **Señal relevante**: Surefire reporta `going to kill self fork JVM. The exit has elapsed 30 seconds after System.exit(0)` — los tests pasan pero el JVM no sale limpio (hilo no-daemon vivo tras el shutdown; probable fuga de contexto Spring o hilo Testcontainers). Coincide con el síntoma "Test hangs" del @Disabled. Además: warning de `pom.xml` — `spring-boot-starter-aop` declarado dos veces (línea 120); limpiar en T2 o T5.
