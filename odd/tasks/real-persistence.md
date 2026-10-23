# Feature: Real Persistence (lotes y movimientos)

> **Rama**: `feat/real-persistence` (desde `dev@d1abb09`).
> **Camino ODD**: feature B1 del A→B→C+D. Fuente de verdad de esta feature: este archivo.
> Espejo Engram: topic `odd/real-persistence/tasks`.

## Contexto (exploración 2026-10-23)

`LotJpaAdapter` y `MovementJpaAdapter` implementan los ports `LotRepository`/`MovementRepository`
pero son `HashMap` en memoria con javadoc que admite el engaño ("replace with JPA entity when
schema is finalized"). Ellos alimentan `RegisterReceiptService`, `RegisterIssueService`,
`RecordTemperatureService`, `QueryLotHistoryService`, `QueryExpiringLotsService` y las
estrategias FEFO/FIFO/LotControl/WeightCertification.

**Hechos verificados**:
- No hay tablas de lots/movements en Flyway (solo V1 tenant + V2 application; `stock_movement_logs`
  es un camino de auditoría distinto, en español, del flujo InventoryService — no confundir).
- `Lot` es dominio puro con factory y transiciones que devuelven nuevas instancias;
  `InventoryMovement` es `@Value` inmutable con factories + `withX`.
- `TemperatureRange` = VO inmin/max BigDecimal; enums: LotStatus(4), MovementType(5).
- Tests existentes de estrategias usan **Mockito mocks** del port → no rompen al reescribir adapters.
- Convención de integración: `@SpringBootTest` + `@ActiveProfiles("test")` + `@Testcontainers`
  + `PostgreSQLContainer("postgres:16-alpine")` + `@DynamicPropertySource`.
- **Gap sistémico (fuera de alcance de B1)**: `TenantSchemaManager.createSchema` crea schemas
  de tenant **vacíos** (solo CREATE SCHEMA + grants; V1 no copia tablas). Con aislamiento ON,
  solo el tenant SYSTEM usa `public`; un tenant real fallaría al consultar (fail-closed).
  TODAS las tablas de V2 tienen el mismo problema — es un gap de provisioning de schemas,
  no de esta feature. B1 persiste en el schema default (igual que V2) y el gap queda registrado.

## Specs

### S1 — Migración Flyway V3

- **CA**: Existe `V3__inventory_lots_and_movements.sql` con tablas `inventory_lots` e
  `inventory_movements`, índices para los queries del port y CHECK constraints para enums.
- **Checks**: Flyway aplica V3 en suite (flyway_schema_history incluye V3); `ddl-auto=validate`
  acepta el esquema; números de versión reales (V1, V2 existentes — NO "V5" del openspec viejo).

### S2 — Entidades JPA y mappers (dominio intocado)

- **CA**: `LotEntity` e `InventoryMovementEntity` en `infrastructure/out/persistence/`;
  `Lot`/`InventoryMovement` del dominio NO se modifican. `TemperatureRange` embebible;
  `metadata: Map<String,String>` persistida como JSON.
- **Checks**: mappers cobren roundtrip (dominio → entity → dominio) sin pérdida de campos.

### S3 — Adaptadores reales sobre Spring Data

- **CA**: `LotJpaAdapter` y `MovementJpaAdapter` delegan a repositorios JPA reales
  (`LotJpaRepository`, `InventoryMovementJpaRepository`) manteniendo la firma del port.
  Movimientos **append-only**: el repo JPA de movements no expone update/delete.
  Queries del port con semántica equivalente al HashMap anterior (orden por timestamp,
  expiring within, active for allocation, counts).
- **Checks**: cero `HashMap`/`Map<...> storage` en ambos adapters (grep).

### S4 — Tests de integración de persistencia (RED→GREEN)

- **CA**: Test de integración que prueba persistencia REAL, no el mapa: (a) la tabla existe
  (consulta a `information_schema`), (b) save vía port es visible por **JDBC directo**
  (no por el mismo adapter), (c) roundtrip de todos los campos (JSON metadata, rango de
  temperatura, enums), (d) orden por timestamp en `findByLotNumber`, (e) semántica
  append-only.
- **Checks**: test escrito primero falla por tabla/repo inexistentes (RED) y pasa tras S1-S3
  (GREEN). Convención de la casa: `@SpringBootTest` + Testcontainers + flyway ON + `ddl-auto=validate`.

### S5 — No-regresión de suite y aislamiento

- **CA**: `.\mvnw.cmd clean verify` completo en verde (317 previos + nuevos); los tests de
  aislamiento tenant no cambian de resultado; el gap de provisioning de schemas queda
  documentado (no silenciado).
- **Checks**: suite completa; ningún test tenant roto.

### S6 — Conciliación de docs y openspec

- **CA**: `CURRENT_STATE.md` actualiza madurez de lotes/movimientos (SCAFFOLD → COMPLETE con
  fecha); `README.md` tabla de madurez actualizada; `ROADMAP.md` aclara B1 (lotes/movimientos)
  vs B2 (sesiones de picking y cold-chain, aún en memoria); `ARCHITECTURE.md` corrige ADR-004
  (SchemaIsolationValidator NO fue eliminado — corrección de una afirmación falsa propia de la
  feature D); `openspec/changes/inventory-movement/tasks.md` marca 6.1-6.5 completadas y
  **corrige Phase 5** (los servicios Transfer/RecordTemperature/QueryLotHistory/QueryExpiring
  SÍ existen con nombres `*Service` — los "no existen" de TD7 fueron un error de búsqueda por
  nombre `*UseCase`; `InternalOperationsService.processInventoryAdjustment` cubre 5.6).
- **Checks**: ningún "no existe" falso en openspec; ADR-004 sin afirmación eliminada-inexistente.

## Tasks

| ID | Spec | Ruta | Commit |
|----|------|------|--------|
| TD1 | — | inline | (pendiente) — Rama + plan de feature |
| TD2 | S4 | inline | (pendiente) — RED: test de integración de persistencia (falla: sin tabla/repos) |
| TD3 | S1 | inline | (pendiente) — `V3__inventory_lots_and_movements.sql` (GREEN parcial) |
| TD4 | S2+S3 | inline | (pendiente) — Entidades, repos, mappers y adapters reales (GREEN) |
| TD5 | S5 | inline | (pendiente) — Suite completa + aislamiento |
| TD6 | S6 | inline | (pendiente) — Conciliación docs + openspec + corrección ADR-004 |
| TD7 | — | inline | (pendiente) — Engram mirror + decisión push/PR del usuario |

## Log

- **L1** (2026-10-23, exploración): Contexto verificado (arriba). **Decisión de alcance B1**:
  solo lotes + movimientos (los adapters que mienten con "Jpa"); sesiones de picking
  (`ConcurrentHashMap` en `PickingService`) y cold-chain (`ColdChainMonitorService`) quedan
  como **B2** — son agregados distintos con semántica de sesión, no de persistencia simple,
  y meterlas en B1 alargaría el feedback loop de TDD. **Decisión de diseño**: V3 (número real,
  no V5 del openspec), tablas `inventory_lots`/`inventory_movements` (evita colisión con
  `stock_movement_logs`), JSON para metadata vía Hibernate 6 (`@JdbcTypeCode`), y el gap de
  provisioning de schemas de tenant queda explícito como sistémico (afecta a V2 igual) en vez
  de resolverlo parcialmente aquí. **Errata propia detectada**: ADR-004 de ARCHITECTURE.md
  (feature D) afirma que `SchemaIsolationValidator` fue eliminado — es falso (el archivo existe
  y lo usan `TenantIsolationIntegrationTest` y `SchemaIsolationValidatorTest`);
  `SearchPathConnectionInterceptor` y los `V__enable_rls_*.sql` sí se eliminaron. Se corregirá
  en TD6. **Errata propia 2**: TD7 de la feature D marcó 5.5-5.10 como "no existen" buscando
  solo nombres `*UseCase`; los servicios existen (`TransferInventoryService`,
  `RecordTemperatureService`, `QueryLotHistoryService`, `QueryExpiringLotsService`,
  `InternalOperationsService.processInventoryAdjustment`). Se corrige en TD6.
