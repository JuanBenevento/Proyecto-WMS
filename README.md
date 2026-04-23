# 🏭 WMS Enterprise - Warehouse Management System

<div align="center">

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-green.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-20-red.svg)](https://angular.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Build](https://img.shields.io/badge/Build-Passing-green.svg)]()

**Sistema de Gestión de Almacenes de nivel empresarial con arquitectura hexagonal y DDD**

</div>

---

## 📋 Descripción

WMS Enterprise es un sistema moderno y escalable para la gestión integral de almacenes. Diseñado bajo principios de **Domain-Driven Design (DDD)** y **Arquitectura Hexagonal**, ofrece automatización inteligente de procesos logísticos, trazabilidad total y capacidad de expansión para operaciones de alto volumen.

### ✨ Características Principales

- 📦 **Gestión de Inventario** - Control de stock con lotes, vencimientos y estados
- 🚚 **Inbound/Outbound** - Recepción, put-away, picking y despacho automatizados
- 📋 **Gestión de Órdenes** - Estados extensibles, prioridades, asignación FEFO
- 🔍 **Trazabilidad Total** - Eventos de dominio persistidos para auditoría
- 🔐 **Multi-tenant** - Arquitectura SaaS con aislamiento por empresa
- 🏗️ **Arquitectura Empresarial** - Hexagonal + DDD + Event-Driven
- 📊 **Dashboard KPIs** - Métricas en tiempo real
- 🌡️ **Cold Chain** - Control de temperatura para industria alimentaria
- 📈 **Escalabilidad** - Kubernetes, HPA, Helm charts

---

## 🏗️ Arquitectura

### Stack Tecnológico

| Capa | Tecnología |
|------|-------------|
| **Backend** | Java 21 LTS, Spring Boot 3.4 |
| **Frontend** | Angular 20 |
| **Database** | PostgreSQL 16 |
| **Security** | JWT OAuth2 |
| **Events** | In-Memory Event Bus |

### Modelo Arquitectónico

```
┌─────────────────────────────────────────────────────────────┐
│                      FRONTEND (Angular 20)                │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐        │
│  │ Orders │ │Inventory│ │ Products│ │ Warehouse│        │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘        │
└────────────────────────┬────────────────────────────────────┘
                         │ REST API (JSON)
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   APPLICATION LAYER                        │
│  Use Cases │ Services │ Commands │ DTOs │ Ports         │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                          │
│  Entities │ Value Objects │ Domain Events │ Rules        │
└─────────────────────────────────────────────────────────────┘
                         ▲
                         │
┌────────────────────────┴────────────────────────────────────┐
│                  INFRASTRUCTURE LAYER                      │
│  REST Adapters │ JPA Repositories │ Event Handlers       │
└─────────────────────────────────────────────────────────────┘
```

---

## 🚀 Quick Start

### Prerequisites

- **Docker** 20.10+ with Docker Compose
- **8GB RAM** minimum (recommended 16GB)
- **20GB disk space**

### Option 1: Docker Compose (Recommended for Development)

La forma más rápida de levantar todo el sistema:

```bash
# Clonar repositorio
git clone https://github.com/JuanBenevento/Proyecto-WMS.git
cd Proyecto-WMS

# Levantar todos los servicios (PostgreSQL + Backend + Frontend)
docker-compose up -d

# Ver logs
docker-compose logs -f

# Ver estado de servicios
docker-compose ps
```

**Tiempo de primer arranque:** ~3-5 minutos (depende de descarga de imágenes)

**Una vez levantado:**

| Servicio | URL | Credenciales |
|----------|-----|--------------|
| Frontend | http://localhost:4200 | superadmin / admin123 |
| API | http://localhost:8080/api/v1 | - |
| Swagger | http://localhost:8080/swagger-ui.html | - |
| Health | http://localhost:8080/actuator/health | - |

**Detener:**
```bash
docker-compose down                    # Detiene servicios
docker-compose down -v               # Detiene Y elimina datos
```

---

### Option 2: Manual Setup (Desarrollo Local)

#### Prerequisites

- **Java** 21 LTS
- **Maven** 3.9+
- **Node.js** 20+
- **PostgreSQL** 16+
- **H2** (development/tests)

#### Installation

```bash
# Clone repository
git clone https://github.com/JuanBenevento/Proyecto-WMS.git
cd Proyecto-WMS

# Backend - Development (H2 in-memory)
./mvnw spring-boot:run

# Backend - Production
export DB_URL=jdbc:postgresql://localhost:5432/wms
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
export JWT_SECRET_KEY=your-secret-key
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Frontend
cd wms-frontend
npm install
npm start
```

### Acceso

| Servicio | URL | Credenciales default |
|----------|-----|---------------------|
| API REST | http://localhost:8080/api/v1 | - |
| Swagger | http://localhost:8080/swagger-ui.html | - |
| Frontend | http://localhost:4200 | superadmin / admin123 |

---

## 📁 Estructura del Proyecto

```
Proyecto-WMS/
├── src/
│   ├── main/
│   │   └── java/com/juanbenevento/wms/
│   │       ├── catalog/          # Gestión de Productos
│   │       ├── identity/         # Autenticación y Usuarios
│   │       ├── inventory/        # Core de Inventario
│   │       ├── orders/          # Gestión de Órdenes
│   │       ├── warehouse/        # Ubicaciones y Layout
│   │       ├── audit/           # Auditoría y Eventos
│   │       └── shared/         # Código Compartido
│   │           ├── domain/       # Value Objects, Excepciones
│   │           └── infrastructure/
│   └── test/                    # Tests Unitarios e Integrados
├── wms-frontend/               # Angular Application
│   └── src/app/
│       ├── core/               # Auth, Guards, Interceptors
│       └── modules/           # Feature Modules
│           ├── orders/
│           ├── inventory/
│           ├── products/
│           └── warehouse/
└── docs/                       # Documentación
    ├── TECHNICAL_SPEC.md       # Especificación Técnica
    ├── ARCHITECTURE.md         # Arquitectura Detallada
    ├── FUNCTIONAL_REQS.md     # Requerimientos Funcionales
    └── CONTRIBUTING.md         # Guía de Contribución
```

---

## 📊 Funcionalidades

### Módulos Implementados

| Módulo | Descripción | Estado |
|--------|-------------|--------|
| **Productos** | Catálogo con dimensiones, peso, perfil logístico | ✅ |
| **Ubicaciones** | Layout jerárquico con capacidades | ✅ |
| **Inventario** | Stock, lotes, vencimientos, estados | ✅ |
| **Recepción** | Recepción ciega, LPN, QC, put-away | ✅ |
| **Órdenes** | Estados, prioridades, transiciones | ✅ |
| **Picking** | Asignación FEFO, short picks, validación | ✅ |
| **Auditoría** | Eventos de dominio persistidos | ✅ |
| **Autenticación** | JWT, multi-tenant, roles | ✅ |
| **Dashboard** | KPIs y métricas | ✅ |
| **Cold Chain** | Monitoreo de temperatura | ✅ |
| **E2E Tests** | Playwright tests | ✅ |

### Flujo de Operaciones

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  RECEPCIÓN  │───>│  PUT-AWAY   │───>│   ALMACÉN   │───>│  ASIGNACIÓN │
│  (Inbound) │    │             │    │   (Stock)   │    │  (FEFO)     │
└─────────────┘    └─────────────┘    └─────────────┘    └──────┬──────┘
                                                                    │
                                                                    ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  DELIVERED  │<───│   SHIPPED   │<───│   PACKED    │<───│  PICKING    │
│  (Entrega)  │    │  (Despacho)│    │ (Empaque)   │    │ (Recolección)│
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

---

## 🔌 API Endpoints

### Orders Management
```
POST   /api/v1/orders              # Crear orden
GET    /api/v1/orders              # Listar órdenes
GET    /api/v1/orders/{id}        # Obtener orden
POST   /api/v1/orders/{id}/confirm   # Confirmar
POST   /api/v1/orders/{id}/cancel    # Cancelar
POST   /api/v1/orders/{id}/hold      # Poner en espera
POST   /api/v1/orders/{id}/pick      # Iniciar picking
POST   /api/v1/orders/{id}/pack      # Empaquetar
POST   /api/v1/orders/{id}/ship      # Enviar
POST   /api/v1/orders/{id}/deliver    # Entregar
```

### Inventory
```
POST   /api/v1/inventory/receive     # Recibir mercancía
POST   /api/v1/inventory/put-away   # Ubicar en almacén
POST   /api/v1/inventory/move       # Mover stock
GET    /api/v1/inventory            # Consultar stock
```

### Picking
```
POST   /api/v1/picking/start        # Iniciar picking
POST   /api/v1/picking/pick-line   # Registrar pick
POST   /api/v1/picking/complete     # Completar picking
```

### Warehouse
```
GET    /api/v1/layout               # Obtener layout
POST   /api/v1/layout             # Guardar layout
GET    /api/v1/locations          # Listar ubicaciones
POST   /api/v1/locations          # Crear ubicación

### Dashboard & KPIs
```
GET    /api/v1/dashboard/kpis              # All KPIs
GET    /api/v1/dashboard/metrics/orders  # Order statistics
GET    /api/v1/dashboard/metrics/warehouse # Warehouse utilization
GET    /api/v1/dashboard/activity      # Recent activity
```

### Cold Chain Monitoring
```
POST   /api/v1/monitoring/temperature           # Record temperature
GET    /api/v1/monitoring/alerts                  # Active alerts
GET    /api/v1/monitoring/alerts/{location}        # Alerts by location
GET    /api/v1/monitoring/temperature/history/{location} # Temperature history
POST   /api/v1/monitoring/alerts/{id}/acknowledge # Acknowledge alert
```
```

---

## 🧪 Testing

```bash
# Backend tests
./mvnw test

# Frontend tests
cd wms-frontend
npm test

# Coverage report
./mvnw test jacoco:report

# E2E Tests (requires running services)
cd e2e
npm install
npm run test        # All tests
npm run test:api   # API tests only
npm run test:ui-suite  # UI tests only
```

### Test Coverage

| Layer | Tests | Coverage |
|-------|-------|----------|
| Domain | 80+ | 95% |
| Application | 60+ | 85% |
| Infrastructure | 45+ | 70% |
| **E2E** | 20+ | API + UI |
| **Total** | **200+** | **~80%** |

---

## ☁️ Deployment

### Docker Compose (Development)
```bash
docker-compose up -d
```

### Docker Compose (Production)
```bash
cp .env.production.example .env
# Edit .env with production values
docker-compose -f docker-compose.prod.yml up -d
```

### Kubernetes (Production)
```bash
# Deploy with Helm
helm install wms ./k8s/values.yaml

# Or apply directly
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/config.yaml
kubectl apply -f k8s/ingress.yaml
```

---

## 🔧 Configuration

### Environment Variables

| Variable | Descripción | Default |
|----------|-------------|---------|
| `DB_URL` | JDBC URL de PostgreSQL | jdbc:h2:mem:testdb |
| `DB_USERNAME` | Usuario de DB | sa |
| `DB_PASSWORD` | Password de DB | (empty) |
| `JWT_SECRET_KEY` | Clave para JWT | (required) |
| `JWT_EXPIRATION` | Expiración JWT (ms) | 86400000 |

### Profiles

| Profile | Uso | Características |
|---------|-----|-----------------|
| `dev` | Desarrollo local | H2, create-drop, logs verbose |
| `staging` | Pre-producción | PostgreSQL, validate, logs moderados |
| `prod` | Producción | PostgreSQL, none, secure, sin swagger |

---

## 📖 Documentación

- 📘 [Especificación Técnica](docs/TECHNICAL_SPEC.md) - Visión, alcance, casos de uso
- 🏗️ [Arquitectura](docs/ARCHITECTURE.md) - Diagramas, patrones, decisiones
- 📋 [Requerimientos](docs/FUNCTIONAL_REQS.md) - Historias de usuario
- 🤝 [Contribución](docs/CONTRIBUTING.md) - Guidelines para PRs

---

## 🚢 Deployment

### Docker Compose (Desarrollo y Staging)

El proyecto incluye `docker-compose.yml` listo para usar:

```bash
# Desarrollo completo (PostgreSQL + Backend + Frontend)
docker-compose up -d

# Con rebuild si hay cambios
docker-compose up --build -d

# Ver todos los logs
docker-compose logs -f backend

# Escalar backend (producción)
docker-compose up -d --scale backend=3
```

### Kubernetes (Producción)

```bash
# Deploy con Helm
helm install wms ./charts/wms \
  --set database.host=postgres.prod.svc \
  --set jwt.secretKey=$JWT_SECRET_KEY

# O usando docker-compose con orchestador externo
docker-compose -f docker-compose.yml config > docker-stack.yml
docker stack deploy -c docker-stack.yml wms
```

### Environment Variables para Producción

| Variable | Descripción | Requerido |
|----------|-------------|-----------|
| `SPRING_DATASOURCE_URL` | JDBC URL de PostgreSQL | ✅ |
| `SPRING_DATASOURCE_PASSWORD` | Password de DB | ✅ |
| `JWT_SECRET_KEY` | Clave JWT (min 256 bits) | ✅ |
| `WMS_TENANT_ISOLATION_ENABLED` | Habilitar schema isolation | Opcional |
| `WMS_TENANT_RLS_ENABLED` | Habilitar RLS policies | Opcional |

---

## 🤝 Contribuir

1. Fork el repositorio
2. Crear rama feature (`git checkout -b feature/amazing-feature`)
3. Commit con mensajes convencionales
4. Push a la rama
5. Abrir Pull Request

Lee nuestro [CONTRIBUTING.md](docs/CONTRIBUTING.md) para detalles.

---

## 📝 Licencia

Este proyecto está bajo licencia MIT. Ver [LICENSE](LICENSE) para detalles.

---

## 👤 Autor

**Juan Manuel Benevento**
- GitHub: [@JuanBenevento](https://github.com/JuanBenevento)
- LinkedIn: [juanbenevento](https://linkedin.com/in/juanbenevento)

---

## ⭐️ Give a Star

Si este proyecto te fue útil, ¡danos una ⭐ en GitHub!

---

*WMS Enterprise - Built with ❤️ using Java 21 + Angular 20*
