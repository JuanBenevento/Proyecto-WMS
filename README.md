# WMS Enterprise — Warehouse Management System

<div align="center">

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-green.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-20-red.svg)](https://angular.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)

**Sistema de Gestión de Almacenes con arquitectura hexagonal y DDD**

Estado real verificado: **[`docs/CURRENT_STATE.md`](docs/CURRENT_STATE.md)** (última verificación: 2026-10-21)

</div>

---

## Descripción

WMS es un backend Spring Boot (Java 21) con frontend Angular para gestión de almacenes:
catálogo de productos, ubicaciones, inventario, órdenes con flujo de estados
(crear → confirmar → pick → pack → ship → deliver), picking, dashboard de KPIs,
monitoreo de cadena de frío y aislamiento multi-tenant por schema (SaaS, fail-closed).

> **Transparencia:** el sistema está en desarrollo activo. Hay módulos completos y módulos
> parciales o en esqueleto (p. ej. lotes/movimientos y cold-chain aún no persisten en BD).
> El estado por módulo con evidencia está en [`docs/CURRENT_STATE.md`](docs/CURRENT_STATE.md);
> no creas ningún README que diga "v1.0 completado" ni porcentajes de cobertura inventados.

---

## Quick Start (verificado 2026-10-21)

### Requisitos

- Docker con Docker Compose (plugin v2: el comando es `docker compose`, con espacio)

### Levantar todo

```bash
git clone https://github.com/JuanBenevento/Proyecto-WMS.git
cd Proyecto-WMS

docker compose up -d        # PostgreSQL + Backend + Frontend
docker compose ps           # los 3 servicios deben quedar "healthy"
```

**Una vez levantado:**

| Servicio | URL | Credenciales |
|----------|-----|--------------|
| Frontend | http://localhost:4200 | `superadmin` / `root123` |
| API | http://localhost:8080/api/v1 | — |
| Swagger UI | http://localhost:8080/swagger-ui.html | — |
| OpenAPI JSON | http://localhost:8080/v3/api-docs | — |
| Health | http://localhost:8080/actuator/health | — |
| PostgreSQL | localhost:5432 (db `wms_db`) | ver `docker-compose.yml` |

**Login (probado el 2026-10-21 — el campo es `username`, no `email`):**

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"superadmin","password":"root123"}'
# → 200 + JWT
```

> La única credencial sembrada es `superadmin/root123` (la crea `DataInitializer` al
> arrancar, de forma idempotente). Documentos viejos que mencionan `admin123` están equivocados.

**Detener:**

```bash
docker compose down        # detiene servicios (conserva datos)
docker compose down -v     # detiene y ELIMINA los volúmenes de BD
```

---

### Arranque manual (NO verificado de punta a punta — best effort)

Si prefieres correr el backend sin Docker:

- Java 21 + `./mvnw` (o `mvnw.cmd` en Windows)
- PostgreSQL 16 corriendo con database `wms_db`
- Variable `DB_PASSWORD` **obligatoria** (no tiene default: `application.properties` usa `${DB_PASSWORD}`)
- El datasource por defecto siempre es PostgreSQL (`jdbc:postgresql://localhost:5432/wms_db`).
  H2 no es el datasource de desarrollo — el perfil `dev` solo desactiva el aislamiento
  multi-tenant y aporta un JWT secret de desarrollo.

```bash
export DB_PASSWORD=...
./mvnw spring-boot:run
```

Este camino **no está verificado hoy** de punta a punta; el camino verificado es `docker compose up -d`.

---

## Funcionalidades y madurez real

| Área | Estado | Evidencia / limitación |
|------|--------|------------------------|
| Auth JWT + usuarios | ✅ | Login verificado en vivo 2026-10-21 |
| Catálogo de productos | ✅ | Persiste en JPA (Flyway V2) |
| Inventario (stock) | ✅ | `InventoryPersistenceAdapter` JPA real |
| Órdenes (flujo de estados) | 🟡 | Flujos probados; evento de orden cancelada con 1 test `@Disabled` |
| Lotes y movimientos | 🔴 | **No persisten**: `LotJpaAdapter`/`MovementJpaAdapter` son `HashMap` en memoria (planificado: feature B) |
| Picking | 🟡 | Lógica completa; sesiones en memoria |
| Cold chain | 🔴 | Scaffold en memoria; sin UI |
| Multi-tenant (aislamiento por schema) | ✅ | Fail-closed, health indicator honesto (feature A, 2026-10-21) |
| Dashboard KPIs | 🟡 | Endpoints presentes; cobertura de datos no verificada |
| Frontend Angular | 🟡 | 133 TS, solo 2 specs |
| E2E Playwright | 🟡 | 7 specs; requiere `npm install` en `e2e/` |

Detalle completo por módulo (COMPLETE / PARTIAL / SCAFFOLD / no verificado): [`docs/CURRENT_STATE.md`](docs/CURRENT_STATE.md).

---

## Estructura del proyecto

```
Proyecto-WMS/
├── src/main/java/com/juanbenevento/wms/
│   ├── audit/           # Auditoría y eventos
│   ├── catalog/         # Productos
│   ├── identity/        # Auth, usuarios, onboarding SaaS (SaaSController)
│   ├── inventory/       # Stock, picking (PickingService/PickingController)
│   ├── monitoring/      # Cadena de frío
│   ├── orders/          # Órdenes y flujo de estados
│   ├── warehouse/       # Ubicaciones y layout
│   └── shared/          # Tenant isolation, seguridad, dashboard (api/DashboardController)
├── src/main/resources/
│   └── db/migration/    # Flyway (V1 tenant_schemas, V2 application tables)
├── src/test/            # 38 archivos de test (JUnit 5 + Testcontainers)
├── wms-frontend/        # Angular 20
├── e2e/                 # Playwright (API + UI)
├── docs/                # Documentación (ver sección final)
├── odd/tasks/           # Fuente de verdad de features (specs + tasks + log)
├── docker-compose.yml   # Dev: postgres + backend + frontend (verificado)
└── docker-compose.prod.yml  # ⚠️ roto — ver docs/CURRENT_STATE.md §4
```

---

## API

Bases de rutas verificadas contra los controllers (el detalle de cada endpoint está en Swagger):

| Controller | Base |
|-----------|------|
| Auth | `/api/v1/auth` |
| Users | `/api/v1/users` |
| SaaS (onboarding tenants) | `/api/v1/saas` |
| Products | `/api/v1/products` |
| Inventory (+ movimientos + picking) | `/api/v1/inventory` |
| Orders | `/api/v1/orders` |
| Cold chain | `/api/v1/monitoring` |
| Dashboard | `/api/v1/dashboard` |
| Audit | `/api/v1/audit` |
| Locations | `/api/v1/locations` |
| Warehouse layout | `/api/v1/warehouse/layout` |

Con el stack levantado: **http://localhost:8080/swagger-ui.html**

---

## Testing

```powershell
# Backend — requiere Docker (los tests de integración usan Testcontainers)
.\mvnw.cmd clean verify
```

Resultado del último run verificable (2026-10-21, con stack Docker corriendo):

```
Tests run: 317, Failures: 0, Errors: 0, Skipped: 2 — BUILD SUCCESS (~2:20 min)
```

- Los 2 skipped son tests `@Disabled` justificados: idempotencia concurrente (hang) y
  evento de orden cancelada (rediseño pendiente).
- **No hay medición de cobertura**: JaCoCo no está en el `pom.xml`. Comandos tipo
  `mvn jacoco:report` fallan. Cualquier % de cobertura que aparezca en otro doc es falso.

```bash
# Frontend (casi sin specs: solo 2)
cd wms-frontend && npm install && npm test

# E2E Playwright (requiere el stack levantado y npm install)
cd e2e && npm install
npm run test:api       # API tests
npm run test:ui-suite  # UI tests
```

---

## Deployment

| Camino | Estado |
|--------|--------|
| `docker compose up -d` (dev) | ✅ Verificado 2026-10-21 (3 servicios healthy, Flyway V1+V2, health OK) |
| `docker-compose.prod.yml` | ⚠️ Roto: referencia `monitoring/prometheus.yml` inexistente + env vars Resilience4j mal formadas |
| Kubernetes / Helm | ❌ No instalable: `k8s/` no tiene `templates/`, imagen del deployment ≠ imagen publicada por CI |

No hay ningún comando Helm funcional hoy — si ves `helm install ...` en otro documento, es falso.
Detalle y evidencia: [`docs/CURRENT_STATE.md`](docs/CURRENT_STATE.md) §4.

---

## Configuración

| Variable | Descripción | Default |
|----------|-------------|---------|
| `DB_PASSWORD` | Password de PostgreSQL. **Obligatoria** para arrancar | *(sin default)* |
| `JWT_SECRET_KEY` | Secret JWT (>= 32 bytes; en prod se recomienda Base64). En `dev` existe un default local | prod: requerida; dev: default |
| `JWT_EXPIRATION` | Expiración del JWT (ms) | `86400000` |
| `WMS_TENANT_ISOLATION_ENABLED` | Aislamiento multi-tenant por schema (fail-closed). En `dev` se fuerza `false` localmente | `true` (base); `false` (perfil dev) |

**Perfiles:** `dev` (aislamiento OFF local, JWT de desarrollo), `staging` (existe; contenido no verificado hoy),
`prod` (JWT obligatorio, aislamiento ON por defecto).

---

## Documentación

- 📍 **[`docs/CURRENT_STATE.md`](docs/CURRENT_STATE.md)** — estado real verificado (empezá por acá)
- 🏗️ [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — arquitectura
- 📋 [docs/FUNCTIONAL_REQS.md](docs/FUNCTIONAL_REQS.md) — requerimientos funcionales
- 🗺️ [docs/ROADMAP.md](docs/ROADMAP.md) — roadmap
- 📘 [docs/TECHNICAL_SPEC.md](docs/TECHNICAL_SPEC.md) — especificación
- 🤝 [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md) — guía de contribución

---

## Contribuir

1. Fork y rama feature desde `dev` (`git checkout -b feature/mi-feature dev`)
2. Commits con convención [Conventional Commits](https://www.conventionalcommits.org/) (sin atribuciones de IA en el mensaje)
3. PR hacia `dev`; el flujo completo de issues/labels está en `docs/CONTRIBUTING.md`

---

## Licencia

El repositorio aún **no incluye un archivo `LICENSE`**. Definir la licencia es un pendiente del autor.

---

## Autor

**Juan Manuel Benevento**
- GitHub: [@JuanBenevento](https://github.com/JuanBenevento)
- LinkedIn: [juanbenevento](https://www.linkedin.com/in/juan-manuel-benevento-1870b5216/)
