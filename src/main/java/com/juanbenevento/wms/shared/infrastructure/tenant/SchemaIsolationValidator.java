package com.juanbenevento.wms.shared.infrastructure.tenant;

import com.juanbenevento.wms.shared.domain.valueobject.WmsConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Servicio de validación para aislamiento de tenants por schema.
 *
 * <p>Este validador permite verificar que las consultas SQL solo impactan
 * el schema del tenant actual, proporcionando funcionalidades de debugging
 * y testing para el sistema de aislamiento multi-tenant basado en schemas.
 *
 * <p>Uso típico:
 * <pre>
 * // En testing o debugging
 * SchemaIsolationValidator validator = ...;
 * validator.validateCurrentSchema(); // Lanza excepción si no coincide
 *
 * // Verificación програмática
 * boolean isValid = validator.isSchemaIsolationActive();
 * </pre>
 *
 * @see TenantContext
 * @see SearchPathConnectionInterceptor
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaIsolationValidator {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Valida que el schema actual de la conexión coincide con el esperado.
     *
     * @throws IllegalStateException si el tenant context no está configurado
     * @throws SecurityException si el search_path no coincide con el schema esperado
     */
    public void validateCurrentSchema() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set - cannot validate schema isolation");
        }

        if (WmsConstants.SYSTEM_TENANT.equals(tenantId)) {
            log.debug("ℹ️ Sistema tenant - omitiendo validación de schema");
            return;
        }

        String expectedSchema = TenantContext.getSchemaName(tenantId);
        String currentSearchPath = getCurrentSearchPath();

        if (!currentSearchPath.contains(expectedSchema)) {
            String message = String.format(
                    "⚠️ AISlamiento DE SCHEMA COMPROMETIDO: search_path actual='%s' pero se esperaba='%s'",
                    currentSearchPath,
                    expectedSchema
            );
            log.error(message);
            throw new SecurityException(message);
        }

        log.debug("✅ Validación de schema exitosa: {} en {}", tenantId, currentSearchPath);
    }

    /**
     * Verifica si el aislamiento de schema está activo para el contexto actual.
     *
     * @return true si el search_path coincide con el schema esperado, false en caso contrario
     */
    public boolean isSchemaIsolationActive() {
        try {
            String tenantId = TenantContext.getTenantId();
            if (tenantId == null || WmsConstants.SYSTEM_TENANT.equals(tenantId)) {
                return true; // SYSTEM tenant siempre tiene acceso
            }

            String expectedSchema = TenantContext.getSchemaName(tenantId);
            String currentSearchPath = getCurrentSearchPath();

            return currentSearchPath != null && currentSearchPath.contains(expectedSchema);
        } catch (Exception e) {
            log.warn("Error verificando aislamiento de schema: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el search_path actual de la sesión de base de datos.
     *
     * @return el valor actual de search_path, o null si no se puede obtener
     */
    public String getCurrentSearchPath() {
        try {
            List<String> paths = jdbcTemplate.query(
                    "SHOW search_path",
                    (rs, rowNum) -> rs.getString(1)
            );
            return paths.isEmpty() ? null : paths.get(0);
        } catch (Exception e) {
            log.warn("No se pudo obtener search_path: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene el schema esperado para el tenant actual.
     *
     * @return el nombre del schema esperado (e.g., "tenant_acme_corp")
     * @throws IllegalStateException si el tenant context no está configurado
     */
    public String getExpectedSchema() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        return TenantContext.getSchemaName(tenantId);
    }

    /**
     * Verifica si un schema específico existe en la base de datos.
     *
     * @param schemaName el nombre del schema a verificar
     * @return true si el schema existe, false en caso contrario
     */
    public boolean schemaExists(String schemaName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = ?",
                    Integer.class,
                    schemaName.toLowerCase()
            );
            return count != null && count > 0;
        } catch (Exception e) {
            log.warn("Error verificando existencia de schema {}: {}", schemaName, e.getMessage());
            return false;
        }
    }

    /**
     * Lista todos los schemas de tenant en la base de datos.
     *
     * @return lista de nombres de schemas de tenant
     */
    public List<String> listTenantSchemas() {
        try {
            return jdbcTemplate.query(
                    "SELECT schema_name FROM information_schema.schemata " +
                            "WHERE schema_name LIKE 'tenant_%' " +
                            "ORDER BY schema_name",
                    (rs, rowNum) -> rs.getString(1)
            );
        } catch (Exception e) {
            log.error("Error listando schemas de tenant: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Obtiene información detallada del estado de aislamiento para debugging.
     *
     * @return string con información de debug
     */
    public String getDebugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Schema Isolation Debug Info ===\n");

        try {
            String tenantId = TenantContext.getTenantId();
            sb.append("TenantContext: ").append(tenantId != null ? tenantId : "(not set)").append("\n");

            if (tenantId != null) {
                sb.append("Expected Schema: ").append(TenantContext.getSchemaName(tenantId)).append("\n");
            }

            String currentSearchPath = getCurrentSearchPath();
            sb.append("Current search_path: ").append(currentSearchPath != null ? currentSearchPath : "(unknown)").append("\n");

            sb.append("Isolation Active: ").append(isSchemaIsolationActive()).append("\n");

            sb.append("\nTenant Schemas in DB:\n");
            listTenantSchemas().forEach(schema -> sb.append("  - ").append(schema).append("\n"));

        } catch (Exception e) {
            sb.append("Error: ").append(e.getMessage());
        }

        return sb.toString();
    }
}