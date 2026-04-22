package com.juanbenevento.wms.shared.infrastructure.tenant;

import com.juanbenevento.wms.shared.domain.valueobject.WmsConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * Thread-local based context for storing the current tenant ID.
 *
 * <p>This class provides tenant isolation at the application level by storing
 * the current tenant ID in a ThreadLocal variable. Combined with TenantConnectionFilter,
 * it enables schema-based multi-tenant isolation in PostgreSQL.
 *
 * <p>Usage:
 * <pre>
 * TenantContext.setTenantId("acme_corp");
 * try {
 *     // All queries execute in tenant_acme_corp schema
 *     List&lt;InventoryItem&gt; items = inventoryRepository.findAll();
 * } finally {
 *     TenantContext.clear();
 * }
 * </pre>
 *
 * @see TenantConnectionFilter
 * @see WmsConstants#TENANT_SCHEMA_PREFIX
 */
@Slf4j
public class TenantContext {
    
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    
    // Optional: Store schema name separately for explicit management
    private static final ThreadLocal<String> CURRENT_SCHEMA = new ThreadLocal<>();

    /**
     * Sets the current tenant ID in the context.
     *
     * @param tenantId the tenant identifier to set
     */
    public static void setTenantId(String tenantId) {
        log.debug("Setting Tenant Context: {}", tenantId);
        CURRENT_TENANT.set(tenantId);
        
        // Also set the schema name for convenient access
        if (tenantId != null) {
            String schemaName = getSchemaName();
            CURRENT_SCHEMA.set(schemaName);
            log.trace("Tenant schema set to: {}", schemaName);
        }
    }

    /**
     * Gets the current tenant ID from the context.
     *
     * @return the current tenant ID, or null if not set
     */
    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    /**
     * Gets the schema name for the current tenant.
     *
     * <p>Format: {@code tenant_{normalizedTenantId}}
     * Example: "acme_corp" → "tenant_acme_corp"
     *
     * @return the schema name prefixed with TENANT_SCHEMA_PREFIX
     * @throws IllegalStateException if tenant ID is not set
     */
    public static String getSchemaName() {
        String tenantId = CURRENT_TENANT.get();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant ID not set in TenantContext");
        }
        
        String normalized = WmsConstants.normalizeTenantId(tenantId);
        return WmsConstants.TENANT_SCHEMA_PREFIX + normalized;
    }
    
    /**
     * Gets the schema name for a specific tenant ID.
     *
     * <p>Useful for utilities and management operations where the context
     * may not be set.
     *
     * @param tenantId the tenant identifier
     * @return the schema name
     */
    public static String getSchemaName(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant ID cannot be null or blank");
        }
        
        String normalized = WmsConstants.normalizeTenantId(tenantId);
        return WmsConstants.TENANT_SCHEMA_PREFIX + normalized;
    }

    /**
     * Gets the current schema name from ThreadLocal.
     *
     * <p>This is a convenience method that returns the schema name
     * that was set when setTenantId was called.
     *
     * @return the current schema name, or null if not set
     */
    public static String getCurrentSchema() {
        return CURRENT_SCHEMA.get();
    }

    /**
     * Checks if a tenant ID is currently set in the context.
     *
     * @return true if tenant ID is set, false otherwise
     */
    public static boolean isSet() {
        return CURRENT_TENANT.get() != null;
    }
    
    /**
     * Creates the tenant schema if it doesn't exist.
     *
     * <p>This method uses TenantSchemaManager to create the PostgreSQL
     * schema for the current tenant. Should be called during tenant
     * provisioning or on first request to ensure the schema exists.
     *
     * @param schemaManager the TenantSchemaManager service
     * @return the schema name that was created or already exists
     * @throws IllegalStateException if tenant ID is not set
     */
    public static String getOrCreateSchema(TenantSchemaManager schemaManager) {
        String tenantId = CURRENT_TENANT.get();
        if (tenantId == null) {
            throw new IllegalStateException("Cannot create schema: tenant ID not set");
        }
        
        return schemaManager.createSchema(tenantId);
    }

    /**
     * Clears both tenant ID and schema from the context.
     *
     * <p>Should always be called in finally blocks to prevent
     * tenant context leakage between requests/threads.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
        CURRENT_SCHEMA.remove();
        log.trace("Tenant context cleared");
    }
    
    /**
     * Clears only the tenant ID, preserving schema for explicit management.
     */
    public static void clearTenantId() {
        CURRENT_TENANT.remove();
        log.trace("Tenant ID cleared (schema preserved)");
    }
    
    /**
     * Clears only the schema.
     */
    public static void clearSchema() {
        CURRENT_SCHEMA.remove();
        log.trace("Schema cleared");
    }
}