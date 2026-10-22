# Feature: Documentación Honesta del Proyecto (Camino D)

- **Objetivo**: Una documentación completa y fehaciente del WMS: que cada documento ciente lo que el código realmente hace, con evidencia, sin métricas inventadas ni estados inflados.
- **Problema**: Los documentos declaran "WMS Enterprise v1.0 Completado, ~80% cobertura, 185+ tests" — falso en cobertura (sin JaCoCo), falso en completitud (persistencia en HashMap, aislamiento roto, despliegue no funcionando). El autoengaño documental guía mal a cualquier humano o agente futuro.
- **Por qué**: Pedida explícitamente por el usuario ("genera una documentacion completa y fehaciente de todo el proyecto"). Va en paralelo al Camino A; es barata y evita que el próximo lector se guíe por premisas falsas.
- **Alcance**: `README.md`, `docs/ARCHITECTURE.md`, `docs/FUNCTIONAL_REQS.md`, `docs/ROADMAP.md`, `ANALISIS_DISENO.md`, `docs/PLAN_*.md`, `docs/AUDIT_2025.md`, marcado de `openspec/changes/inventory-movement/tasks.md`. Nuevo `docs/CURRENT_STATE.md como fuente única de verdad verificada.
- **Fuera de alcance**: Código de la aplicación (excepto agregar JaCoCo solo si el usuario lo pide como parte de C), infraestructura, features nuevas.
- **Restricciones**: Rama `docs/honest-project-documentation` desde `dev`. Toda afirmación numérica o de estado debe tener evidencia (comando, archivo, o referencia al análisis #611). Cuando algo no se pudo verificar hoy, se marca como "no verificado" en vez de afirmarse.

## Specs

### SD1 — CURRENT_STATE.md como fuente única de verdad

- **Criterio de aceptación**: Existe `docs/CURRENT_STATE.md` con: stack verificado, módulos y su madurez real (COMPLETE/PARTIAL/SCAFFOLD/BROKEN con ruta de evidencia), estado de tests real (números del último run verificable), riesgos abiertos, y fecha de última verificación. Todo extraído del análisis #611 o de comandos re-ejecutados al escribir el doc.
- **Checks**: cada sección con ruta de archivo o comando citado; cero afirmaciones sin evidencia.

### SD2 — README sin promesas falsas

- **Criterio de aceptación**: El README describe qué hace el sistema hoy (flujos reales), cómo levantarlo (pasos que funcionan: incluye `DB_PASSWORD`, Docker, puerto), credenciales reales (`DataInitializer` siembra `root123` — verificar y documentar la verdad), y quita o corrige: "v1.0 Completado", cobertura inventada, `helm install wms ./charts/wms` (la carpeta `charts/` no existe).
- **Checks**: seguir el README desde cero en una terminal limpia → el sistema levanta siguiendo sus propias instrucciones (o el README dice explícitamente qué falta).

### SD3 — ARCHITECTURE.md sin duplicados y actualizada

- **Criterio de aceptación**: Se elimina la sección 3 duplicada; los diagramas de paquetes coinciden con el árbol real bajo `src/main/java/com/juanbenevento/wms/`; los patrones declarados (hexagonal, tenants por schema) se describen como son hoy, incluyendo las limitaciones conocidas (placeholder interceptor, flags inertes — hasta que la feature `tenant-isolation-and-schema` las cierre, con nota de fecha).
- **Checks**: diff de estructura de paquetes doc vs árbol real sin divergencias.

### SD4 — FUNCTIONAL_REQS.md alineada al código actual

- **Criterio de aceptación**: Reemplaza el documento de 2025-11-27 por requisitos que reflejen el código de 2026: inbound, putaway, inventory (con limits de lotes en memoria hasta la feature B), picking, outbound, cold chain, dashboard, multi-tenant. Cada requisito marca su estado real: implemented / partial / stub (en memoria) / missing.
- **Checks**: cada feature mencionada tiene al menos un controlador, servicio o test que la respalde; las marcadas "stub" corresponden a los `HashMap` conocidos.

### SD5 — ROADMAP desinflado y accionable

- **Criterio de aceptación**: Elimina "v1.0 Completado" y "~80% cobertura". Reemplaza por: hecho-verificado (con evidencia), en-progreso, y pendiente. Incluye los caminos A→B→C+D con estado. Nota: JaCoCo no está en el `pom.xml` — el ROADMAP no puede citar cobertura hasta que exista.
- **Checks**: cero porcentajes sin herramienta de medición referenciada.

### SD6 — Documentos históricos marcados, no borrados

- **Criterio de aceptación**: `ANALISIS_DISENO.md` (autocalificación 8.5/10), `docs/PLAN_1_*.md`, `docs/PLAN_2_*.md`, `docs/AUDIT_2025.md` reciben un encabezado visible: "DOCUMENTO HISTÓrico — superado por docs/CURRENT_STATE.md (fecha)". No se reescriben enteros salvo corrección de errores que confundan.
- **Checks**: grep de encabezado histórico presente en cada uno.

### SD7 — openspec/inventory-movement/tasks.md conciliado

**Cita del análisis (verbatim)**: `openspec/changes/inventory-movement/tasks.md: 47/47 tareas sin marcar aunque el código existe (y nombra LotRepositoryPort vs LotRepository real)`

- **Criterio de aceptación**: Las tareas cuyo código existe en el árbol se marcan completadas; las que no, quedan abiertas con nota. Los nombres de clases se corrigen a los reales.
- **Checks**: ninguna tarea marcada completa sin archivo de código correspondiente.

## Tasks

| ID | Spec | Ruta | Commit |
|----|------|------|--------|
| TD1 | SD1 | inline | ✅ — `docs/CURRENT_STATE.md` escrito desde #611 **re-verificado** con comandos del día (suite 317/0/0/2, stack healthy, JaCoCo=0, charts/templates/prometheus inexistentes, 2 @Disabled, credenciales root123). Tabla de madurez con COMPLETE/PARTIAL/SCAFFOLD/no-verificado + evidencia por fila. Ver L5. |
| TD2 | SD2 | inline | ✅ — `README.md` reescrito (455 → ~250 líneas): credenciales `superadmin/root123` (antes `admin123`, falso), Quick Start con `docker compose` verificado + ejemplo de login real, tabla de madurez honesta, testing con el resultado real 317/0/0/2 y "no hay JaCoCo", deployment marcando prod-compose y k8s como rotos, sin badge de licencia/build, sin `helm` ni H2 como datasource. Además se corrigieron rutas de paquetes en `CURRENT_STATE.md` (picking/dashboard/super-admin NO son top-level) y se eliminó la propiedad muerta `wms.tenant.isolation.rls.enabled` de `application-dev.properties` (nadie la lee en Java). Ver L6. |
| TD3 | SD3 | inline | ✅ — `docs/ARCHITECTURE.md` reescrito (284 → ~230 líneas): sección §3 duplicada eliminada, mermaid con fences reales (antes no renderizaba), topología real (monolito modular, sin gateway/broker/OAuth2/virtual-threads — verificado con grep), stack con SB 3.4.1, diagrama de dominio solo con clases existentes (sin `PickingTask`/`FEFOStrategy`/`PutAwayService`), y **ADR-004 corregido**: ya no declara "✅ COMPLETADO Fases 1-8" con archivos RLS eliminados — ahora documenta el mecanismo post-A real + RLS diferido (S7). Ver L7. |
| TD4 | SD4 | inline | (pendiente) — Reescribir `docs/FUNCTIONAL_REQS.md` con estados implemented/partial/stub/missing |
| TD5 | SD5 | inline | (pendiente) — Reescribir `docs/ROADMAP.md` desinflado con A→B→C+D |
| TD6 | SD6 | inline | (pendiente) — Agregar encabezado histórico a ANALISIS_DISENO.md, PLAN_*.md, AUDIT_2025.md |
| TD7 | SD7 | inline | (pendiente) — Conciliar `openspec/changes/inventory-movement/tasks.md` contra el árbol real |

## Log

- **L1** (2026-10-21, solicitud original del usuario, verbatim): `Nesecito que hagamos ODD. Pero como primer paso quiero que analisemos por completo de que va el proyecto, como funciona cada parte para poder determinar como retomar su desarrollo y que camino vamos a tomar para continuar con el mismo`
- **L2** (2026-10-21, solicitud de documentación, verbatim): `3. Si reemplaza, y si es posible genera una documentacion completa y fehaciente de todo el proyecto.`
- **L3** (2026-10-21, fuente de verdad): Análisis #611 (`sdd/project-recovery-analysis/explore`). Toda cifra de CURRENT_STATE.md debe rastrearse a ese análisis o a un comando re-ejecutado al momento de escribir.
- **L4** (2026-10-21, decisión de alcance): Esta feature es D del camino A→B→C+D y corre en paralelo a `tenant-isolation-and-schema`. Cuando A cierre, los docs que describan el aislamiento deben actualizarse para reflejar el estado post-fix (SD3 lo prevé con nota de fecha).
- **L5** (2026-10-21, secuencia acordada + TD1): El usuario eligió la opción (a): **primero merge de A** (PR #44 → merge commit `813be57` en `dev`, issue #43 cerrado) y luego D desde `dev` ya corregido — para que `CURRENT_STATE.md` documente la realidad arreglada y no un estado a punto de borrar (recomendación del asistente aceptada: "sigue tu recomendación: a"). Rama `docs/honest-project-documentation` creada desde `dev@813be57`. TD1 completado: `docs/CURRENT_STATE.md` con evidencia re-ejecutada el mismo día. **Diferencias post-A detectadas vs #611**: aislamiento ahora COMPLETE (fue ROTO), compose dev ahora FUNCIONA (7 bugs corregidos), `@Disabled` bajó de 3 a 2 (TenantIsolation reactivado), credenciales reales = `superadmin/root123` (el seed SQL con `admin123` fue eliminado), `ci-cd.yml` pasado a `workflow_dispatch` (`fdac9bd`).
- **L6** (2026-10-21, TD2 + correcciones de evidencia): README reescrito con solo hechos verificados el mismo día. La pasada de verificación expuso **3 imprecisiones propias de TD1** que se corrigieron: (1) `picking`, `dashboard` y `super-admin` NO son paquetes top-level — viven en `inventory` (`PickingController`/`PickingService`), `shared/api` (`DashboardController`) e `identity` (`SaaSController`) respectivamente; top-level real = 8 paquetes (audit, catalog, identity, inventory, monitoring, orders, shared, warehouse); (2) faltaban paths completos en el riesgo #1; (3) `application-dev.properties` aún contenía `wms.tenant.isolation.rls.enabled=false` — propiedad muerta (la claim de T7 de "rls eliminadas" cubrió base+compose pero no el perfil dev); línea eliminada. **Gotcha documentado**: `LICENSE` no existe en el repo → se quitó el badge MIT del README; H2 figura en el `pom.xml` pero el datasource por defecto del runtime es siempre PostgreSQL (`wms_db`, `${DB_PASSWORD}` sin default); springdoc sí está presente (Swagger UI real en `/swagger-ui.html`).
- **L7** (2026-10-21, TD3): `ARCHITECTURE.md` reescrito. Además del alcance de SD3 (§3 duplicado, árbol), se corrigió **ADR-004**, que era el punto más peligroso del documento: afirmaba "✅ COMPLETADO (Fases 1-8)" listando `V__enable_rls_*.sql`, `SearchPathConnectionInterceptor.java` y `SchemaIsolationValidator.java` — archivos que la feature A eliminó. Ahora documenta el mecanismo real post-A (wrapper `TenantAwareDataSource` fail-closed + health indicator) y deja RLS explícitamente como pendiente (S7). Verificaciones nuevas que sustentan el doc: `PutAwayStrategy` existe con única implementación `SmartPutAwayStrategy` (no hay `FEFOStrategy` ni `PutAwayService` ni `PickingTask`); cero configuración de virtual threads en properties/pom (grep=0); Spring Boot real = 3.4.1 (el doc decía 3.2); sin RabbitMQ/gateway (monolito modular).
