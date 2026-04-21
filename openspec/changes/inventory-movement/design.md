# Design: Módulo de Movimiento de Inventario con Trazabilidad

## Enfoque Técnico

Implementar un módulo de inventario separado (inventory-movement) dentro del bounded context inventory, siguiendo hexagonal architecture现有的 patrones. El diseño usa estrategia para asignación de lotes (FEFO, FIFO) y monitoreo de temperatura para cadena de frío.

Las decisiones clave:
- Crear nuevo paquete `inventory-movement` para evitar acoplar con InventoryItem existente
- Usar Strategy pattern existente (PutAwayStrategy) como modelo para AllocationStrategy
- Crear `Lot` como aggregate root separado (no extender BatchNumber value object)
- Flyway migrations para nuevas tablas

## Decisiones de Arquitectura

### Decisión 001: Patrón Estrategia para Asignación de Lotes

**Elección**: Implementar `AllocationStrategy` interface con múltiples implementaciones (FEFO, FIFO, LotControl, WeightCert)
**Alternativas consideradas**: Un solo algoritmo FEFO hardcodeado en use case
**Rationale**: Specs requieren estrategias específicas por industria. Estrategia permite per-company activation sin modificar core.

### Decisión 002: Lot como Aggregate Root vs Value Object

**Elección**: `Lot` como aggregate root con su propio repositorio
**Alternativas consideradas**: Extender `BatchNumber` value object existente
**Rationale**: Lot tiene lifecycle propio (ACTIVE → EXHAUSTED → EXPIRED → QUARANTINE), necesita auditoría independiente y queries por expiry. Value object no tiene estas necesidades.

### Decisión 003: Monitoreo de Temperatura

**Elección**: `TemperatureMonitoringStrategy` interface con implementaciones configurables
**Alternativas consideradas**: Registro de temperatura en InventoryMovement
**Rationale**: Permite diferentes thresholds por producto/lote y múltiples canales de alerta (email, webhook). Spec define interfaz separable.

### Decisión 004: Ubicación del Módulo

**Elección**: Nuevo paquete `com.juanbenevento.wms.inventory.inventorymovement` (submódulo dentro de inventory)
**Alternativas consideradas**: `inventory.domain.lot` o `inventory.domain.movement`
**Rationale**: Specs definen "Inventory Movement Module" como capacidad separada. Mantiene clear boundary sin modificar inventory existente.

## Flujo de Datos

```
InventoryController (REST)
    ↓ RegisterReceiptCommand
RegisterReceiptUseCase
    → LotFactory.create() → Lot aggregate
    → AllocationStrategy.selectLots()
    → InventoryMovementRepositoryPort.save() → InventoryMovement
    → LotRepositoryPort.save() → Lot
    
AllocationStrategy (Strategy Pattern)
    → FefoAllocationStrategy.selectLots() → List<LotAllocation>
    → FifoAllocationStrategy.selectLots()
    → LotControlStrategy.selectLots()
    → WeightCertificationStrategy.selectLots()

TemperatureMonitoringStrategy
    → recordTemperature() → TemperatureLog
    → checkRange() → boolean
    → alertIfOutOfRange() → AlertEvent
```

## Cambios en Archivos

| Archivo | Acción | Descripción |
|---------|--------|-------------|
| `inventory/domain/model/Lot.java` | Crear | Aggregate root con lifecycle |
| `inventory/domain/model/InventoryMovement.java` | Crear | Entity para auditoría |
| `inventory/domain/model/LotStatus.java` | Crear | Enum: ACTIVE, EXHAUSTED, EXPIRED, QUARANTINE |
| `inventory/domain/model/MovementType.java` | Crear | Enum: RECEIPT, ISSUE, TRANSFER, ADJUSTMENT, RETURN |
| `inventory/domain/valueobject/TemperatureRange.java` | Crear | Value object min/max temp |
| `inventory/domain/valueobject/LotAllocation.java` | Crear | Value object para resultado de estrategia |
| `inventory/domain/strategy/AllocationStrategy.java` | Crear | Interface estrategia |
| `inventory/domain/strategy/FefoAllocationStrategy.java` | Crear | Implementación FEFO |
| `inventory/domain/strategy/FifoAllocationStrategy.java` | Crear | Implementación FIFO |
| `inventory/domain/strategy/LotControlStrategy.java` | Crear | Implementación control制药 |
| `inventory/domain/strategy/WeightCertificationStrategy.java` | Crear | Implementación certificación peso |
| `inventory/domain/strategy/TemperatureMonitoringStrategy.java` | Crear | Interface monitoreo |
| `inventory/domain/exception/ExpiredLotMovementException.java` | Crear | Excepción lote vencido |
| `inventory/domain/exception/LotQuarantineException.java` | Crear | Excepción lote en cuarentena |
| `inventory/domain/exception/InsufficientStockException.java` | Crear | Excepción stock insuficiente |
| `inventory/application/port/in/usecases/RegisterReceiptUseCase.java` | Crear | Use case receipt |
| `inventory/application/port/in/usecases/RegisterIssueUseCase.java` | Crear | Use case issue |
| `inventory/application/port/in/usecases/AllocateStockUseCase.java` | Crear | Use case asignación por estrategia |
| `inventory/application/port/in/usecases/QueryLotHistoryUseCase.java` | Crear | Use case trazabilidad |
| `inventory/application/port/in/usecases/QueryExpiringLotsUseCase.java` | Crear | Use case lotes por vencer |
| `inventory/application/port/in/usecases/RecordTemperatureUseCase.java` | Crear | Use case temperatura |
| `inventory/application/port/out/LotRepositoryPort.java` | Crear | Puerto repositorio Lot |
| `inventory/application/port/out/InventoryMovementRepositoryPort.java` | Crear | Puerto repositorio Movimiento |
| `inventory/application/service/RegisterReceiptService.java` | Crear | Servicio aplicación receipt |
| `inventory/application/service/RegisterIssueService.java` | Crear | Servicio aplicación issue |
| `inventory/application/service/AllocationService.java` | Crear | Servicio aplicación estrategia |
| `inventory/application/service/TemperatureMonitoringService.java` | Crear | Servicio temp |
| `inventory/infrastructure/out/persistence/LotEntity.java` | Crear |/entity JPA Lot |
| `inventory/infrastructure/out/persistence/InventoryMovementEntity.java` | Crear | Entity JPA Movimiento |
| `inventory/infrastructure/out/persistence/LotRepositoryAdapter.java` | Crear | Adapter JPA Lot |
| `inventory/infrastructure/out/persistence/InventoryMovementRepositoryAdapter.java` | Crear | Adapter JPA Movimiento |
| `inventory/infrastructure/in/rest/InventoryMovementController.java` | Crear | REST controller |
| `src/main/resources/db/migration/V5__add_inventory_movement_schema.sql` | Crear | Flyway migration |
| `inventory/application/port/in/command/RegisterReceiptCommand.java` | Crear | Command receipt |
| `inventory/application/port/in/command/RegisterIssueCommand.java` | Crear | Command issue |
| `inventory/application/port/in/dto/ReceiptResponse.java` | Crear | DTO respuesta receipt |
| `inventory/application/port/in/dto/IssueResponse.java` | Crear | DTO respuesta issue |
| `inventory/application/port/in/dto/LotHistoryResponse.java` | Crear | DTO trazabilidad |

## Interfaces / Contratos

```java
// Domain Model: Lot
public class Lot {
    private final String lotNumber;
    private final String productSku;
    private final String batchNumber;
    private final LocalDate productionDate;
    private final String origin;
    private LotStatus status;
    private TemperatureRange temperatureRange;
    private BigDecimal netWeight;
    private BigDecimal grossWeight;
    private Map<String, String> metadata;
    
    public static Lot create(RegisterReceiptCommand command) { }
    public void updateStatus(LotStatus newStatus) { }
    public boolean isExpired() { }
    public void decreaseQuantity(BigDecimal amount) { }
}

// AllocationStrategy Interface
public interface AllocationStrategy {
    List<LotAllocation> selectLots(String productSku, BigDecimal quantity, AllocationContext context);
    String getStrategyName();
    boolean isEnabled();
}

// TemperatureMonitoringStrategy Interface
public interface TemperatureMonitoringStrategy {
    void recordTemperature(String lotNumber, BigDecimal temperature, LocalDateTime timestamp, String location);
    boolean checkRange(String lotNumber);
    void alertIfOutOfRange(String lotNumber, BigDecimal currentTemp);
}

// Ports
public interface LotRepositoryPort {
    Optional<Lot> findByLotNumber(String lotNumber);
    List<Lot> findByProductSku(String sku);
    List<Lot> findAvailableForAllocation(String sku);
    List<Lot> findExpiringWithinDays(int days);
    Lot save(Lot lot);
}

public interface InventoryMovementRepositoryPort {
    InventoryMovement save(InventoryMovement movement);
    List<InventoryMovement> findByLotNumber(String lotNumber);
    List<InventoryMovement> findByProductSku(String sku);
}
```

## Estrategia de Pruebas

| Capa | Qué probar | Enfoque |
|------|------------|---------|
| Unit | Lot aggregate lifecycle | Unit tests con mock de repositorio |
| Unit | AllocationStrategy.selectLots() | Tests con lots conocidos, verificar orden |
| Unit | TemperatureMonitoringStrategy.checkRange() | Tests con rangos conocido |
| Integration | Use cases completos | Spring integration tests con @DataJpaTest |
| Integration | REST controller | MockMvc integration tests |
| E2E | Full receipt → allocation → issue flow | Cypress/Playwright (futura fase) |

## Migración / Lanzamiento

No se requiere migración de datos existente. Los campos nuevos en InventoryItem son opcionales.

Feature flags para rollout gradual:
- `enable.inventory-movement.module`: Activa nuevo módulo
- `enable.fefo.allocation`: Activa estrategia FEFO
- `enable.temperature.monitoring`: Activa monitoreo temperatura

## Preguntas Abiertas

- [ ] ¿Índices adicionales en inventory_items para lot queries? (performance NFR)
- [ ] ¿Event sourced movement log o append-only table? (decisión auditoría)
- [ ] ¿Webhooks para alerts temperatura o solo email? (configuración)
- [ ] ¿Multi-tenant con戦略 activa por empresa?

---

## Resumen Ejecutivo

Diseño técnico para módulo de movimiento de inventario con:
- Lot aggregate root con lifecycle (ACTIVE/EXHAUSTED/EXPIRED/QUARANTINE)
- InventoryMovement entity para auditoría immutable
- AllocationStrategy interface con 4 implementaciones (FEFO, FIFO, LotControl, WeightCert)
- TemperatureMonitoringStrategy interface para cold chain
- Flyway migration para nuevas tablas
- REST API con todos los endpoints definidos en specs

Arquitectura hexagonal siguiendo patrones existentes. Estrategia permite per-company activation sin modificar core.

**Arquitect Decisions**: 4 documentadas
**Files**: ~30 archivos nuevos
**Testing**: Unit + Integration coverage plan

Listo para tareas (sdd-tasks).