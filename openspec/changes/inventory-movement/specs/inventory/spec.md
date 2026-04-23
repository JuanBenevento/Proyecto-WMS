# Delta Spec — Inventory Movement Module

> **Change**: inventory-movement
> **Status**: NEW CAPABILITY (no existing main spec)
> **Artifact Mode**: `openspec`
> **Domain**: inventory

---

## MODIFIED Requirements

### Requirement: InventoryItem — Extended with Lot Fields

The system MUST extend `InventoryItem` with the following optional fields to support lot traceability, cold chain, and weight tracking.

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `lotNumber` | String | Yes | Unique lot identifier (auto-generated or supplier batch) |
| `productionDate` | LocalDate | Yes | Manufacturing/production date |
| `origin` | String | Yes | Supplier/farm origin identifier |
| `temperatureRange` | TemperatureRange | No | Min/max storage temp for cold chain |
| `netWeight` | BigDecimal | No | Net weight of the batch |
| `grossWeight` | BigDecimal | No | Gross weight (with packaging) |
| `metadata` | Map<String, String> | No | Flexible industry-specific attributes |

#### Scenario: Receipt with lot and temperature data

- GIVEN a product SKU "TOMATE-001" and a receipt of 100 units from supplier "Fincas del Sur"
- WHEN the receipt is registered with productionDate=2026-04-01, origin="Fincas del Sur", temperature=4.5°C
- THEN the system MUST create an `InventoryItem` with those lot fields populated
- AND `InventoryStatus` MUST be `IN_QUALITY_CHECK`

#### Scenario: InventoryItem reconstruction from repository

- GIVEN an existing `InventoryItem` with lot fields in the database
- WHEN `InventoryItem.fromRepository()` is called
- THEN the system MUST reconstruct the item with all lot fields including `metadata`

---

## ADDED Requirements

### Requirement: Lot Aggregate

The system MUST provide a `Lot` aggregate root to manage batch lifecycle independently of `InventoryItem`.

| Attribute | Type | Description |
|-----------|------|-------------|
| `lotNumber` | String (PK) | Unique identifier |
| `productSku` | String | Associated product |
| `batchNumber` | String | Supplier batch reference |
| `productionDate` | LocalDate | Manufacturing date |
| `origin` | String | Source identifier |
| `status` | LotStatus | ACTIVE, EXHAUSTED, EXPIRED, QUARANTINE |
| `temperatureRange` | TemperatureRange | Min/max storage temp |
| `netWeight` | BigDecimal | Net weight |
| `grossWeight` | BigDecimal | Gross weight |
| `metadata` | Map | Industry-specific data |

`LotStatus` enum: `ACTIVE`, `EXHAUSTED` (qty=0), `EXPIRED` (past expiry), `QUARANTINE` (held for inspection)

#### Scenario: Lot creation during receipt

- GIVEN a receipt command with lot data (batchNumber, productionDate, origin, temperatureRange, weights)
- WHEN `RegisterReceiptUseCase.execute()` is called
- THEN the system MUST create a new `Lot` aggregate with status=ACTIVE
- AND link it to the `InventoryItem`

#### Scenario: Lot exhaustion when quantity depletes

- GIVEN a Lot with status=ACTIVE and quantity approaching zero
- WHEN quantity reaches 0 via any issue/transfer
- THEN the system MUST update Lot status to EXHAUSTED

#### Scenario: Lot expiry detection

- GIVEN a Lot with an associated expiry date
- WHEN the current date exceeds that expiry date
- THEN the system MUST mark Lot status as EXPIRED
- AND alert the configured notification channel

---

### Requirement: InventoryMovement Entity

The system MUST provide an `InventoryMovement` entity to record all inventory state changes with full audit trail.

| Attribute | Type | Required | Description |
|-----------|------|----------|-------------|
| `movementId` | UUID | Yes | Unique identifier |
| `type` | MovementType | Yes | RECEIPT, ISSUE, TRANSFER, ADJUSTMENT, RETURN |
| `lotNumber` | String | Yes | Source lot |
| `quantity` | BigDecimal | Yes | Quantity moved |
| `fromLocation` | String | No | Source location (null for RECEIPT) |
| `toLocation` | String | Yes | Destination location |
| `reason` | String | Yes | Business justification |
| `performedBy` | String | Yes | User identifier |
| `temperatureAtMovement` | BigDecimal | No | Temp recorded at movement time |
| `weightAtMovement` | BigDecimal | No | Weight at movement |
| `certificateUrl` | String | No | SENASA/ADANA certificate reference |
| `timestamp` | LocalDateTime | Yes | When movement occurred |

`MovementType` enum: `RECEIPT`, `ISSUE`, `TRANSFER`, `ADJUSTMENT`, `RETURN`

#### Scenario: Full movement audit trail

- GIVEN a transfer from location "ZONA-FRIA-A-01-01" to "PREP-AREA-01"
- WHEN `TransferInventoryUseCase.execute()` is called by user "juan.martinez"
- THEN the system MUST create an `InventoryMovement` with type=TRANSFER
- AND `fromLocation`="ZONA-FRIA-A-01-01", `toLocation`="PREP-AREA-01", `performedBy`="juan.martinez"
- AND persist it immutably

#### Scenario: Movement with temperature reading for cold chain

- GIVEN a movement of a cold-chain item
- WHEN the movement is registered with temperatureAtMovement=5.2°C
- THEN the movement MUST record that temperature
- AND the system MUST check if it's within the lot's `temperatureRange`

#### Scenario: Reject movement with expired lot

- GIVEN a lot with status=EXPIRED
- WHEN any movement (except RETURN to quarantine) is attempted
- THEN the system MUST reject with `ExpiredLotMovementException`
- AND log the attempted movement with status=REJECTED

---

### Requirement: AllocationStrategy Interface (Strategy Pattern)

The system MUST provide an `AllocationStrategy` interface for pluggable lot selection algorithms. Each company enables the strategies it needs. Core WMS has NO industry-specific logic baked in.

```java
public interface AllocationStrategy {
    List<LotAllocation> selectLots(String productSku, BigDecimal quantity, AllocationContext context);
    String getStrategyName();
    boolean isEnabled();
}
```

| Strategy | Industry | Behavior |
|----------|----------|----------|
| `FefoAllocationStrategy` | Food (frutihortícola) | Orders by expiryDate ASC, excludes expired |
| `FifoAllocationStrategy` | Metalworking (parque industrial) | Orders by productionDate ASC |
| `LotControlStrategy` | Pharmaceutical | Requires specific lot, blocks others |
| `WeightCertificationStrategy` | Port/Export | Records weight at each movement |

`LotAllocation` record: `lotNumber`, `quantity`, `priority`, `reason`

#### Scenario: FEFO allocation for picking

- GIVEN product SKU "TOMATE-001" needs 50 units
- AND lots exist: L001 (expiry 2026-05-01, qty 30), L002 (expiry 2026-06-01, qty 40)
- WHEN `AllocateByStrategyUseCase.execute(productSku, 50)` with FEFO strategy
- THEN the system MUST return [L001 qty 30, L002 qty 20] (FEFO order)

#### Scenario: FIFO allocation for metalworking

- GIVEN product SKU "ACERO- tubular-50mm" needs 100 units
- AND lots exist: L100 (productionDate 2026-01-15, qty 80), L101 (productionDate 2026-03-20, qty 50)
- WHEN FIFO strategy is applied
- THEN the system MUST prioritize L100 before L101 (older first)

#### Scenario: Insufficient stock across lots

- GIVEN product SKU "TOMATE-001" needs 200 units
- AND available lots total only 150 units
- WHEN `AllocateByStrategyUseCase.execute()` is called
- THEN the system MUST throw `InsufficientStockException` with available=150, requested=200

---

### Requirement: TemperatureMonitoringStrategy Interface

The system MUST provide a `TemperatureMonitoringStrategy` interface for cold chain compliance.

```java
public interface TemperatureMonitoringStrategy {
    void recordTemperature(String lotNumber, BigDecimal temperature, 
                          LocalDateTime timestamp, String location);
    boolean checkRange(String lotNumber);
    void alertIfOutOfRange(String lotNumber, BigDecimal currentTemp);
}
```

#### Scenario: Temperature out of range alert

- GIVEN lot L001 has temperatureRange (2°C min, 8°C max)
- WHEN a reading of 10.5°C is recorded at location "ZONA-FRIA-A-01"
- THEN `checkRange()` MUST return false
- AND the system MUST trigger an alert via the configured channel (email/webhook)

#### Scenario: Temperature within range — no alert

- GIVEN lot L001 has temperatureRange (2°C min, 8°C max)
- WHEN a reading of 5.0°C is recorded
- THEN `checkRange()` MUST return true
- AND no alert is triggered

---

### Requirement: RegisterReceiptUseCase

The system MUST provide a use case to receive goods and create lots with traceability data.

**Command**: `RegisterReceiptCommand`
- `productSku`, `quantity`, `locationCode`, `lotNumber`, `batchNumber`, `productionDate`, `origin`
- `temperatureRange` (optional), `netWeight` (optional), `grossWeight` (optional), `metadata` (optional)
- `temperatureAtReceipt` (optional), `certificateUrl` (optional)

**Result**: `ReceiptResponse` with `lpn`, `lotNumber`, `movementId`, `quantity`

#### Scenario: Receipt with all traceability fields

- GIVEN a receipt command with batchNumber="BATCH-2026-0415", productionDate=2026-04-15, origin="Fincas El Sol"
- WHEN `RegisterReceiptUseCase.execute(command)` is called
- THEN the system MUST create a Lot with status=ACTIVE
- AND create an InventoryMovement with type=RECEIPT
- AND return the new LPN

#### Scenario: Receipt for quarantined goods

- GIVEN a receipt command with `status=QUARANTINE` (flagged by SENASA)
- WHEN the receipt is registered
- THEN the Lot MUST be created with status=QUARANTINE
- AND not be available for picking until released

---

### Requirement: RegisterIssueUseCase

The system MUST provide a use case to issue goods from lots using the configured allocation strategy.

**Command**: `RegisterIssueCommand`
- `productSku`, `quantity`, `toLocation` (for picking slip), `reason`, `allocationStrategy` (optional)

**Result**: `IssueResponse` with `allocations[]`, `movementId`

#### Scenario: Issue picks from FEFO lots

- GIVEN product "TOMATE-001" needs 40 units for order ORD-2026-001
- AND available lots: L1 (30 units, expiry 2026-05-01), L2 (20 units, expiry 2026-06-01)
- WHEN `RegisterIssueUseCase.execute(command)` is called
- THEN the system MUST allocate from L1 first (expires sooner)
- AND create movement with quantities matching allocation

#### Scenario: Issue blocked for quarantined lot

- GIVEN a lot with status=QUARANTINE
- WHEN an issue is attempted against that lot
- THEN the system MUST reject with `LotQuarantineException`

---

### Requirement: QueryLotHistoryUseCase

The system MUST provide a use case to retrieve full traceability by lot number.

**Command**: `QueryLotHistoryCommand` with `lotNumber`

**Result**: `LotHistoryResponse` with lot details and list of all movements affecting that lot

#### Scenario: Full traceability report

- GIVEN lot number "LOT-2026-0415"
- WHEN `QueryLotHistoryUseCase.execute()` is called
- THEN the system MUST return lot details (origin, productionDate, weights)
- AND all movements (RECEIPT → TRANSFERS → ISSUE) in chronological order
- AND temperature readings linked to movements

---

### Requirement: QueryExpiringLotsUseCase

The system MUST provide a use case to query lots expiring within N days.

**Command**: `QueryExpiringLotsCommand` with `daysThreshold`, `productSku` (optional)

**Result**: List of lots with expiry date within threshold, sorted by expiry ASC

#### Scenario: Alert for lots expiring in 7 days

- GIVEN daysThreshold=7
- WHEN the query is executed
- THEN the system MUST return all ACTIVE lots with expiryDate within 7 days
- AND include quantity, location, and contact info

---

## MODIFIED Requirements

### Requirement: Product — Temperature Requirement Opt-in

The system MUST extend `Product` with an optional `temperatureRequirement` field for products requiring cold chain.

| Field | Type | Description |
|-----------|------|-------------|
| `temperatureRequirement` | TemperatureRange | Required storage range (opt-in) |

#### Scenario: Product with cold chain requirement

- GIVEN a product "TOMATE-001" with temperatureRequirement (2°C, 8°C)
- WHEN a lot is received for this product
- THEN the system MUST validate temperature readings against this requirement
- AND warn if lot's temperatureRange differs from product's requirement

---

## Non-Functional Requirements

| NFR | Requirement |
|-----|-------------|
| **Performance** | FEFO allocation query MUST complete in < 500ms for 10,000 lots (indexed on expiryDate, productSku) |
| **Audit** | All movements MUST be persisted immutably with timestamp, user, reason |
| **API** | REST endpoints with OpenAPI 3.0 documentation |
| **Scalability** | Strategy pattern allows per-company activation — no hardcoded industry logic in core |

---

## Scope Summary

| Capability | Type | Requirements | Scenarios |
|-----------|------|-------------|----------|
| `InventoryItem` lot extension | MODIFIED | 2 | 2 |
| `Lot` aggregate | ADDED | 3 | 3 |
| `InventoryMovement` entity | ADDED | 3 | 3 |
| `AllocationStrategy` interface | ADDED | 3 | 3 |
| `TemperatureMonitoringStrategy` | ADDED | 2 | 2 |
| `RegisterReceiptUseCase` | ADDED | 2 | 2 |
| `RegisterIssueUseCase` | ADDED | 2 | 2 |
| `QueryLotHistoryUseCase` | ADDED | 1 | 1 |
| `QueryExpiringLotsUseCase` | ADDED | 1 | 1 |
| `Product.temperatureRequirement` | MODIFIED | 1 | 1 |

**Total**: 20 requirements, 22 scenarios