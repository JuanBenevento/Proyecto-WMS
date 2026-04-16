# 🏗️ Enterprise WMS - Documentación de Arquitectura

## 1. Visión del Producto
Sistema de Gestión de Almacenes (WMS) diseñado bajo arquitectura de microservicios y principios DDD. Su objetivo es optimizar el flujo logístico mediante estrategias inteligentes de ubicación y despacho, garantizando trazabilidad total y escalabilidad para operaciones de alto volumen.

### 1.1 Glosario (Lenguaje Ubicuo)
* **SKU (Stock Keeping Unit):** Identificador único de un producto.
* **LPN (License Plate Number):** Identificador único de un contenedor (pallet/caja) que agrupa inventario.
* **Bin/Ubicación:** Coordenada física tridimensional dentro del almacén.
* **Picking:** Proceso de recolección de mercancía para un pedido.
* **Put-Away:** Proceso de ubicación estratégica de mercancía recibida.

---

## 2. Alcance y Requerimientos

### 2.1 Épicas Principales
1.  **Gestión de Inventario Core:** Control de stock, lotes y vencimientos.
2.  **Inbound Logistics:** Recepción, Control de Calidad y Put-Away.
3.  **Outbound Logistics:** Reserva de stock, Picking y Despacho.

### 2.2 Requisitos No Funcionales (NFRs)
* **Latencia:** Las operaciones de escaneo (lectura) deben responder en < 200ms.
* **Consistencia:** El inventario debe mantener consistencia eventual entre microservicios, pero consistencia fuerte dentro del mismo LPN.
* **Seguridad:** Autenticación vía OAuth2/JWT.

---

## 3. Arquitectura de la Solución

### 3.1 Estilo Arquitectónico
Se utiliza **Arquitectura Hexagonal (Ports & Adapters)** para desacoplar la lógica de negocio de la infraestructura.
* **Core:** Java puro (POJOs). Contiene las reglas de negocio (Entidades, Value Objects).
* **Ports:** Interfaces que definen las entradas y salidas del hexágono.
* **Adapters:** Implementaciones técnicas (Controladores REST, Repositorios JPA).

### 3.2 Diagrama de Contenedores (Microservicios)
Este diagrama muestra la distribución física de los componentes y su comunicación.

mermaid
C4Context
    title Diagrama de Contenedores - Sistema WMS

    Person(operario, "Operario de Almacén", "Usa el sistema para recibir y despachar")
    Person(admin, "Gerente de Logística", "Configura reglas y audita inventario")

    System_Boundary(wms_system, "WMS Enterprise System") {
        
        Container(web_app, "SPA Frontend", "Angular, TypeScript", "Interfaz visual para operarios y admins")
        
        Container(api_gateway, "API Gateway", "Spring Cloud Gateway", "Enruta peticiones y maneja seguridad centralizada")
        
        Container(auth_service, "Identity Service", "Spring Security / OAuth2", "Maneja tokens JWT y Usuarios")
        
        Container(inventory_service, "Core Inventory Service", "Spring Boot (Hexagonal)", "Maneja Lógica de Dominio, Reglas de Ubicación y Stock")
        
        ContainerDb(database, "WMS Database", "PostgreSQL", "Persistencia de Productos, Lotes y Ubicaciones")
        
        ContainerQueue(broker, "Event Bus", "RabbitMQ", "Comunicación asíncrona de eventos de dominio")
    }

    Rel(operario, web_app, "Usa", "HTTPS")
    Rel(admin, web_app, "Usa", "HTTPS")
    
    Rel(web_app, api_gateway, "API Calls", "JSON/HTTPS")
    
    Rel(api_gateway, auth_service, "Valida Token", "gRPC/REST")
    Rel(api_gateway, inventory_service, "Proxies Request", "REST")
    
    Rel(inventory_service, database, "Lee/Escribe", "JDBC/JPA")
    Rel(inventory_service, broker, "Publica Eventos (StockChanged)", "AMQP")

## 3. Arquitectura de la Solución

### 3.1 Estilo Arquitectónico
Se utiliza **Arquitectura Hexagonal (Ports & Adapters)** para desacoplar la lógica de negocio de la infraestructura.
* **Core:** Java puro (POJOs). Contiene las reglas de negocio (Entidades, Value Objects).
* **Ports:** Interfaces que definen las entradas y salidas del hexágono.
* **Adapters:** Implementaciones técnicas (Controladores REST, Repositorios JPA).

### 3.2 Diagrama de Contenedores (Microservicios)
Este diagrama muestra la distribución física de los componentes y su comunicación.

mermaid
C4Context
    title Diagrama de Contenedores - Sistema WMS

    Person(operario, "Operario de Almacén", "Usa el sistema para recibir y despachar")
    Person(admin, "Gerente de Logística", "Configura reglas y audita inventario")

    System_Boundary(wms_system, "WMS Enterprise System") {
        
        Container(web_app, "SPA Frontend", "Angular, TypeScript", "Interfaz visual para operarios y admins")
        
        Container(api_gateway, "API Gateway", "Spring Cloud Gateway", "Enruta peticiones y maneja seguridad centralizada")
        
        Container(auth_service, "Identity Service", "Spring Security / OAuth2", "Maneja tokens JWT y Usuarios")
        
        Container(inventory_service, "Core Inventory Service", "Spring Boot (Hexagonal)", "Maneja Lógica de Dominio, Reglas de Ubicación y Stock")
        
        ContainerDb(database, "WMS Database", "PostgreSQL", "Persistencia de Productos, Lotes y Ubicaciones")
        
        ContainerQueue(broker, "Event Bus", "RabbitMQ", "Comunicación asíncrona de eventos de dominio")
    }

    Rel(operario, web_app, "Usa", "HTTPS")
    Rel(admin, web_app, "Usa", "HTTPS")
    
    Rel(web_app, api_gateway, "API Calls", "JSON/HTTPS")
    
    Rel(api_gateway, auth_service, "Valida Token", "gRPC/REST")
    Rel(api_gateway, inventory_service, "Proxies Request", "REST")
    
    Rel(inventory_service, database, "Lee/Escribe", "JDBC/JPA")
    Rel(inventory_service, broker, "Publica Eventos (StockChanged)", "AMQP")

### 3.3 Stack Tecnológico
| Capa | Tecnología | Justificación |
| :--- | :--- | :--- |
| **Lenguaje** | Java 21 LTS | Uso de Virtual Threads para alta concurrencia. |
| **Framework** | Spring Boot 3.2 | Estándar de industria, soporte nativo para GraalVM. |
| **BD Relacional** | PostgreSQL 16 | Robustez ACID para transacciones de inventario. |
| **Mensajería** | RabbitMQ | Comunicación asíncrona entre servicios (Domain Events). |

---

## 4. Diseño del Dominio (Core)

### 4.1 Modelo de Entidades
classDiagram
    %% RELACIONES
    Product "1" -- "0..*" InventoryItem : define características de
    Location "1" -- "0..*" InventoryItem : almacena
    InventoryItem "1" -- "0..1" LPN : identificado por
    PickingTask "1" -- "1" InventoryItem : reserva stock de
    PickingTask "1" -- "1" Order : pertenece a
    
    %% INTERFACES (STRATEGY PATTERN)
    <<Interface>> PutAwayStrategy
    PutAwayStrategy <|.. FEFOStrategy
    PutAwayStrategy <|.. HeavyLoadStrategy
    
    Product ..> PutAwayStrategy : usa estrategia según familia

    %% CLASES DEL DOMINIO
    class Product {
        -SKU sku
        -String name
        -Dimensions dimensions
        -StorageProfile storageProfile
        -FamilyType family
        +calculateVolume() double
        +isCompatibleWith(Location loc) boolean
    }

    class Location {
        -String locationCode
        -ZoneType zone
        -Dimensions maxCapacity
        -double currentWeight
        +hasSpaceFor(Product p) boolean
        +reserveSpace(double volume) void
    }

    class InventoryItem {
        -UUID id
        -LPN lpn
        -Batch batch
        -double quantity
        -InventoryStatus status
        +allocate(double amount) void
        +moveTo(Location newLoc) void
        +markAsDamaged() void
    }

    class PickingTask {
        -UUID taskId
        -PickingStatus status
        -double quantityToPick
        -User assignedPicker
        +confirmPick(LPN scannedLpn, Location scannedLoc) boolean
        +reportShortage() void
    }

    %% VALUE OBJECTS (Objetos inmutables)
    class Dimensions {
        +double height
        +double width
        +double depth
        +double weight
        +calculateVolume() double
    }

    class BatchNumber {
        +String value
        +isValid() boolean
    }
    
    class Lpn {
        +String value
        +isSentinel() boolean
        +isPickingLpn() boolean
        +generate() Lpn
    }

    %% MÉTODOS DE LA INTERFAZ
    class PutAwayStrategy {
        +suggestLocation(Product p, List~Location~ candidates) Location
    }

### 4.2 Patrones de Diseño Aplicados
* **Strategy Pattern:** Utilizado en el motor de `PutAwayService` para alternar dinámicamente entre estrategias de almacenamiento (FEFO, Carga Pesada, Refrigerados).
* **Factory Pattern:** Para la creación de tareas de Picking complejas.

---

## 5. Registro de Decisiones de Arquitectura (ADR)

**ADR-001: Separación de Product Master e Inventory Item**
* **Contexto:** Necesitamos gestionar lotes y estados variables (roto/sano) sin duplicar la información del producto base.
* **Decisión:** Se crean dos entidades separadas. `Product` contiene la metadata estática y `InventoryItem` contiene la instancia física con su LPN.
* **Consecuencia:** Mayor complejidad en las consultas (Joins), pero total flexibilidad en la gestión de almacén.

---

**ADR-002: Value Objects para Lpn y BatchNumber**
* **Contexto:** Lpn y BatchNumber eran Strings sin validación, causando datos inválidos en la DB.
* **Decisión:** Crear Value Objects inmutables con:
  - `Lpn`: Valida formato `LPN-{8-CHARS}`, `PICK-{8-CHARS}`, sentinels
  - `BatchNumber`: Valida longitud máx 50 chars, caracteres válidos
* **Consecuencia:**
  - Validación en el dominio, no en la infraestructura
  - Inmutabilidad garantiza consistencia
  - JPA Converters para persistencia transparente

---

**ADR-003: Estandarización API REST**
* **Contexto:** Endpoints inconsistentes (`create`, `save`, `add`, etc.)
* **Decisión:** Usar verbos HTTP según convención:
  - `POST /receive` - Recibir mercancía
  - `POST /put-away` - Ubicar en almacén
  - `POST /move` - Mover entre ubicaciones
  - `POST /adjust` - Ajustar inventario
  - `POST /allocate` - Reservar stock para picking
* **Consecuencia:** API predecible y documentable
