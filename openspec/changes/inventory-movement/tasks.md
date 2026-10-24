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

- [ ] 5.1 Create `RegisterReceiptCommand.java` **and** `RegisterReceiptUseCase.java` — el Command existe (`inventory/application/RegisterReceiptCommand.java`); la interfaz `RegisterReceiptUseCase` **no existe** (la lógica vive en `RegisterReceiptService.java`)
- [x] 5.2 Create `RegisterReceiptService.java` — real: `inventory/application/service/RegisterReceiptService.java`
- [ ] 5.3 Create `RegisterIssueCommand.java` **and** `RegisterIssueUseCase.java` — el Command existe (`inventory/application/RegisterIssueCommand.java`); la interfaz `RegisterIssueUseCase` **no existe** (la lógica vive en `RegisterIssueService.java`)
- [x] 5.4 Create `RegisterIssueService.java` — real: `inventory/application/service/RegisterIssueService.java`
- [x] 5.5 Create `TransferInventoryUseCase.java` — real (equivalente): `inventory/application/service/TransferInventoryService.java` (la interfaz `*UseCase` no existe; el código usa servicios directos)
- [x] 5.6 Create `AdjustInventoryUseCase.java` — real (equivalente): `inventory/application/service/InternalOperationsService.java` (`processInventoryAdjustment`)
- [x] 5.7 Create `RecordTemperatureUseCase.java` — real (equivalente): `inventory/application/service/RecordTemperatureService.java`
- [ ] 5.8 Create `RecordWeightUseCase.java` — **no existe** ni interfaz ni servicio con ese nombre (corregido 2026-10-23: búsqueda previa solo miraba `*UseCase`; ahora verificado con `Get-ChildItem *Service.java` — tampoco hay `RecordWeightService`)
- [x] 5.9 Create `QueryLotHistoryUseCase.java` — real (equivalente): `inventory/application/service/QueryLotHistoryService.java`
- [x] 5.10 Create `QueryExpiringLotsUseCase.java` — real (equivalente): `inventory/application/service/QueryExpiringLotsService.java`

## Phase 6: Infrastructure (Persistence, REST)

- [x] 6.1 Create Flyway migration — real (2026-10-23, feature B1): `V3__inventory_lots_and_movements.sql` con `inventory_lots` + `inventory_movements` (el plan proponía "V5"; se usó el siguiente número real tras V1/V2)
- [x] 6.2 Create `LotEntity.java` JPA entity — real (2026-10-23): `inventory/infrastructure/out/persistence/LotEntity.java`
- [x] 6.3 Create `InventoryMovementEntity.java` JPA entity — real (2026-10-23): `inventory/infrastructure/out/persistence/InventoryMovementEntity.java`
- [x] 6.4 Create JPA adapter for `LotRepository` — real (2026-10-23): `LotJpaAdapter.java` ahora delega a `LotJpaRepository` (Spring Data JPA); era un HashMap hasta la feature B1
- [x] 6.5 Create JPA adapter for `MovementRepository` — real (2026-10-23): `MovementJpaAdapter.java` ahora delega a `InventoryMovementJpaRepository` (append-only, sin delete/update); era un HashMap hasta la feature B1
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
- [x] 7.8 Run `./mvnw test` and verify all tests pass — la suite global está verde (323/0/0/2, 2026-10-23, tras feature B1); las pruebas 7.3–7.7 siguen pendientes

---

**Resumen de conciliación (2026-10-23, actualizado tras feature B1):** Phase 5 corregida —
los servicios `TransferInventoryService`, `RecordTemperatureService`, `QueryLotHistoryService`,
`QueryExpiringLotsService` y `InternalOperationsService.processInventoryAdjustment` **sí existen**
(la conciliación de 2026-10-21 solo buscaba nombres `*UseCase`, que nunca se usaron en el código;
quedan marcadas 5.5-5.7, 5.9, 5.10 con sus equivalentes reales). `RecordWeight*` sigue sin existir
(5.8 abierta, verificado 2026-10-23). Phase 6 (persistencia) completada por la feature B1:
migración `V3`, `LotEntity`, `InventoryMovementEntity` y adapters JPA reales. La feature B1
también añadió `LotAndMovementPersistenceIntegrationTest` (6 tests RED→GREEN).
