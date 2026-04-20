# WMS - Roadmap de Desarrollo Futuro

## 📋 Estado Actual (Abril 2025)

### ✅ Completado (Plan #1)
- Endpoints REST estandarizados
- Value Objects (LPN, BatchNumber)
- Tests passing (185)
- Documentación completa

### 🚀 Próximos Pasos Sugeridos

---

## 🎯 Phase 1: Estabilización (2-3 semanas)

### Objetivos
- [ ] Hacer el proyecto executable fácil
- [ ] Fix tests deshabilitados
- [ ] API Response estándar

### Tareas
1. **Docker Compose** - Levantar todo con un comando
   ```yaml
   # docker-compose.yml
   services:
     postgres:
       image: postgres:16
       environment:
         POSTGRES_DB: wms_db
       ports:
         - "5432:5432"
   
     app:
       build: .
       ports:
         - "8080:8080"
       depends_on:
         - postgres
   ```

2. **Fix EventPersistenceIntegrationTest** - Investigar y corregir

3. **API Response Wrapper** - Estandarizar respuestas
   ```java
   public record ApiResponse<T>(T data, String message, boolean success) {}
   ```

---

## 🎯 Phase 2: Observabilidad (2 semanas)

### Objetivos
- [ ] Health checks
- [ ] Métricas
- [ ] Logging estructurado

### Tareas
1. Agregar Spring Boot Actuator
2. Agregar Micrometer + Prometheus
3. Agregar Circuit Breaker (Resilience4j)

---

## 🎯 Phase 3: Performance (2-3 semanas)

### Objetivos
- [ ] Caching
- [ ] Query optimization
- [ ] Pagination

### Tareas
1. Implementar caching con Redis/Guava
2. Optimizar queries N+1 con JOINs
3. Agregar pagination efectivo en endpoints

---

## 🎯 Phase 4: Escalabilidad (3-4 semanas)

### Objetivos
- [ ] Kubernetes ready
- [ ] CI/CD
- [ ] External config

### Tareas
1. Dockerize la aplicación
2. Crear pipeline de GitHub Actions
3. Externalizar configuración a config server

---

## 📊 Recursos Necesarios

### Para completar todo el roadmap:
- **Backend**: ~3-4 meses
- **Frontend**: ~2 meses adicionales

### Equipo sugerido:
- 1-2 developers full-stack
- DevOps para infraestructura

---

## 💡 Recomendación Inmediata

1. **Hacer executable el proyecto** - Prioridad máxima
2. **Docker Compose** - Facilitar onboarding
3. **Documentar API** - Usar Swagger existente

---

*Documento generado: 2025-04-20*