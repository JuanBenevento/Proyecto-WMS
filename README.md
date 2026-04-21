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

- **Java** 21 LTS
- **Maven** 3.9+
- **Node.js** 20+
- **PostgreSQL** 16+ (production)
- **H2** (development/tests)

### Installation

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
```

### Test Coverage

| Layer | Tests | Coverage |
|-------|-------|----------|
| Domain | 80+ | 95% |
| Application | 60+ | 85% |
| Infrastructure | 45+ | 70% |
| **Total** | **185+** | **~80%** |

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

### Docker Compose (Desarrollo)

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: wms_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
  
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/wms_db
      JWT_SECRET_KEY: your-secret-key
    depends_on:
      - postgres
```

### Kubernetes (Producción)

```bash
# Deploy con Helm
helm install wms ./charts/wms \
  --set database.host=postgres.prod.svc \
  --set jwt.secretKey=$JWT_SECRET_KEY
```

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
