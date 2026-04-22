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

---

**ADR-004: Aislamiento de Datos por Tenant - Schema por Empresa**
* **Contexto:** El sistema actual utiliza un patrón de "soft multi-tenancy" con columna `tenant_id` + Hibernate filter. Esto representa un riesgo de seguridad desde el punto de vista de auditoría profesional, porque:
  - Si hay acceso directo a la base de datos (DBA, backup comprometido), los datos de TODAS las empresas son visibles
  - No hay aislamiento a nivel de base de datos
  - No cumple con requisitos de certificaciones SOC2 o ISO 27001
* **Decisión:** Implementar schema por tenant (schema-based multi-tenancy) en PostgreSQL:
  - Cada empresa (tenant) tendrá su propio `SCHEMA` dentro de la misma base de datos
  - El usuario de aplicación tendrá permisos `USAGE` solo sobre el schema de su empresa
  - Se complementará con PostgreSQL RLS (Row-Level Security) como capa adicional
  - No se usa la opción de base de datos por tenant por su complejidad operacional
* **Alternativas evaluadas:**
  - *Opción A - Soft multi-tenancy (tenant_id)*: Estado actual. No pasa auditoría.
  - *Opción B - Schema por tenant*: Elegida. Isolation a nivel DB + bajo costo operacional.
  - *Opción C - Base de datos por tenant*: Excesivo costo operacional para >50 tenants.
* **Estado de Implementación:** ✅ COMPLETADO (Fases 1-8)
* **Fecha de Implementación:** Abril 2026
* **Archivos Implementados:**
  - `src/main/java/.../tenant/TenantSchemaManager.java` - Gestión de esquemas PostgreSQL
  - `src/main/java/.../tenant/SearchPathConnectionInterceptor.java` - JDBC interceptor
  - `src/main/java/.../tenant/TenantContext.java` - Extendido con getSchemaName()
  - `src/main/java/.../tenant/TenantFilterAspect.java` - Refactorizado para schema validation
  - `src/main/java/.../tenant/SchemaIsolationValidator.java` - Validador de aislamiento
  - `src/main/resources/db/migration/V1__initial_tenant_structure.sql` - Infraestructura inicial
  - `src/main/resources/db/migration/V__enable_rls_*.sql` - Políticas RLS (5 archivos)
* **Notas de Implementación:**
  - Schema naming convention: `tenant_{tenantId}` (lowercase, underscores)
  - Connection pooling con search_path dinámico por sesión
  - RLS habilitado como capa de defensa en profundidad
  - Fase 6 (data migration) requiere ejecución manual en producción
* **Consecuencias:**
  - Requiere refactorización de capas de persistencia para obtener conexión dinámica por schema
  - scripts de migración deben crear schema por tenant
  - El TenantContext debe traducirse a schema de BD
  - Backup y restore deben ser por schema o selectivos
  - Cumple con requisitos de auditoría profesional
