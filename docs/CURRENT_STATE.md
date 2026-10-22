# CURRENT_STATE — Estado Verificado del WMS

> **Última verificación: 2026-10-21** (rama `dev` @ `813be57`, merge de la feature `tenant-isolation-and-schema`).
> Fuente primaria: análisis #611 (`sdd/project-recovery-analysis/explore`, 2026-10-21) **re-verificado** con comandos
> ejecutados el mismo día. Toda afirmación lleva evidencia: ruta de archivo, comando o commit.
> Lo que no pudo verificarse se marca **"no verificado"** en vez de afirmarse.
>
> Este documento es la **fuente única de verdad del estado real**. Los demás docs históricos
> (`ANALISIS_DISENO.md`, `docs/ROADMAP.md`, `docs/AUDIT_2025.md`, `docs/PLAN_*.md`) están
> superados por este.

---

## 1. Stack verificado

| Capa | Tecnología | Evidencia |
|------|-----------|-----------|
| Runtime | Java 21 (Temurin 21.0.11) | `pom.xml` (maven.compiler), banner de arranque verificado en vivo |
| Framework | Spring Boot 3.4.1 | Banner `:: Spring Boot :: (v3.4.1)` en `docker logs wms-backend`, 2026-10-21 |
| Build | Maven Wrapper (`./mvnw`), sin JaCoCo | `grep -c jacoco pom.xml` → **0** (2026-10-21) |
| Base de datos | PostgreSQL 16 (`postgres:16-alpine`), Flyway 10.x | `docker-compose.yml`; log `Database: jdbc:postgresql://localhost:5432/wms_db (PostgreSQL 16.13)` |
| Multi-tenant | Aislamiento por schema vía wrapper de DataSource (S1/S2), fail-closed | `src/main/java/com/juanbenevento/wms/shared/infrastructure/tenant/TenantAwareDataSource.java` |
| Frontend | Angular (`wms-frontend/`), servido por nginx en Docker | `wms-frontend/Dockerfile` |
| Tests | JUnit 5 + Testcontainers + Mockito | `src/test/` (38 archivos `.java`, 2026-10-21) |
| Orquestación local | `docker compose` (postgres + backend + frontend) | 3 servicios **healthy** verificados 2026-10-21 |

---

## 2. Módulos y madurez real

Leyenda: **COMPLETE** = persiste y tiene evidencia de ejecución · **PARTIAL** = funciona con
limitaciones documentadas · **SCAFFOLD** = código existe pero no persiste / estado en memoria ·
**BROKEN** = no funciona · **no verificado** = no se ejecutó hoy.

**Paquetes top-level (verificado 2026-10-21, `Get-ChildItem -Directory src\main\java\com\juanbenevento\wms`)**:
`audit`, `catalog`, `identity`, `inventory`, `monitoring`, `orders`, `shared`, `warehouse`.
`picking`, `dashboard` y `super-admin` **no** son paquetes top-level (viven dentro de `inventory`, `shared` e `identity` respectivamente).

| Módulo (`src/main/java/com/juanbenevento/wms/<x>`) | Madurez | Evidencia |
|----------------------------------------------------|---------|-----------|
| `identity` (usuarios/auth) | COMPLETE | Login en vivo 2026-10-21: `POST /api/v1/auth/login` → 200 + JWT; usuarios reales en BD: psql → `superadmin/SUPER_ADMIN`; seed idempotente `shared/infrastructure/config/DataInitializer.java` |
| `catalog` (productos) | COMPLETE | Products persiste en JPA (tabla `products` creada por `db/migration/V2__application_tables.sql`, generada del metamodelo Hibernate); endpoint consultado en E2E `TenantFailClosedEndToEndTest` |
| `orders` (órdenes) | PARTIAL | Flujos de estado probados (`OrderInventoryIntegrationTest`, `PickingWorkflowIntegrationTest` en suite verde 2026-10-21); **evento `OrderCancelled`**: test `EventPersistenceIntegrationTest:192` sigue `@Disabled` |
| `inventory` (stock + lotes/movimientos) | PARTIAL | Persistencia de stock real: `InventoryPersistenceAdapter` (JPA, Spring Data) — suite verde. **PERO** lotes y movimientos NO persisten: `LotJpaAdapter` y `MovementJpaAdapter` son `HashMap` pese al nombre "Jpa" (análisis #611 §5, verificado en código) → **feature B** |
| `picking` (subdomain dentro de `inventory`) | PARTIAL | `inventory/application/service/PickingService.java` — lógica completa pero sesiones en `ConcurrentHashMap` → estado **en memoria**, se pierde al reiniciar (análisis #611 §5); controller `inventory/infrastructure/in/rest/PickingController.java` |
| `monitoring` (cadena de frío) | SCAFFOLD | `monitoring/application/service/ColdChainMonitorService.java` usa `ConcurrentHashMap` (datos en memoria, no multi-tenant-safe); sin UI Angular para `/api/v1/monitoring/*` (análisis #611 §6) |
| `dashboard` (en `shared/api`) | PARTIAL | `shared/api/DashboardController.java` + visualizaciones KPI (commits `427bd59`, `87b1e0e`); **no verificado hoy** su cobertura de datos reales |
| `audit` | PARTIAL | Eventos vía `InventoryEventBridge` `@Async` con `ContextAwareTaskDecorator` → **eventualmente consistente** (análisis #611 §6) |
| `warehouse` | no verificado | Paquete existe; no se ejecutó hoy |
| `super-admin` (en `identity`: `SaaSController`) | no verificado | Onboarding de tenants vía `shared/infrastructure/tenant/TenantSchemaManager.java` — no verificado hoy |
| `shared/infrastructure/tenant` (aislamiento) | COMPLETE | **Cerrado por la feature A** (merge `813be57`, 2026-10-21): wrapper en cada checkout de conexión, fail-closed, health indicator honesto. Evidencia: suite 317/0/0/2 + `/actuator/health` → `tenantIsolation: UP (schema-based fail-closed)` verificado en vivo 2026-10-21 |
| Frontend Angular | PARTIAL | 133 archivos `.ts`, solo **2** `*.spec.ts` (comando: `Get-ChildItem -Recurse wms-frontend\src -Filter *.spec.ts` → 2); sin UI para cold-chain/lotes/idempotencia |
| E2E Playwright | PARTIAL | 7 specs en `e2e/` (4 API + 3 UI) pero `e2e/node_modules` **ausente** → no ejecutables sin `npm install` (verificado 2026-10-21) |

---

## 3. Estado de tests (2026-10-21)

**Último run verificable** — comando: `.\mvnw.cmd clean verify` (PowerShell; `-Dtest=` entrecomillado si se filtra), con el stack Docker corriendo:

```
Tests run: 317, Failures: 0, Errors: 0, Skipped: 2 — BUILD SUCCESS (2:20 min)
```

- **317 tests / 0 failures / 0 errors / 2 skipped.** Evolución: 307 (baseline #611) → 317 tras la feature A.
- **Skipped justificados (2)** — ambos `@Disabled` con motivo documentado:
  - `EventPersistenceIntegrationTest.java:192` — rediseño del flujo de órdenes pendiente.
  - `IdempotencyIntegrationTest.java:34` — "Test hangs" (luego de 30s Sureforce fuerza kill del JVM por hilo no-daemon).
  - Comando: `Get-ChildItem -Recurse src\test -Filter *.java | Select-String "@Disabled"` → exactamente esos 2 (2026-10-21).
- **Cobertura de código: NO EXISTE.** `grep -c jacoco pom.xml` → `0`. Cualquier porcentaje de cobertura que aparezca en otro documento es **infundado**.
- Guard Docker: `RequiresDocker` como meta-anotación registrada en los tests que lo necesitan (commit `2e5ba2f`) — sin Docker, esos tests se saltan en vez de fallar.

---

## 4. Despliegue y CI

| Artefacto | Estado | Evidencia (2026-10-21) |
|-----------|--------|------------------------|
| `docker-compose.yml` (dev) | **FUNCIONA** | `docker compose up -d` → postgres + backend + frontend **los 3 healthy**; Flyway aplicó V1+V2; `ddl-auto=validate` OK; fix en `c597cfd` |
| `docker-compose.prod.yml` | ROTO (parcial) | Referencia `./monitoring/prometheus.yml` **inexistente** (`Test-Path` → False); env vars Resilience4j aún mal formadas (`..._EXceptions` con mayúsculas mezcladas) — solo 2 typos corregidos en `c597cfd` |
| Credenciales reales | `superadmin` / `root123` | Seed de `DataInitializer.java`; login verificado en vivo. El `admin123` de los docs viejos es falso (el seed SQL que lo sostenía fue eliminado en `c597cfd`) |
| CI `maven.yml` | Parcial | Corre `./mvnw clean verify` solo en `push: [dev, main]` y `pull_request: [main]` → **NO ejecuta CI en PRs hacia `dev`** (`.github/workflows/maven.yml`) |
| CI `ci-cd.yml` | Manual-only | Tenía `on:` comentado = workflow inválido que fallaba en **cada** push; dejado en `workflow_dispatch` por `fdac9bd` |
| Kubernetes | **NO INSTALABLE** | `k8s/` tiene `Chart.yaml`/`values.yaml` pero **sin `templates/`** (`Test-Path k8s\templates` → False); imagen del deployment (`ghcr.io/juanbenvento/wms-backend`) ≠ imagen publicada por CI; ingress apunta a Service `wms-frontend` inexistente |
| Helm en README | Falso | README dice `helm install wms ./charts/wms`; `charts/` **no existe** (`Test-Path charts` → False) |

---

## 5. Riesgos abiertos (ordenados por impacto)

1. **Persistencia en memoria (lotes, movimientos, picking, cold-chain)** — datos se pierden al reiniciar y no son multi-tenant-safe. Camino: feature B. Evidencia: `inventory/infrastructure/out/persistence/LotJpaAdapter.java` y `MovementJpaAdapter.java` (HashMap pese al nombre "Jpa"), `inventory/application/service/PickingService.java` (ConcurrentHashMap), `monitoring/application/service/ColdChainMonitorService.java`.
2. **Sin medición de cobertura** — JaCoCo ausente del `pom.xml`; ningún % de cobertura es citable.
3. **k8s/prod no desplegable** — sin templates Helm, mismatch de imagen, ingress a Service inexistente, secrets placeholder `changeme` versionados en `k8s/values.yaml`.
4. **`docker-compose.prod.yml` roto** — archivo de monitoring inexistente + env vars Resilience4j mal formadas.
5. **CI hueco en PRs hacia `dev`** — `maven.yml` no dispara; los PRs a dev no se validan automáticamente.
6. **2 tests `@Disabled`** — idempotencia (hang) y evento de orden cancelada (rediseño pendiente).
7. **Frontend con 2 specs / 133 TS** y E2E Playwright sin `node_modules` — cobertura de UI casi nula.
8. **RLS: no implementado** — decisión S7 de la feature A: los archivos SQL se eliminaron; el health reporta `rls: not-implemented` en vez de inventar estado (`TenantIsolationHealthIndicator.java`).
9. **Docs históricos con afirmaciones falsas** — "v1.0 Completado", "~80% cobertura", "185+ tests": corregidos por esta feature (`docs/ROADMAP.md` TD5, `README.md` TD2, etc.).
10. **`src/main/resources/schema.sql`** — DDL de lots/inventory_movements que **no se usa en runtime** (Flyway es el dueño del esquema).

---

## 6. Cómo se verificó (comandos re-ejecutados)

```powershell
.\mvnw.cmd clean verify                                  # 317/0/0/2 BUILD SUCCESS (con stack Docker arriba)
docker compose ps                                         # 3 servicios healthy
# GET http://localhost:8080/actuator/health               → 200 {"status":"UP",...,"tenantIsolation":...}
# POST /api/v1/auth/login {"username":"superadmin","password":"root123"} → 200 + JWT
docker exec wms-postgres psql -U postgres -d wms_db -c "SELECT version, success FROM flyway_schema_history;"
grep -c jacoco pom.xml                                    # 0
Get-ChildItem -Recurse src\test -Filter *.java | Select-String "@Disabled"   # 2
Test-Path charts, k8s\templates, monitoring\prometheus.yml, e2e\node_modules  # todos False
```

- Análisis base: Engram #611 (`sdd/project-recovery-analysis/explore`).
- Estado git: `dev` @ `813be57` (2026-10-21), PR #44 mergeado, issue #43 cerrado.
