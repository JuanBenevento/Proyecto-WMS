# Warehouse Management System (WMS)

## 📌 Descripción General

Este proyecto es un **Warehouse Management System (WMS)** desarrollado con arquitectura hexagonal y principios DDD.

El sistema modela operaciones centrales de un almacén:

* Gestión de productos
* Control de inventario
* Ubicaciones físicas con restricciones de capacidad
* Movimientos de stock (recepción, reserva, picking, despacho)
* **Gestión de órdenes (Order Management)**
* Auditoría mediante eventos de dominio

El foco principal del proyecto está puesto en:

* **Arquitectura limpia (Hexagonal / Clean Architecture)**
* **Event-Driven Architecture** con Event Bus y retry/DLQ
* **Reglas de negocio explícitas**
* **Separación de responsabilidades**
* **Escalabilidad y mantenibilidad**

---

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
```

---

## 📁 Estructura del Proyecto

```
src/
├── main/java/com/juanbenevento/wms/
│   ├── catalog/           # Dominio de Catálogo de Productos
│   ├── identity/          # Gestión de Usuarios y Tenants
│   ├── inventory/         # Core de Inventario
│   ├── orders/            # Gestión de Órdenes
│   ├── warehouse/         # Gestión de Ubicaciones
│   ├── shared/            # Código compartido (Value Objects, excepciones)
│   │
│   ├── domain/           # Entidades, Value Objects, Eventos
│   ├── application/      # Casos de uso, Servicios, Commands, DTOs
│   └── infrastructure/    # Adapters (REST, JPA, etc.)
└── test/                  # Tests unitarios e integrados
```

---

## 🏗️ Arquitectura

El backend implementa **Arquitectura Hexagonal (Ports & Adapters)**, con una separación clara entre:

```
com.juanbenevento.wms
├── domain            # Núcleo del negocio (entidades, reglas, eventos)
├── application       # Casos de uso y puertos
└── infrastructure    # Adaptadores (REST, persistencia, seguridad)
```

### Arquitectura de Eventos (Orders ↔ Inventory)

```
┌─────────────────────────────────────────────────────────────────┐
│                         ORDER SERVICE                             │
│  OrderService.createOrder()                                      │
│       │                                                           │
│       ├── OrderCreatedEvent                                      │
│       └── EventBus (InMemory + Retry/DLQ)                        │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                     INVENTORY SERVICE                             │
│  OrderAssignmentService detecta pedidos PENDING                  │
│       │                                                           │
│       ├── StockAssignedEvent (Spring Events)                     │
│       └── InventoryEventBridge                                    │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                   ORDER EVENT HANDLER                             │
│  onStockAssigned() → Order.allocate()                          │
│  onStockShortage() → Order.hold()                              │
│  onPickingStarted() → Order.startPicking()                      │
│  onPickingCompleted() → Order.pack()                             │
└─────────────────────────────────────────────────────────────────┘
```

### Flujo de Picking con Short Pick Handling

```
1. startPicking(orderId, decision)
       │
       ├── Crear PickingSession
       └── PickingStartedEvent → Order.status = PICKING

2. pickLine(orderId, lineId, qty)
       │
       ├── Full Pick: qty == allocated
       └── Short Pick: qty < allocated
                    │
                    └── Según decisión:
                        - ALLOW_PARTIAL_SHIPMENT
                        - BLOCK_UNTIL_COMPLETE
                        - AUTO_REPLENISH
                        - MANUAL_DECISION

3. completePicking(orderId)
       │
       ├── PickingCompletedEvent
       └── Order.status = PACKED
```

---

## 🧩 Modelado de Dominio

El dominio no es anémico. Algunas reglas implementadas:

* Un producto **no puede modificar sus dimensiones** si existe stock físico
* Una ubicación **no puede exceder su capacidad** (peso / volumen)
* El inventario genera **eventos de dominio** ante cambios relevantes
* **Order Management** con estados extensibles y razones configurables

---

## ⚙️ Stack Tecnológico

### Backend
* **Java 21**
* **Spring Boot 3**
* Spring Data JPA
* Spring Security + JWT
* PostgreSQL
* SpringDoc OpenAPI (Swagger)

### Frontend
* **Angular 20**
* Arquitectura modular por features

---

## 🧪 Testing

* Tests unitarios enfocados en el **dominio y reglas de negocio**
* Tests de servicio con mocks
* Validaciones de invariantes críticas

---

## 🔌 API Endpoints

### Orders
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/orders` | Crear orden |
| GET | `/api/v1/orders/{id}` | Obtener orden |
| GET | `/api/v1/orders` | Listar órdenes |
| POST | `/api/v1/orders/{id}/confirm` | Confirmar |
| POST | `/api/v1/orders/{id}/cancel` | Cancelar |
| POST | `/api/v1/orders/{id}/hold` | Poner en espera |
| POST | `/api/v1/orders/{id}/ship` | Enviar |

### Inventory
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/inventory/receive` | Recibir mercancía |
| POST | `/api/v1/inventory/put-away` | Ubicar en almacén |

### Picking
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/picking/start` | Iniciar picking |
| POST | `/api/v1/picking/pick-line` | Registrar pick de línea |
| POST | `/api/v1/picking/complete` | Completar picking |

---

## 👤 Autor

**Juan Manuel Benevento**

---

## 📄 Licencia

Este proyecto se publica con fines demostrativos y educativos.
