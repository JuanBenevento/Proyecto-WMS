# Guía de Contribución

## 🔀 Workflow de Git

### Ramas

| Prefijo | Uso |
|---------|-----|
| `feature/` | Nuevas funcionalidades |
| `fix/` | Bug fixes |
| `refactor/` | Refactoring de código |
| `docs/` | Documentación |
| `chore/` | Tareas de mantenimiento |

### Proceso

1. Crear branch desde `dev`: `git checkout -b feature/mi-feature`
2. Desarrollar y testear localmente
3. Commitear usando conventional commits
4. Push y crear Pull Request hacia `dev`
5. Code review y merge

---

## 📝 Conventional Commits

Este proyecto usa [Conventional Commits](https://www.conventionalcommits.org/).

### Formato

```
<tipo>(<alcance>): <descripción>

[body opcional]

[footer opcional]
```

### Tipos Permitidos

| Tipo | Descripción |
|------|-------------|
| `feat` | Nueva funcionalidad |
| `fix` | Bug fix |
| `refactor` | Refactoring sin cambio de funcionalidad |
| `docs` | Solo documentación |
| `test` | Agregar o modificar tests |
| `chore` | Tareas de build, config, dependencias |
| `perf` | Mejoras de performance |
| `ci` | Cambios en CI/CD |

### Ejemplos

```bash
# Nueva feature
git commit -m "feat(inventory): agregar endpoint para ajustes de stock"

# Bug fix
git commit -m "fix(picking): corregir race condition en reserva de stock"

# Refactoring
git commit -m "refactor(domain): migrar InventoryItem a Value Objects"

# Solo docs
git commit -m "docs(api): actualizar changelog"
```

### Breaking Changes

Usar `!` después del tipo o `BREAKING CHANGE:` en el footer:

```bash
git commit -m "feat(inventory)!: cambiar formato de LPN

BREAKING CHANGE: El campo lpn ahora es un objeto con validaciones.
```

---

## ✅ Checklist antes de PR

- [ ] Código compila (`./mvnw compile`)
- [ ] Tests pasan (`./mvnw test`)
- [ ] Commits siguen conventional commits
- [ ] No hay `TODO` sin justificar
- [ ] Documentación actualizada si aplica

---

## 🧪 Testing

```bash
# Tests unitarios
./mvnw test

# Con coverage
./mvnw test jacoco:report

# Solo una clase
./mvnw test -Dtest=NombreDelTest
```

### E2E Tests (Playwright)

```bash
# Instalar dependencias
cd e2e
npm install

# Todos los tests
npm run test

# Solo API tests
npm run test:api

# Solo UI tests
npm run test:ui-suite

# Modo interactivo
npm run test:ui
```

### Principios

1. **Testea el comportamiento, no la implementación**
2. **Un assert por test** (cuando sea posible)
3. **Nombres descriptivos**: `shouldReturnEmptyListWhenNoInventory`

---

## 📐 Convenciones de Código

### Nomenclatura

- **Clases**: PascalCase (`InventoryService`)
- **Métodos**: camelCase (`processInventory`)
- **Constantes**: UPPER_SNAKE_CASE (`MAX_RETRY_ATTEMPTS`)
- **Paquetes**: lowercase (`com.juanbenevento.wms.inventory`)

### Estructura de Paquetes

```
module/
├── domain/
│   ├── model/        # Entidades, Value Objects
│   ├── event/        # Domain Events
│   └── exception/    # Excepciones del dominio
├── application/
│   ├── service/      # Servicios de aplicación
│   ├── port/
│   │   ├── in/       # Interfaces de entrada (UseCases)
│   │   └── out/      # Interfaces de salida (Repositories)
│   ├── command/       # Commands (CQRS)
│   ├── dto/          # Data Transfer Objects
│   └── mapper/       # Mappers
└── infrastructure/
    ├── in/
    │   └── rest/     # Controladores REST
    └── out/
        └── persistence/  # JPA Entities, Repositories
```

---

## 🔍 Code Review

### Lo que se revisa

- ✅ Lógica de negocio correcta
- ✅ Nombres claros y descriptivos
- ✅ Tests adecuados
- ✅ Sin código duplicado
- ✅ Performance adecuada
- ✅ Seguridad (SQL injection, XSS, etc.)

### Lo que NO se revisa

- Estilo de formatting (eso lo hace el linter)
- Preferencias personales sin impacto real
