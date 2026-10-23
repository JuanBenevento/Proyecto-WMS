# Tasks: Inventory Movement Module

> **Conciliado contra el árbol real el 2026-10-21** (regla: ninguna tarea marcada sin archivo
> correspondiente). Correcciones de nombres aplicadas: `LotRepositoryPort` → **`LotRepository`**,
> `InventoryMovementRepositoryPort` → **`MovementRepository`**, adapters reales
> `LotJpaAdapter`/`MovementJpaAdapter` (⚠️ en memoria), paths de dominio reales
> (`domain/model/`, no `domain/valueobject/`; strategies en `domain/strategy/`).
> **Faltantes grosorosos = pendiente de la feature B**: la persistencia JPA real de lotes y
> movimientos (Flyway + entities + adapters JPA) no existe todavía — ver `docs/CURRENT_STATE.md` §5.

## Phase 1: Domain Foundation (Value Objects, Enums, Exceptions)

- [x] 1.1 Create `TemperatureRange.java` value object — real: `inventory/domain/model/TemperatureRange.java`
- [x] 1.2 Create `AllocationContext.java` value object — real: `inventory/domain/model/AllocationContext.java`
- [x] 1.3 Create `LotAllocation.java` value object — real: `inventory/domain/model/LotAllocation.java`
- [x] 1.4 Create `LotStatus.java` enum: ACTIVE, EXHAUSTED, EXPIRED, QUARANTINE — real: `inventory/domain/model/LotStatus.java`
- [x] 1.5 Create `MovementType.java` enum: RECEIPT, ISSUE, TRANSFER, ADJUSTMENT, RETURN — real: `inventory/domain/model/MovementType.java`
- [x] 1.6 Create `ExpiredLotException.java` — real: `inventory/domain/exception/ExpiredLotException.java`
- [x] 1.7 Create `LotQuarantineException.java` — real: `inventory/domain/exception/LotQuarantineException.java`
- [x] 1.8 Create `InsufficientStockException.java` — real: `inventory/domain/exception/InsufficientStockException.java`

## Phase 2: Domain Models (Lot Aggregate, InventoryMovement, Events)

- [x] 2.1 Create `Lot.java` aggregate root — real: `inventory/domain/model/Lot.java`
- [x] 2.2 Create `InventoryMovement.java` entity — real: `inventory/domain/model/InventoryMovement.java`
- [x] 2.3 Create `LotReceivedEvent.java` domain event — real: `inventory/domain/event/LotReceivedEvent.java`
- [ ] 2.4 Create `LotExpiredEvent.java` domain event — **no existe en el árbol** (2026-10-21)
- [x] 2.5 Create `TemperatureAlertEvent.java` domain event — real: `inventory/domain/event/TemperatureAlertEvent.java`

## Phase 3: Ports (Interfaces)

- [x] 3.1 Create repository port — real: **`LotRepository`** en `inventory/application/port/out/LotRepository.java` (el plan decía `LotRepositoryPort`)
- [x] 3.2 Create movement repository port — real: **`MovementRepository`** en `inventory/application/port/out/MovementRepository.java` (el plan decía `InventoryMovementRepositoryPort`)
- [x] 3.3 Create `AllocationStrategy.java` interface — real: `inventory/application/port/in/AllocationStrategy.java`
- [x] 3.4 Create `TemperatureMonitoringStrategy.java` interface — real: `inventory/application/port/in/TemperatureMonitoringStrategy.java`

## Phase 4: Strategy Implementations

- [x] 4.1 Create `FefoAllocationStrategy.java` — real: `inventory/domain/strategy/FefoAllocationStrategy.java`
- [x] 4.2 Create `FifoAllocationStrategy.java` — real: `inventory/domain/strategy/FifoAllocationStrategy.java`
- [x] 4.3 Create `LotControlStrategy.java` — real: `inventory/domain/strategy/LotControlStrategy.java`
- [x] 4.4 Create `WeightCertificationStrategy.java` — real: `inventory/domain/strategy/WeightCertificationStrategy.java`

## Phase 5: Application Services & Use Cases

- [ ] 5.1 Create `RegisterReceiptCommand.java` **and** `RegisterReceiptUseCase.java` — el Command existe (`inventory/application/RegisterReceiptCommand.java`); la interfaz `RegisterReceiptUseCase` **no existe**
- [x] 5.2 Create `RegisterReceiptService.java` — real: `inventory/application/service/RegisterReceiptService.java`
- [ ] 5.3 Create `RegisterIssueCommand.java` **and** `RegisterIssueUseCase.java` — el Command existe (`inventory/application/RegisterIssueCommand.java`); la interfaz `RegisterIssueUseCase` **no existe**
- [x] 5.4 Create `RegisterIssueService.java` — real: `inventory/application/service/RegisterIssueService.java`
- [ ] 5.5 Create `TransferInventoryUseCase.java` — no existe (hay `InternalMoveCommand` en `port/in/command/`, pero no el use case)
- [ ] 5.6 Create `AdjustInventoryUseCase.java` — no existe (hay `InventoryAdjustmentCommand`, no el use case)
- [ ] 5.7 Create `RecordTemperatureUseCase.java` — no existe
- [ ] 5.8 Create `RecordWeightUseCase.java` — no existe
- [ ] 5.9 Create `QueryLotHistoryUseCase.java` — no existe
- [ ] 5.10 Create `QueryExpiringLotsUseCase.java` — no existe

## Phase 6: Infrastructure (Persistence, REST)

- [ ] 6.1 Create Flyway migration `V5__add_inventory_movement_schema.sql` — **no existe**; solo `V1__initial_tenant_structure.sql` y `V2__application_tables.sql`. Tablas de lotes/movimientos fuera de Flyway (feature B)
- [ ] 6.2 Create `LotEntity.java` JPA entity — no existe
- [ ] 6.3 Create `InventoryMovementEntity.java` JPA entity — no existe
- [ ] 6.4 Create JPA adapter for `LotRepository` — existe `LotJpaAdapter.java` en `inventory/infrastructure/out/persistence/` pero **es un HashMap en memoria**, no JPA (feature B); la tarea no está completa
- [ ] 6.5 Create JPA adapter for `MovementRepository` — existe `MovementJpaAdapter.java` pero **es un HashMap en memoria**, no JPA (feature B); la tarea no está completa
- [ ] 6.6 Create DTOs: `ReceiptResponse.java`, `IssueResponse.java`, `LotHistoryResponse.java` — solo existe `ReceiptResponse.java` (en `inventory/application/`); `IssueResponse` y `LotHistoryResponse` **no existen**
- [x] 6.7 Create `InventoryMovementController.java` with REST endpoints — real: `inventory/infrastructure/in/rest/InventoryMovementController.java`
- [x] 6.8 Create `RegisterReceiptCommand.java`, `RegisterIssueCommand.java` — existen ambos en `inventory/application/` (el plan los ubicaba en `application/port/in/command/`)

## Phase 7: Testing

- [x] 7.1 Write unit tests for `Lot` aggregate lifecycle — real: `src/test/.../LotTest.java` (+ `LotStatusTest.java`)
- [x] 7.2 Write unit tests for `FefoAllocationStrategy.selectLots()` ordering — real: `FefoAllocationStrategyTest.java`
- [ ] 7.3 Write unit tests for `TemperatureMonitoringStrategy.checkRange()` — no existe test dedicado (hay `TemperatureRangeTest.java`, que prueba el VO, no la strategy)
- [ ] 7.4 Write integration tests for `RegisterReceiptUseCase` with @DataJpaTest — no existen
- [ ] 7.5 Write integration tests for `RegisterIssueUseCase` with allocation — no existen
- [ ] 7.6 Write integration tests for `QueryLotHistoryUseCase` — no existen (el use case tampoco)
- [ ] 7.7 Write MockMvc integration tests for REST controller endpoints — no existen para `InventoryMovementController`
- [ ] 7.8 Run `./mvnw test` and verify all tests pass — la suite global está verde (317/0/0/2, 2026-10-21), pero las pruebas 7.3–7.7 siguen pendientes; tarea abierta

---

**Resumen de conciliación (2026-10-21): 25/47 completadas.** Las 22 abiertas son reales: 8 de
ellas (6.1–6.5 + relacionadas) corresponden exactamente al alcance de la feature B
(persistencia JPA de lotes/movimientos), el resto son casos de uso/DTOs/tests que nunca se
construyeron.
