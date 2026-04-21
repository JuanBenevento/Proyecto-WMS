# Tasks: Inventory Movement Module

## Phase 1: Domain Foundation (Value Objects, Enums, Exceptions)

- [ ] 1.1 Create `TemperatureRange.java` value object in `inventory/domain/valueobject/`
- [ ] 1.2 Create `AllocationContext.java` value object in `inventory/domain/valueobject/`
- [ ] 1.3 Create `LotAllocation.java` value object in `inventory/domain/valueobject/`
- [ ] 1.4 Create `LotStatus.java` enum: ACTIVE, EXHAUSTED, EXPIRED, QUARANTINE
- [ ] 1.5 Create `MovementType.java` enum: RECEIPT, ISSUE, TRANSFER, ADJUSTMENT, RETURN
- [ ] 1.6 Create `ExpiredLotException.java` in `inventory/domain/exception/`
- [ ] 1.7 Create `LotQuarantineException.java` in `inventory/domain/exception/`
- [ ] 1.8 Create `InsufficientStockException.java` in `inventory/domain/exception/`

## Phase 2: Domain Models (Lot Aggregate, InventoryMovement, Events)

- [ ] 2.1 Create `Lot.java` aggregate root with factory method `Lot.create()` and lifecycle methods
- [ ] 2.2 Create `InventoryMovement.java` entity with all audit fields
- [ ] 2.3 Create `LotReceivedEvent.java` domain event
- [ ] 2.4 Create `LotExpiredEvent.java` domain event
- [ ] 2.5 Create `TemperatureAlertEvent.java` domain event

## Phase 3: Ports (Interfaces)

- [ ] 3.1 Create `LotRepositoryPort.java` interface in `application/port/out/`
- [ ] 3.2 Create `InventoryMovementRepositoryPort.java` interface in `application/port/out/`
- [ ] 3.3 Create `AllocationStrategy.java` interface in `application/port/in/`
- [ ] 3.4 Create `TemperatureMonitoringStrategy.java` interface in `application/port/in/`

## Phase 4: Strategy Implementations

- [ ] 4.1 Create `FefoAllocationStrategy.java` - orders by expiryDate ASC, excludes expired
- [ ] 4.2 Create `FifoAllocationStrategy.java` - orders by productionDate ASC
- [ ] 4.3 Create `LotControlStrategy.java` - requires specific lot, blocks others
- [ ] 4.4 Create `WeightCertificationStrategy.java` - records weight at each movement

## Phase 5: Application Services & Use Cases

- [ ] 5.1 Create `RegisterReceiptCommand.java` and `RegisterReceiptUseCase.java`
- [ ] 5.2 Create `RegisterReceiptService.java` application service
- [ ] 5.3 Create `RegisterIssueCommand.java` and `RegisterIssueUseCase.java`
- [ ] 5.4 Create `RegisterIssueService.java` application service
- [ ] 5.5 Create `TransferInventoryUseCase.java` for transfers
- [ ] 5.6 Create `AdjustInventoryUseCase.java` for adjustments
- [ ] 5.7 Create `RecordTemperatureUseCase.java`
- [ ] 5.8 Create `RecordWeightUseCase.java`
- [ ] 5.9 Create `QueryLotHistoryUseCase.java`
- [ ] 5.10 Create `QueryExpiringLotsUseCase.java`

## Phase 6: Infrastructure (Persistence, REST)

- [ ] 6.1 Create Flyway migration `V5__add_inventory_movement_schema.sql` with lot and movement tables
- [ ] 6.2 Create `LotEntity.java` JPA entity in `infrastructure/out/persistence/`
- [ ] 6.3 Create `InventoryMovementEntity.java` JPA entity
- [ ] 6.4 Create `LotRepositoryAdapter.java` implementing `LotRepositoryPort`
- [ ] 6.5 Create `InventoryMovementRepositoryAdapter.java` implementing port
- [ ] 6.6 Create DTOs: `ReceiptResponse.java`, `IssueResponse.java`, `LotHistoryResponse.java`
- [ ] 6.7 Create `InventoryMovementController.java` with all REST endpoints
- [ ] 6.8 Create `RegisterReceiptCommand.java`, `RegisterIssueCommand.java` in `application/port/in/command/`

## Phase 7: Testing

- [ ] 7.1 Write unit tests for `Lot` aggregate lifecycle (create, updateStatus, isExpired)
- [ ] 7.2 Write unit tests for `FefoAllocationStrategy.selectLots()` ordering
- [ ] 7.3 Write unit tests for `TemperatureMonitoringStrategy.checkRange()`
- [ ] 7.4 Write integration tests for `RegisterReceiptUseCase` with @DataJpaTest
- [ ] 7.5 Write integration tests for `RegisterIssueUseCase` with allocation
- [ ] 7.6 Write integration tests for `QueryLotHistoryUseCase`
- [ ] 7.7 Write MockMvc integration tests for REST controller endpoints
- [ ] 7.8 Run `./mvnw test` and verify all tests pass
