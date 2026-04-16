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
| 1 | Estandarización de endpoints REST | ✅ Completado | - |
| 2 | Constantes y valores mágicos | ✅ Completado | - |
| 3 | Refactoring de dominio | 🔄 En progreso | - |
| 4 | Seguridad básica | ✅ Completado (en rama separada) | - |
| 5 | Documentación | ⏳ Pendiente | - |

---

## Fase 1: Estandarización de Endpoints REST ✅

### Commits Realizados

| Hash | Mensaje |
|------|---------|
| `6c43328` | refactor(api)!: estandarizar endpoints REST según convención |
| `1f388b2` | feat(frontend): adaptar servicios a nuevos endpoints REST |

---

## Fase 2: Constantes y Valores Mágicos ✅

### Commits Realizados

| Hash | Mensaje |
|------|---------|
| `f336685` | refactor(core): extract magic strings to WmsConstants |

### Constantes Extraídas

| Constante | Valor | Archivos |
|-----------|-------|----------|
| `WmsConstants.LPN_PREFIX` | `"LPN-"` | InboundService, AuditEventListener, tests |
| `WmsConstants.PICK_PREFIX` | `"PICK-"` | PickingService |
| `WmsConstants.LPN_UNKNOWN` | `"LPN-UNKNOWN"` | AuditEventListener |
| `WmsConstants.LPN_VARIOUS` | `"VARIOUS"` | AuditEventListener |

---

## Fase 3: Refactoring de Dominio 🔄

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

## Fase 4: Seguridad Básica ✅ (rama separada)

### Ramas Mergeadas/Por Merge

| Rama | Commit | Descripcion | Status |
|------|--------|-------------|--------|
| `fix/ddl-auto-security` | `ce8be8a` | Profiles de ambiente para DDL-auto | PR pendiente |
| `feature/picking-idempotency` | `8435971` | Idempotencia para operaciones | PR pendiente |

---

## Fase 5: Documentación ⏳

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

- [x] Todos los endpoints estandarizados REST
- [x] 0 magic numbers/constants en código
- [x] Secrets en variables de ambiente (profiles)
- [ ] Tests pasando
- [ ] README.md 100% completo
- [x] Conventional commits en todo
