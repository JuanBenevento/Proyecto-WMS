package com.juanbenevento.wms.shared.infrastructure.tenant;

import com.juanbenevento.wms.shared.domain.valueobject.WmsConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for managing PostgreSQL schemas for multi-tenant isolation.
 * Each tenant gets its own schema (tenant_{tenantId}) for complete data isolation.
 *
 * <p>This service provides schema lifecycle operations:
 * <ul>
 *   <li>createSchema - Creates a new schema for a tenant</li>
 *   <li>dropSchema - Removes a tenant schema (use with caution)</li>
 *   <li>schemaExists - Checks if a schema already exists</li>
 *   <li>normalizeSchemaName - Converts tenant ID to schema name format</li>
 * </ul>
 *
 * <p>Schema naming follows the format: {@code tenant_{normalizedTenantId}}
 *
 * @see WmsConstants#TENANT_SCHEMA_PREFIX
 * @see WmsConstants#normalizeTenantId(String)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantSchemaManager {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a PostgreSQL schema for the specified tenant.
     *
     * <p>The schema name is derived from the tenant ID using the pattern:
     * {@code tenant_{normalizeTenantId(tenantId)}}
     *
     * <p>Example:
     * <ul>
     *   <li>Input: "ACME Corp" → Schema: "tenant_acme_corp"</li>
     *   <li>Input: "tenant-123" → Schema: "tenant_tenant_123"</li>
     * </ul>
     *
     * @param tenantId the tenant identifier to create schema for
     * @return the name of the created schema
     * @throws IllegalStateException if schema creation fails or schema already exists
     */
    @Transactional
    public String createSchema(String tenantId) {
        String schemaName = buildSchemaName(tenantId);
        
        log.info("Creating schema '{}' for tenant '{}'", schemaName, tenantId);

        if (schemaExists(schemaName)) {
            log.warn("Schema '{}' already exists for tenant '{}'", schemaName, tenantId);
            return schemaName;
        }

        try {
            // Create the schema
            jdbcTemplate.execute(String.format("CREATE SCHEMA IF NOT EXISTS %s", schemaName));
            log.debug("Schema '{}' created successfully", schemaName);

            // Grant usage permissions to application user
            grantSchemaPermissions(schemaName);

            log.info("Schema '{}' created and permissions granted for tenant '{}'", schemaName, tenantId);
            return schemaName;
        } catch (Exception e) {
            log.error("Failed to create schema '{}' for tenant '{}': {}", schemaName, tenantId, e.getMessage());
            throw new IllegalStateException("Failed to create schema: " + schemaName, e);
        }
    }

    /**
     * Drops a PostgreSQL schema for the specified tenant.
     *
     * <p>WARNING: This operation is destructive and will remove all objects
     * within the schema including tables, indexes, and data.
     *
     * @param tenantId the tenant identifier whose schema should be dropped
     * @throws IllegalStateException if schema drop fails
     */
    @Transactional
    public void dropSchema(String tenantId) {
        String schemaName = buildSchemaName(tenantId);
        
        log.warn("Dropping schema '{}' for tenant '{}'", schemaName, tenantId);

        if (!schemaExists(schemaName)) {
            log.warn("Schema '{}' does not exist, nothing to drop", schemaName);
            return;
        }

        try {
            // CASCADE ensures all objects in schema are dropped
            jdbcTemplate.execute(String.format("DROP SCHEMA %s CASCADE", schemaName));
            log.info("Schema '{}' dropped successfully", schemaName);
        } catch (Exception e) {
            log.error("Failed to drop schema '{}' for tenant '{}': {}", schemaName, tenantId, e.getMessage());
            throw new IllegalStateException("Failed to drop schema: " + schemaName, e);
        }
    }

    /**
     * Checks whether a schema exists in the database.
     *
     * @param schemaName the schema name to check (can be full name with tenant_ prefix)
     * @return true if the schema exists, false otherwise
     */
    public boolean schemaExists(String schemaName) {
        String normalizedName = normalizeSchemaName(schemaName);
        
        String sql = """
            SELECT EXISTS (
                SELECT 1 FROM information_schema.schemata 
                WHERE schema_name = ?
            )
            """;

        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, normalizedName);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Checks whether a schema exists for the specified tenant.
     *
     * @param tenantId the tenant identifier
     * @return true if the tenant's schema exists, false otherwise
     */
    public boolean schemaExistsForTenant(String tenantId) {
        return schemaExists(buildSchemaName(tenantId));
    }

    /**
     * Builds the schema name for a given tenant ID.
     *
     * @param tenantId the tenant identifier
     * @return the fully qualified schema name (e.g., "tenant_acme_corp")
     */
    public String buildSchemaName(String tenantId) {
        String normalized = WmsConstants.normalizeTenantId(tenantId);
        return WmsConstants.TENANT_SCHEMA_PREFIX + normalized;
    }

    /**
     * Normalizes a schema name to ensure consistent format.
     *
     * <p>If the input already has the tenant_ prefix, it returns the input.
     * Otherwise, it adds the prefix.
     *
     * @param schemaName the schema name to normalize
     * @return normalized schema name
     */
    public String normalizeSchemaName(String schemaName) {
        if (schemaName == null || schemaName.isBlank()) {
            throw new IllegalArgumentException("Schema name cannot be null or blank");
        }
        
        String normalized = schemaName.trim().toLowerCase();
        if (!normalized.startsWith(WmsConstants.TENANT_SCHEMA_PREFIX)) {
            normalized = WmsConstants.TENANT_SCHEMA_PREFIX + normalized;
        }
        return normalized;
    }

    /**
     * Grants the necessary permissions to the application user within a schema.
     *
     * <p>Permissions granted:
     * <ul>
     *   <li>USAGE - Required to access objects in the schema</li>
     *   <li>ALL PRIVILEGES on tables - Full access to tables</li>
     *   <li>ALL PRIVILEGES on sequences - Required for auto-increment columns</li>
     * </ul>
     *
     * <p>Also sets default privileges for future objects created in the schema.
     *
     * @param schemaName the schema to grant permissions for
     */
    private void grantSchemaPermissions(String schemaName) {
        // Get the application user from datasource configuration
        // For now, we grant to current_user (the user executing the statement)
        String grantSql = String.format("GRANT USAGE ON SCHEMA %s TO CURRENT_USER", schemaName);
        jdbcTemplate.execute(grantSql);

        String tableGrantSql = String.format(
            "GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA %s TO CURRENT_USER", schemaName);
        jdbcTemplate.execute(tableGrantSql);

        String sequenceGrantSql = String.format(
            "GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA %s TO CURRENT_USER", schemaName);
        jdbcTemplate.execute(sequenceGrantSql);

        // Set default privileges for future objects
        String defaultTablePrivs = String.format(
            "ALTER DEFAULT PRIVILEGES IN SCHEMA %s GRANT ALL ON TABLES TO CURRENT_USER", schemaName);
        jdbcTemplate.execute(defaultTablePrivs);

        String defaultSeqPrivs = String.format(
            "ALTER DEFAULT PRIVILEGES IN SCHEMA %s GRANT ALL ON SEQUENCES TO CURRENT_USER", schemaName);
        jdbcTemplate.execute(defaultSeqPrivs);

        log.debug("Permissions granted for schema '{}'", schemaName);
    }
}