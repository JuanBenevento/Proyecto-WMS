# Warehouse Management System (WMS)

Sistema de Gestión de Almacenes construido con arquitectura hexagonal y principios DDD.

## 🚀 Quick Start

### Prerequisites
- Java 21
- Maven 3.9+
- PostgreSQL 16 (para desarrollo local)
- H2 (base de datos en memoria para tests)

### Setup

```bash
# Clonar el repositorio
git clone https://github.com/JuanBenevento/Proyecto-WMS.git
cd Proyecto-WMS

# Ejecutar con Maven (usa H2 en memoria por defecto)
./mvnw spring-boot:run
```

La API estará disponible en: `http://localhost:8080`

### Tests

```bash
# Ejecutar todos los tests
./mvnw test

# Tests específicos
./mvnw test -Dtest=InventoryServiceTest
```

---

## 📁 Estructura del Proyecto

```
src/
├── main/java/com/juanbenevento/wms/
│   ├── catalog/           # Dominio de Catálogo de Productos
│   ├── identity/          # Gestión de Usuarios y Tenants
│   ├── inventory/         # Core de Inventario
│   ├── shared/            # Código compartido (Value Objects, excepciones)
│   └── warehouse/         # Gestión de Ubicaciones
│   │
│   ├── domain/           # Entidades, Value Objects, Eventos
│   ├── application/      # Casos de uso, Servicios, Commands, DTOs
│   └── infrastructure/   # Adapters (REST, JPA, etc.)
└── test/                  # Tests unitarios e integrados
```

---

## 🏗️ Arquitectura

El proyecto sigue **Arquitectura Hexagonal** con **DDD**:

- **Domain Layer**: Java puro, reglas de negocio
- **Application Layer**: Casos de uso, servicios
- **Infrastructure Layer**: Controladores REST, persistencia JPA

### Value Objects Principales

| Value Object | Descripción |
|--------------|-------------|
| `Lpn` | License Plate Number - identificador único de inventario |
| `BatchNumber` | Número de lote para trazabilidad |
| `Dimensions` | Dimensiones físicas (alto, ancho, profundo, peso) |

### Entidades Core

- `InventoryItem`: Instancia física de inventario (LPN + cantidad + ubicación)
- `Location`: Ubicación física en el almacén
- `Product`: Master de producto (SKU + dimensiones)

---

## 🔌 API Endpoints

### Inventario
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/inventory/receive` | Recibir mercancía |
| POST | `/api/v1/inventory/put-away` | Ubicar en almacén |
| POST | `/api/v1/inventory/move` | Mover entre ubicaciones |
| POST | `/api/v1/inventory/adjust` | Ajustar inventario |
| GET | `/api/v1/inventory/{lpn}` | Consultar por LPN |

### Picking
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/picking/allocate` | Reservar stock para picking |

---

## 📖 Documentación Adicional

- [Arquitectura](docs/ARCHITECTURE.md)
- [Requisitos Funcionales](docs/FUNCTIONAL_REQS.md)

---

## 🤝 Contribuir

Ver [CONTRIBUTING.md](docs/CONTRIBUTING.md) para guidelines de commits y workflow.

---

## 📄 Licencia

MIT
