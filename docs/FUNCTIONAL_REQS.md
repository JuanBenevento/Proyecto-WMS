# Requerimientos Funcionales — WMS

> **Backlog del producto con estado real de implementación** (verificado el 2026-10-21 contra
> el código). Las historias describen el producto deseado; cada una lleva su estado honesto.
> Contexto global: [`docs/CURRENT_STATE.md`](CURRENT_STATE.md).

**Leyenda de estados:** ✅ implementado y verificado · 🟡 parcial (limitaciones listadas) ·
🔴 no implementado (historia = roadmap)

---

## Épica 1: Master Data

### US-MD-01: Alta de producto con perfil logístico — 🟡

**Como** gerente de inventario, **quiero** registrar productos con dimensiones, peso y
restricciones, **para** que el sistema sugiera dónde guardarlos.

- **CA1 (alto/ancho/profundidad/peso obligatorios):** ✅ `Product` tiene `Dimensions` con
  validación en `validateState()` (`catalog/domain/model/Product.java`).
- **CA2 (flag automático si peso > 20kg):** ✅ como **cálculo**, no como flag persistido:
  `Product.isHeavyLoad()` delega en `Dimensions.isHeavyLoad()`
  (`catalog/domain/model/Product.java:55`).
- **CA3 (selección de `StorageCondition` en el producto):** 🔴 no existe tal campo. Está
  **superseded** por la derivación de zona en el put-away: `SmartPutAwayStrategy.determineZone()`
  mapea producto → `ZoneType` (DRY/COLD/FROZEN/HAZMAT).

### US-MD-02: Layout jerárquico del almacén — ✅

**Como** administrador, **quiero** ubicaciones jerárquicas con capacidad, **para** mapear el
almacén físico.

- ✅ `Location` tiene `locationCode`, `aisle`, `column`, `level`, `zoneType`,
  `maxWeight`/`maxVolume` y `hasSpaceFor(...)` (`warehouse/domain/model/Location.java`).
- El vocabulario del código (`aisle/column/level`) difiere del "Pasillo > Rack > Nivel" de
  historias viejas; el código es la referencia.
- Zonas disponibles (`ZoneType`): DRY_STORAGE, COLD_STORAGE, FROZEN_STORAGE, HAZMAT,
  RECEIVING_AREA, PICKING_AREA, DISPATCH_AREA, DOCK_DOOR, YARD, QUARANTINE.

---

## Épica 2: Inbound

### US-IN-01: Recepción y generación de LPN — ✅

**Como** operario de recepción, **quiero** generar un LPN con lote y vencimiento, **para**
mover pallets enteros.

- ✅ LPN como VO con validación y generación (`Lpn.java`; formato real `LPN-XXXXXXXX` —
  el ejemplo viejo `LPN-2024-00055` no corresponde al VO).
- ✅ `InventoryItem` persiste `lpn`, `batchNumber`, `expiryDate`
  (`inventory/domain/model/InventoryItem.java`).
- ✅ Estado inicial `IN_QUALITY_CHECK` existe en el enum de estado.

### US-IN-02: Put-away dirigido — 🟡

**Como** operario, **quiero** que el sistema sugiera la ubicación óptima, **para** optimizar
espacio y respetar cadena de frío.

- ✅ CA1 (compatibilidad de zona): `SmartPutAwayStrategy.determineZone(Product)` deriva
  FROZEN / COLD / HAZMAT / DRY según el producto; usado por
  `warehouse/application/service/LocationSuggestionService.java`.
- 🟡 CA2 (validación de capacidad física) y CA3 (consolidación por mismo SKU): existe lógica
  de capacidad a nivel entidad (`Location.hasSpaceFor`), pero el flujo completo
  sugerencia→reserva **no está verificado hoy** de punta a punta.

---

## Épica 3: Outbound

### US-OUT-01: Asignación de stock (hard allocation) al confirmar — ✅

**Como** sistema, **quiero** reservar stock al confirmar la orden, **para** evitar sobreventa.

- ✅ CA1 (FEFO): la asignación FEFO está implementada (código FEFO presente en el flujo de
  inventario; `expiryDate` en `InventoryItem` es el criterio de vencimiento).
- ✅ CA2 (AVAILABLE → ALLOCATED): estados presentes y ejercitados por la suite de workflows
  (`OrderInventoryIntegrationTest`, verde el 2026-10-21).
- ✅ CA3 (concurrencia): bloqueo optimista con `@Version` (3 entidades lo usan).

### US-OUT-02: Picking con validación de escaneo — 🟡

**Como** picker, **quiero** escanear ubicación y producto antes de confirmar, **para**
asegurar exactitud.

- 🟡 El flujo de picking existe (`inventory/application/service/PickingService.java`,
  workflows probados), **pero**: las sesiones viven en `ConcurrentHashMap` (se pierden al
  reiniciar) y la validación estricta de escaneo (bloqueo por ubicación/SKU incorrecto)
  **no está verificada hoy** con evidencia de test dedicado.

---

## Épica 4: Auditoría y trazabilidad

### US-CORE-01: Kardex de movimientos inmutable — 🟡

**Como** auditor, **quiero** consultar el historial de un LPN, **para** trazar quién lo movió.

- 🟡 Existen el módulo `audit` y `AuditController` (`/api/v1/audit`), con eventos de dominio
  publicados por un bridge `@Async` **en el mismo proceso**. Limitaciones reales:
  - consistencia **eventual** (no transaccional);
  - la persistencia de eventos tiene 1 test `@Disabled` (evento de orden cancelada);
  - lotes y movimientos aún no persisten en BD (ver `CURRENT_STATE.md` §5) → el kardex
    completo de esos movimientos no existe todavía.

---

## Historias sin implementar (roadmap, no backlog activo)

Estas aparicion en documentos históricos como "completadas" — **no lo están**:

- 🔴 Recepción ciega con UI de escáner.
- 🔴 UI de cold-chain (`/api/v1/monitoring/*` sin Angular; datos en memoria).
- 🔴 Idempotencia de API end-to-end (test `@Disabled` por hang).
- 🔴 Migración de datos existentes a schemas multi-tenant en producción.
