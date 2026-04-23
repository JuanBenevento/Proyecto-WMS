# Proposal: Módulo de Movimiento de Inventario con Trazabilidad

## Intent

Resolver la trazabilidad completa de lotes y movimiento de inventario para empresas del parque industrial y cordón frutihortícola de Mar del Plata, donde la normativa de frío (cadena de frío) y el control de vence son requisitos críticos para exportaciones agrícolas y pesqueras.

## Scope

### In Scope
- Movimiento de inventario: recepciones, emisiones, transferencias, ajustes físicos
- Trazabilidad por lote/número de batch con fecha de producción y origen del proveedor
- Estrategia FEFO (First Expired First Out) para asignación automática de inventario
- Cadena de frío: registro de temperatura por lote/elemento
- Control de peso: peso neto y bruto por batch

### Out of Scope
- App móvil PWA (fase posterior)
- Integraciones externas (MercadoLibre, sistemas portuarios)
- Multi-almén (ya resuelto por módulo location existente)

## Capabilities

### New Capabilities
- `inventory-movement`: Gestión de movimientos de inventario con tipos: RECEIPT, ISSUE, TRANSFER, ADJUSTMENT
- `lot-traceability`: Trazabilidad completa por lote con producción, origen, temperatura
- `fefo-allocation`: Estrategia FEFO para picking automático
- `cold-chain-monitoring`: Registro de temperatura por lote/item
- `batch-weight-tracking`: Peso neto/bruto por batch

### Modified Capabilities
- `inventory-item`: Extender con productionDate, temperature, netWeight, grossWeight, lotOrigin
- `product`: Añadir temperatureRequirement (opt-in para productos que requieren frío)

## Approach

1. Extender `InventoryItem` con campos adicionales: productionDate, temperature, netWeight, grossWeight, lotOrigin
2. Crear agregado `Lot` (o convertir BatchNumber a Value Object con más atributos)
3. Crear interfaz `FefoAllocationStrategy` con implementación default
4. Añadir entity `InventoryMovement` con auditoría completa
5. Migraciones de DB para nuevos campos

## Affected Areas

| Área | Impact | Descripción |
|------|--------|-------------|
| `inventory/domain/model/InventoryItem.java` | Modificado | Añadir productionDate, temperature, weight fields |
| `shared/domain/valueobject/BatchNumber.java` | Modificado | Extender como Lot value object |
| `inventory/domain/model/InventoryMovement.java` | Nuevo | Entity para movimientos |
| `inventory/application/port/in/FefoAllocationStrategy.java` | Nuevo | Interfaz para estrategia FEFO |
| `inventory/infrastructure/out/persistence/` | Modificado | Migraciones y entity updates |

## Risks

| Riesgo | Probabilidad | Mitigación |
|-------|--------------|------------|
| Migraciones con datos existentes | Alta | Migraciones con defaults seguros, no-null donde aplica |
| Performance con FEFO en inventarios grandes | Media | Index en expiryDate, paginación en queries |
| Retrocompatibilidad con API existente | Media | Añadir campos opcionales en DTOs |

## Rollback Plan

1. Revertir migración de DB (schema migration tool)
2. Revertir cambios en InventoryItem a versión anterior
3. Eliminar InventoryMovement entity si no hay datos críticos
4. Tags en git para rollback atómico

## Dependencies

- Módulo Inventory existente ( InventoryItem, InventoryStatus)
- Módulo Product ( Product dimensions)
- Módulo Warehouse ( ZoneType para cold chain)
- Reference: Order module para patrones (si existe)

## Success Criteria

- [ ] Recepciones registran productionDate, temperature, weight por lote
- [ ] Picking usa FEFO: items con menor expiry se seleccionan primero
- [ ] Movimientos tienen auditoría completa (who, when, from/to)
- [ ] Temperatura se registra en recepciones para zonas frías
- [ ] Trazabilidad: cualquier item puede rastrearse a lote origen