# 📘 Especificación Técnica Completa - WMS Enterprise

## Tabla de Contenidos
1. [Visión del Producto](#1-visión-del-producto)
2. [Alcance del Sistema](#2-alcance-del-sistema)
3. [Ingeniería de Requerimientos](#3-ingeniería-de-requerimientos)
4. [Arquitectura del Sistema](#4-arquitectura-del-sistema)
5. [Casos de Uso](#5-casos-de-uso)
6. [Modelado de Dominio](#6-modelado-de-dominio)
7. [Estándares de Desarrollo](#7-estándares-de-desarrollo)

---

## 1. Visión del Producto

### 1.1 Propósito
**WMS Enterprise** es un sistema de gestión de almacenes moderno y escalable, diseñado para optimizar operaciones logísticas de alto volumen mediante automatización inteligente de procesos, trazabilidad total y decisiones basadas en datos.

### 1.2 Problema que Resuelve
Los almacenes tradicionales enfrentan:
- **Ineficiencia en ubicación de stock**: Mercancía almacenada sin criterios óptimos
- **Falta de trazabilidad**: No se conoce el historial completo de cada unidad
- **Errores en picking**: Picking manual genera equivocaciones costosas
- **Sobreventa**: Reservas de stock inconsistentes causan overselling
- **Lentitud operativa**: Procesos manuales ralentizan operaciones

### 1.3 Solución Propuesta
Sistema automatizado que:
- **Optimiza ubicación** mediante algoritmos estratégicos (FEFO, consolidación, compatibilidad de zona)
- **Garantiza trazabilidad** con eventos de dominio persistidos
- **Valida operaciones** con escaneo de ubicación y producto
- **Reserva stock atomicamente** para evitar sobreventa
- **Escala horizontalmente** usando arquitectura hexagonal

### 1.4 Stakeholders

| Stakeholder | Rol | Necesidad Principal |
|------------|-----|---------------------|
| Gerente de Inventario | Administrador | Control de stock, reportes, configuración |
| Operario de Recepción | Usuario | Interfaz rápida para recibir mercancía |
| Picker | Usuario | Guía clara de rutas de picking |
| Administrador (SaaS) | SuperAdmin | Gestión multi-tenant |
| Auditor | Usuario | Trazabilidad completa de movimientos |

### 1.5 Objetivos Estratégicos

| Objetivo | Métrica | Meta |
|----------|---------|------|
| Reducir errores de picking | Tasa de error | < 0.5% |
| Optimizar espacio de almacén | Utilización | > 85% |
| Tiempo de recepción por pallet | Minutos | < 3 min |
| Trazabilidad | Eventos registrados | 100% |
| Disponibilidad | Uptime | > 99.5% |

---

## 2. Alcance del Sistema

### 2.1 Alcance Funcional

#### Módulos Incluidos ✅

| Módulo | Funcionalidad | Estado |
|--------|---------------|--------|
| **Gestión de Productos** | Catálogo con perfil logístico, dimensiones, condiciones de almacenamiento | ✅ |
| **Gestión de Ubicaciones** | Layout jerárquico (Zona→Pasillo→Rack→Nivel→Posición), capacidades | ✅ |
| **Gestión de Inventario** | Stock, lotes, vencimientos, estados, movimientos | ✅ |
| **Inbound (Recepción)** | Recepción ciega, generación LPN, control calidad, put-away | ✅ |
| **Outbound (Pedidos)** | Órdenes, asignación stock FEFO, picking, despacho | ✅ |
| **Gestión de Órdenes** | Estados extensibles, transiciones controladas, prioridades | ✅ |
| **Auditoría** | Eventos de dominio persistidos, historial por LPN | ✅ |
| **Identidad y Acceso** | JWT Auth, multi-tenant, roles (SuperAdmin, Admin, Operator) | ✅ |

#### Funcionalidades Futuras (Roadmap) 📋

| Funcionalidad | Descripción | Prioridad |
|--------------|------------|-----------|
| Módulo de Billing | Facturación por almacenamiento | Media |
| Reporting Avanzado | Dashboard KPIs, gráficos | Alta |
| Integraciones | APIs externas, EDI | Media |
| Handhelds | Interfaz para terminales de mano | Alta |
| Wave Planning | Agrupación de órdenes para picking | Baja |

### 2.2 Alcance No Funcional

#### Requisitos de Calidad

| Requisito | Descripción | Target |
|-----------|-------------|--------|
| **Rendimiento** | Latencia operaciones de escaneo | < 200ms p95 |
| **Rendimiento** | Latencia consultas inventario | < 500ms p95 |
| **Escalabilidad** | Órdenes concurrentes | > 1000/min |
| **Disponibilidad** | Uptime mensual | > 99.5% |
| **Seguridad** | Autenticación | JWT OAuth2 |
| **Seguridad** | Encriptación DB | AES-256 |
| **Maintainability** | Código coverage | > 70% |
| **Recoverability** | RTO (Recovery Time Objective) | < 30 min |
| **Recoverability** | RPO (Recovery Point Objective) | < 5 min |

#### Tecnologías Requeridas

| Capa | Tecnología | Versión |
|------|------------|---------|
| Backend | Java | 21 LTS |
| Framework | Spring Boot | 3.4.x |
| Database | PostgreSQL | 16+ |
| Cache | Redis | 7.x |
| Mensajería | RabbitMQ | 3.12+ |
| Frontend | Angular | 20.x |
| Build | Maven | 3.9+ |

---

## 3. Ingeniería de Requerimientos

### 3.1 Clasificación de Requerimientos

#### Requerimientos Funcionales

```
RF-001: Gestión de Productos
├── RF-001.01: Crear producto con dimensiones y peso
├── RF-001.02: Editar producto (solo si sin stock)
├── RF-001.03: Listar productos con filtros
├── RF-001.04: Asignar perfil logístico (sec/refrigerado/congelado)
└── RF-001.05: Validar dimensiones contra ubicación

RF-002: Gestión de Ubicaciones
├── RF-002.01: Crear ubicación jerárquica
├── RF-002.02: Definir capacidad (volumen, peso)
├── RF-002.03: Clasificar zona (storage/operational)
├── RF-002.04: Bloquear/desbloquear ubicación
└── RF-002.05: Consultar ocupación

RF-003: Recepción (Inbound)
├── RF-003.01: Recepción ciega con generación LPN
├── RF-003.02: Capturar lote y vencimiento (obligatorio)
├── RF-003.03: Validar estado inicial (quality check)
├── RF-003.04: Put-away dirigido con sugerencia
└── RF-003.05: Consolidar mismo SKU

RF-004: Gestión de Órdenes
├── RF-004.01: Crear orden con líneas
├── RF-004.02: Confirmar orden (trigger asignación)
├── RF-004.03: Asignar stock FEFO automático
├── RF-004.04: Transiciones de estado controladas
├── RF-004.05: Prioridades (HIGH/MEDIUM/LOW)
└── RF-004.06: Cancelación con razón

RF-005: Picking
├── RF-005.01: Iniciar picking por orden
├── RF-005.02: Registrar pick por línea
├── RF-005.03: Manejo de short picks (varias estrategias)
├── RF-005.04: Validar escaneo ubicación/producto
└── RF-005.05: Completar picking → pack

RF-006: Despacho
├── RF-006.01: Asignar carrier
├── RF-006.02: Generar tracking number
├── RF-006.03: Registrar shipped
└── RF-006.04: Registrar delivered

RF-007: Auditoría
├── RF-007.01: Persistir todos los eventos de dominio
├── RF-007.02: Consultar historial por LPN
├── RF-007.03: Consultar historial por orden
└── RF-007.04: Reporte de movimientos
```

#### Requerimientos No Funcionales

```
RNF-001: El sistema debe responder en < 200ms para operaciones de escaneo
RNF-002: Debe soportar 1000+ órdenes simultáneas
RNF-003: Autenticación JWT con expiración configurable
RNF-004: Multi-tenant con aislamiento de datos por empresa
RNF-005: Logs de auditoría inmutables
RNF-006: Código con cobertura > 70%
RNF-007: Documentación API con OpenAPI/Swagger
```

### 3.2 Matriz de Trazabilidad

| ID Requerimiento | Módulo | Prioridad | Criterio de Aceptación | Test |
|-----------------|--------|-----------|------------------------|------|
| RF-001.01 | Productos | Alta | Producto creado con todas las dimensiones | Unit Test |
| RF-002.02 | Ubicaciones | Alta | Capacidad no excedida | Integration Test |
| RF-003.01 | Inbound | Alta | LPN único generado | Unit Test |
| RF-004.03 | Orders | Crítica | FEFO implementado | Unit Test |
| RF-005.04 | Picking | Alta | Bloqueo por escaneo incorrecto | Integration Test |
| RNF-001 | Performance | Alta | < 200ms p95 | Load Test |

---

## 4. Arquitectura del Sistema

### 4.1 Estilo Arquitectónico

**Arquitectura Hexagonal (Ports & Adapters)** con DDD

```
┌─────────────────────────────────────────────────────────────────┐
│                      INFRASTRUCTURE                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │ REST API     │  │  JPA/Postgre │  │  Event Bus  │    │
│  │ Controllers  │  │  Adapters    │  │  (RabbitMQ) │    │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘    │
│         │                 │                 │             │
│         ▼                 ▼                 ▼             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                  APPLICATION                         │   │
│  │  ┌─────────────────────────────────────────────┐    │   │
│  │  │              USE CASES / SERVICES          │    │   │
│  │  │   (OrderService, InventoryService, etc)  │    │   │
│  │  └──────────────────────┬──────────────────────┘    │   │
│  │                         │                           │   │
│  │         ┌───────────────┴───────────────┐          │   │
│  │         ▼                               ▼          │   │
│  │  ┌──────────────┐               ┌──────────────┐   │   │
│  │  │   PORTS IN  │               │  PORTS OUT   │   │   │
│  │  │ (Use Cases) │               │(Repositories)│   │   │
│  │  └──────────────┘               └──────────────┘   │   │
│  └─────────────────────────────────────────────────────┘   │
│                           │                                 │
│                           ▼                                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                    DOMAIN                            │   │
│  │  Entities │ Value Objects │ Domain Events │ Rules   │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 4.2 Capas del Sistema

| Capa | Responsabilidad | Ejemplos |
|------|----------------|----------|
| **Domain** | Entidades, reglas de negocio, eventos | Order, InventoryItem, Location |
| **Application** | Casos de uso, orchestration | OrderService, PickingService |
| **Infrastructure** | adapters técnicos | REST, JPA, Event Bus |
| **Presentation** | UI Angular | Componentes, páginas |

### 4.3 Patrones Aplicados

| Patrón | Aplicación |
|--------|-------------|
| **Domain Events** | OrderCreated, StockAssigned, PickingCompleted |
| **Event Sourcing** | Persistencia de eventos para auditoría |
| **Strategy** | PutAway strategies (FEFO, HeavyLoad, Refrigerated) |
| **Factory** | Creación de PickingSessions complejas |
| **Repository** | Abstracción de persistencia |
| **CQRS (parcial)** | Separación lectura/escritura en Orders |

---

## 5. Casos de Uso

### 5.1 UC-001: Recepción de Mercancía

**Actor**: Operario de Recepción  
**Precondición**: Usuario autenticado con rol OPERATOR

**Flujo Principal**:
1. Operario inicia recepción (scan o manual)
2. Sistema genera LPN único
3. Operario ingresa: SKU, cantidad, lote, vencimiento
4. Sistema valida producto existe
5. Sistema crea InventoryItem en estado IN_QUALITY_CHECK
6. Operario confirma QC aprobado
7. Sistema cambia estado a AVAILABLE
8. Sistema sugiere ubicación (put-away)
9. Operario confirma ubicación
10. Sistema registra movimiento y evento

**Flujo Alternativo**:
- Paso 4: Producto no existe → Error → Crear producto primero
- Paso 7: QC rechazado → Cambiar a DAMAGED → No disponible

**Postcondición**: InventoryItem creado en ubicación, evento persistido

---

### 5.2 UC-002: Creación y Asignación de Orden

**Actor**: Administrador / Sistema  
**Precondición**: Productos con stock disponible

**Flujo Principal**:
1. Admin crea orden con líneas (SKU, cantidad)
2. Sistema valida cada línea tiene stock
3. Admin confirma orden
4. Sistema cambia estado a PENDING
5. Sistema dispara asignación automática (FEFO)
6. Para cada línea:
   a. Busca库存 con mismo SKU, fecha vencimiento más próxima
   b. Reserva cantidad solicitada
   c. Cambia estado inventory: ALLOCATED
7. Si todas las líneas asignadas → estado ALLOCATED
8. Si parcial → estado PENDING (espera más stock)

**Flujo Alternativo**:
- Paso 3: Stock insuficiente → Mostrar faltantes → Poner en HOLD

**Postcondición**: Líneas de orden con inventory reservado

---

### 5.3 UC-003: Proceso de Picking

**Actor**: Picker  
**Precondición**: Orden en estado ALLOCATED

**Flujo Principal**:
1. Picker inicia picking de orden
2. Sistema crea PickingSession, cambia estado a PICKING
3. Sistema genera ruta óptima por ubicación
4. Para cada línea:
   a. Picker escanea ubicación
   b. Sistema valida ubicación correcta → Si no, error
   c. Picker escanea producto
   d. Sistema valida SKU correcto → Si no, error
   e. Picker ingresa cantidad pickeada
   f. Sistema compara: 
      - Si igual a allocated → FULL PICK
      - Si menor → SHORT PICK (según estrategia)
5. Picker completa picking
6. Sistema cambia estado a PACKED
7. Sistema registra evento PickingCompleted

**Estrategias de Short Pick**:
- ALLOW_PARTIAL_SHIPMENT: Continuar con lo pickeado
- BLOCK_UNTIL_COMPLETE: Bloquear hasta completar
- AUTO_REPLENISH: Generar reorder automático
- MANUAL_DECISION: Esperar decisión del supervisor

**Postcondición**: Orden en PACKED lista para shipment

---

### 5.4 UC-004: Despacho de Orden

**Actor**: Operario de Despacho  
**Precondición**: Orden en estado PACKED

**Flujo Principal**:
1. Operario inicia despacho
2. Operario ingresa carrier_id (obligatorio)
3. Operario ingresa tracking_number (opcional)
4. Sistema cambia estado a SHIPPED
5. Sistema registra evento OrderShipped
6. Sistema descuenta stock reservado

**Postcondición**: Stock publicado, orden SHIPPED

---

## 6. Modelado de Dominio

### 6.1 Entidades Principales

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│    Product      │────<│  InventoryItem  │>────│   Location      │
├─────────────────┤     ├─────────────────┤     ├─────────────────┤
│ - sku           │     │ - id (UUID)    │     │ - locationCode  │
│ - name          │     │ - lpn           │     │ - zone          │
│ - dimensions    │     │ - batchNumber   │     │ - capacity      │
│ - weight        │     │ - quantity      │     │ - currentWeight│
│ - storageCond   │     │ - status        │     │ - type          │
│ - family        │     │ - location      │     └─────────────────┘
└─────────────────┘     │ - allocatedTo   │
                       └─────────────────┘
                              │
                              │
                       ┌──────┴──────┐
                       │ Order       │
                       ├─────────────┤
                       │ - orderId   │
                       │ - customer  │
                       │ - status    │
                       │ - priority  │
                       │ ─────────── │
                       │ lines[]     │
                       └─────────────┘
```

### 6.2 Value Objects

| Value Object | Responsabilidad | Validaciones |
|--------------|----------------|--------------|
| **LPN** | Identificador de contenedor | Formato: LPN-{8CHARS}, PICK-{8CHARS} |
| **BatchNumber** | Número de lote | Max 50 chars, alfanumérico |
| **Dimensions** | Dimensiones físicas | Alto, ancho, profundidad > 0 |
| **LocationCode** | Código de ubicación | Formato jerárquico |
| **OrderStatus** | Estado de orden | Enum extensible |

### 6.3 Máquina de Estados - Order

```
                    ┌──────────────┐
                    │   CREATED   │
                    └──────┬───────┘
                           │ confirm()
                           ▼
                    ┌──────────────┐
                    │  CONFIRMED   │
                    └──────┬───────┘
                           │ (auto)
                           ▼
                    ┌──────────────┐
                    │   PENDING   │────┐
                    └──────┬───────┘    │
                           │ assign()   │ hold()
                           ▼           ▼
                    ┌──────────────┐ ┌─────────┐
                    │  ALLOCATED  │ │  HOLD  │
                    └──────┬───────┘ └───┬───┘
                           │ release()   │
                           ▼            │
                    ┌──────────────┐  │
                    │   PICKING    │──┘
                    └──────┬───────┘
                           │ pack()
                           ▼
                    ┌──────────────┐
                    │   PACKED     │
                    └──────┬───────┘
                           │ ship()
                           ▼
                    ┌──────────────┐
                    │   SHIPPED    │
                    └──────┬───────┘
                           │ deliver()
                           ▼
                    ┌──────────────┐
                    │  DELIVERED   │  (Terminal)
                    └──────────────┘

Estados Terminales: DELIVERED, CANCELLED
```

---

## 7. Estándares de Desarrollo

### 7.1 Conventional Commits

```
<tipo>(<alcance>): <descripción>

Tipos:
- feat: Nueva funcionalidad
- fix: Bug fix
- refactor: Refactoring sin cambio funcional
- perf: Mejora de rendimiento
- test: Tests
- docs: Documentación
- chore: Mantenimiento

Ejemplos:
feat(orders): add order cancellation with reason
fix(inventory): resolve FEFO allocation race condition
refactor(picking): extract short pick strategy to enum
```

### 7.2 Naming Conventions

| Elemento | Convención | Ejemplo |
|----------|-----------|---------|
| Clases Java | PascalCase | `OrderService` |
| Métodos | camelCase | `createOrder()` |
| Variables | camelCase | `orderId` |
| Constantes | UPPER_SNAKE | `MAX_QUANTITY` |
| Interfaces | PascalCase + sufijo Port | `OrderRepositoryPort` |
| Enums | PascalCase | `OrderStatus.CREATED` |
| Componentes Angular | kebab-case | `order-list` |
| Tablas DB | snake_case | `order_items` |

### 7.3 Estructura de Paquetes

```
com.juanbenevento.wms
├── [module]/
│   ├── domain/
│   │   ├── model/          # Entidades
│   │   ├── valueobject/    # VOs
│   │   ├── event/          # Domain events
│   │   └── exception/      # Excepciones de dominio
│   ├── application/
│   │   ├── port/
│   │   │   ├── in/         # Use case ports
│   │   │   └── out/        # Repository ports
│   │   ├── service/        # Servicios
│   │   ├── command/        # Commands (CQRS)
│   │   ├── query/         # Queries (CQRS)
│   │   └── dto/            # DTOs
│   └── infrastructure/
│       ├── in/rest/        # Controllers
│       └── out/persistence/# Adapters
└── shared/
    └── [código compartido]
```

---

## Anexo: Glosario

| Término | Definición |
|---------|------------|
| **SKU** | Stock Keeping Unit - Identificador único de producto |
| **LPN** | License Plate Number - Identificador de contenedor/pallet |
| **FEFO** | First Expire First Out - Estratégia de asignación por vencimiento |
| **Put-Away** | Proceso de ubicar mercancía recibida en almacén |
| **Picking** | Proceso de recolectar mercancía para pedidos |
| **Short Pick** | Picking parcial (menos de lo solicitado) |
| **Allocation** | Reserva de stock para una orden |
| **Tenant** | Empresa/cliente en arquitectura multi-tenant |
| **Domain Event** | Evento que representa un cambio significativo en el dominio |

---

*Documento generado para WMS Enterprise - Versión 1.0*
*Última actualización: 2026-04-21*
