# Roadmap — WMS

> **Actualizado: 2026-10-21** con estado verificado contra el código y la suite
> (`.\mvnw.cmd clean verify` → 317/0/0/2). El detalle con evidencia por módulo está en
> [`docs/CURRENT_STATE.md`](CURRENT_STATE.md). El roadmap anterior ("v1.0 Completado",
> Helm/HPA ✅, "~80% cobertura") era incorrecto y fue reemplazado por este.

---

## Dónde estamos (2026-10-21)

| Área | Estado | Nota |
|------|--------|------|
| Auth JWT + usuarios | ✅ | Login verificado en vivo; seed `superadmin/root123` |
| Catálogo de productos | ✅ | JPA real, Flyway V2 |
| Inventario (stock) | ✅ | `InventoryPersistenceAdapter` JPA real |
| Órdenes (flujo de estados) | ✅ | Workflows verdes; 1 test de evento cancelado `@Disabled` |
| Estrategias de asignación | ✅ | FEFO / FIFO / WeightCertification en `inventory/domain/strategy/` — operan sobre `LotRepository`, ahora persistente en BD (JPA, feature B1) |
| Multi-tenant (schema, fail-closed) | ✅ | Feature A, mergeada 2026-10-21 (PR #44) |
| Layout / ubicaciones | ✅ | Capacidad y zonas implementadas |
| Dashboard KPIs | 🟡 | Endpoints presentes; cobertura de datos no verificada |
| Picking | 🟡 | Lógica completa; sesiones en `ConcurrentHashMap` |
| Auditoría | 🟡 | Bridge `@Async` eventualmente consistente |
| Lotes y movimientos | ✅ | JPA real desde 2026-10-23 (feature B1): `inventory_lots`/`inventory_movements` vía Flyway `V3` |
| Cold chain | 🔴 | Servicio scaffold en memoria; sin UI Angular |
| E2E Playwright | 🟡 | 7 specs; requieren `npm install` en `e2e/` |
| CI en PRs a `dev` | ✅ | `maven.yml` dispara en PRs a `main` **y** `dev` (corregido 2026-10-23, feature D, `cca03c3`) |
| `docker-compose.prod.yml` | 🔴 | Archivo de monitoring inexistente + env vars mal formadas |
| Kubernetes / Helm | 🔴 | `k8s/` sin `templates/`; imagen ≠ CI; no instalable |
| Cobertura de código | 🔴 | Sin JaCoCo: no hay métrica |

---

## Historial de features (fuente de verdad: `odd/tasks/`)

| Feature | Estado | Fecha |
|---------|--------|-------|
| `tenant-isolation-and-schema` (Aislamiento multi-tenant) | ✅ Cerrada | 2026-10-21 (PR #44, issue #43) |
| `honest-project-documentation` (docs verificadas) | ✅ Cerrada | 2026-10-23 (PR #46, issue #45) |
| `real-persistence` (lotes y movimientos, "B1") | ✅ Implementada | 2026-10-23 (rama `feat/real-persistence`, pendiente de PR/merge) |
| Persistencia de sesiones picking/cold-chain ("B2") | ⏳ Pendiente | — |
| Idempotencia + production hardening | ⏳ Pendiente ("C") | — |

---

## Correcciones respecto del roadmap anterior

El roadmap viejo (2026-04-23) marcaba ✅ estas afirmaciones; **son falsas o parciales**:

- "WMS Enterprise v1.0 Completado" → no existe una versión 1.0; hay módulos scaffold.
- "Kubernetes deployment / Helm chart / HPA / Ingress ✅" → `k8s/` no es instalable (sin
  `templates/`); `charts/` no existe.
- "docker-compose.prod.yml ✅" → el archivo existe pero no levanta (referencias rotas).
- "Full CI/CD pipeline ✅" → no hay pipeline que despliegue; `ci-cd.yml` es manual y
  `maven.yml` no cubre PRs a `dev`.
- "Tests 185+ / E2E 20+ / Cobertura ~80%" → hoy: 317 backend (0 fallos, 2 `@Disabled`),
  7 specs E2E, cobertura **no medible**.
- "Cold chain ✅" → servicio en memoria sin UI (scaffold).
- Corrupción de texto en la sección v1.1 ("para继续") → eliminada.

Lo que **sí era cierto** y se conserva: las estrategias de asignación FEFO/FIFO/WeightCertification
existen como código real.

---

## Próximos pasos (orden propuesto)

1. **Feature B2 — Persistencia de sesiones**: migrar sesiones de picking (`PickingService`) y
   lecturas/alertas de cold-chain (`ColdChainMonitorService`) de `ConcurrentHashMap` a JPA/Flyway
   (se pierden al reiniciar). B1 (lotes y movimientos) completada 2026-10-23.
2. **Feature C — Idempotencia y hardening**: rehabilitar `IdempotencyIntegrationTest`
   (hang) y el evento de orden cancelada; reglas idempotentes en la API.
3. **Merge de B1**: `feat/real-persistence` está implementada y verde, pendiente de PR/merge
   a `dev` (decisión abierta del autor).
4. **Decidir destino de prod/k8s**: arreglar `docker-compose.prod.yml` + k8s, **o** retirarlos
   de la documentación hasta que sean utilizables.
5. **RLS** (diferido, decisión S7 de la feature A): solo si aparece requisito de auditoría
   que lo exija.

## Decisiones abiertas

- CI automático en PRs hacia `dev`: ¿habilitar triggers o dejar merge manual con suites locales?
- Licencia del repositorio: no hay archivo `LICENSE`.
- Frontend: ¿subir cobertura de specs (2 hoy) o aceptar el riesgo de UI sin tests?
