# Plan #1: Base Sólida y Profesional

## metadata
- **Inicio:** 2025-04-15
- **Rama:** refactor/rest-api-standards
- **Desde:** dev
- **Estado:** En Progreso

---

## Resumen de Cambios

| Fase | Descripcion | Estado | Tickets |
|------|-------------|--------|---------|
| 1 | Estandarización de endpoints REST | 🔄 En progreso | - |
| 2 | Constantes y valores mágicos | ⏳ Pendiente | - |
| 3 | Refactoring de dominio | ⏳ Pendiente | - |
| 4 | Seguridad básica | ⏳ Pendiente | - |
| 5 | Documentación | ⏳ Pendiente | - |

---

## Fase 1: Estandarización de Endpoints REST

### Objetivo
Estandarizar todos los endpoints según convención REST:
- `GET /resources` - Listar todos
- `GET /resources/{id}` - Obtener uno
- `POST /resources` - Crear
- `PUT /resources/{id}` - Actualizar
- `DELETE /resources/{id}` - Eliminar

### Cambios Identificados

| Archivo | Endpoint Actual | Endpoint Nuevo | Tipo Cambio |
|---------|-----------------|----------------|-------------|
| `InventoryController.java` | `GET /api/v1/inventory/getAll` | `GET /api/v1/inventory` | Refactor |
| `InventoryController.java` | `GET /suggest-location` retorna `String` | `GET /suggest-location` retorna DTO | Fix |
| `WarehouseLayoutController.java` | `GET /warehouse/layout/getLayout` | `GET /api/v1/warehouse/layout` | Refactor |
| `WarehouseLayoutController.java` | `POST /warehouse/layout/saveLayout` | `PUT /api/v1/warehouse/layout` | Refactor |

### Commits Realizados

| Hash | Mensaje |
|------|---------|
| - | - |

---

## Fase 2: Constantes y Valores Mágicos

### Objetivos
- Extraer magic strings/numbers a constantes nombradas
- Crear clase `WmsConstants` con prefijos y valores comunes

### Constantes Identificadas

| Ubicacion | Valor | Constante Propuesta | Estado |
|-----------|-------|---------------------|--------|
| `InboundService.java:76` | `"LPN-"` | `LPN_PREFIX` | ⏳ |
| `PickingService.java:95` | `"PICK-"` | `PICK_PREFIX` | ⏳ |
| `application.properties:15` | `86400000` | `JWT_EXPIRATION_24H` | ⏳ |

---

## Fase 3: Refactoring de Dominio

### Objetivos
- Value Objects para LPN y Batch
- Inmutabilidad en modelos
- Remover setters públicos en InventoryItem

### Tickets
- [ ] Crear `Lpn.java` Value Object
- [ ] Crear `BatchNumber.java` Value Object
- [ ] Actualizar `InventoryItem.java` con inmutabilidad
- [ ] Actualizar mapeos JPA

---

## Fase 4: Seguridad Básica

### Objetivos
- Remover password hardcodeado en `DataInitializer`
- Hacer CORS configurable por ambiente
- Cambiar `ddl-auto=create` a `ddl-auto=create-drop` para dev

### Vulnerabilidades a Corregir

| Severidad | Archivo | Issue | Ticket |
|-----------|---------|-------|--------|
| 🔴 CRITICAL | `DataInitializer.java:36` | Password hardcodeado | - |
| 🔴 CRITICAL | `application.properties:6` | `ddl-auto=create` | - |
| 🟠 HIGH | `CorsConfig.java:17` | CORS hardcodeado | - |

---

## Fase 5: Documentación

### Objetivos
- README.md profesional completo
- CONTRIBUTING.md con convenios
- Actualizar ARCHITECTURE.md

### Checklist
- [ ] README.md con Quick Start
- [ ] CONTRIBUTING.md con conventional commits
- [ ] API documentation en Swagger
- [ ] ADR-002: Decisiones de refactor

---

## Definition of Done

- [ ] Todos los endpoints estandarizados REST
- [ ] 0 magic numbers/constants en código
- [ ] Secrets en variables de ambiente
- [ ] Tests pasando
- [ ] README.md 100% completo
- [ ] Conventional commits en todo
