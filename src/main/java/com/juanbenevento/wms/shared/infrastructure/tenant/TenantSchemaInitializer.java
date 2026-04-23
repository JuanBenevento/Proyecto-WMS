package com.juanbenevento.wms.shared.infrastructure.tenant;

import com.juanbenevento.wms.shared.domain.valueobject.WmsConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Initializes and validates tenant schema on application startup.
 *
 * <p>This component ensures that the schema for the application's
 * default/system tenant exists before the application begins accepting
 * requests. It provides automatic schema provisioning as a safety net
 * in case schema creation was not triggered during initial tenant setup.
 *
 * <p>Behavior:
 * <ul>
 *   <li>On application startup, checks if the current tenant's schema exists</li>
 *   <li>If schema does not exist, creates it automatically</li>
 *   <li>Logs schema status for debugging and audit purposes</li>
 * </ul>
 *
 * <p>Note: This is a fallback mechanism. Primary schema creation should
 * occur during tenant provisioning in SaaSManagementService.
 *
 * @see TenantSchemaManager
 * @see TenantContext
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantSchemaInitializer {

    private final TenantSchemaManager schemaManager;

    @Value("${spring.jpa.properties.hibernate.default_schema:public}")
    private String defaultSchema;

    @Value("${tenant.auto-initialize-schema:true}")
    private boolean autoInitializeSchema;

    /**
     * Initializes the tenant schema on application startup.
     *
     * <p>This method is triggered by ApplicationReadyEvent, ensuring all
     * beans are initialized and the application context is fully loaded.
     *
     * <p>The initialization only proceeds if:
     * <ul>
     *   <li>Auto-initialize is enabled (tenant.auto-initialize-schema=true)</li>
     *   <li>A tenant context is set (not in system/migration mode)</li>
     *   <li>The schema does not already exist</li>
     * </ul>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (!autoInitializeSchema) {
            log.info("Tenant schema auto-initialization is disabled");
            return;
        }

        if (!TenantContext.isSet()) {
            log.info("No tenant context set - skipping schema initialization (system/migration mode)");
            return;
        }

        String tenantId = TenantContext.getTenantId();
        String schemaName = TenantContext.getSchemaName();

        log.info("=== Tenant Schema Initialization ===");
        log.info("Tenant ID: {}", tenantId);
        log.info("Schema Name: {}", schemaName);
        log.info("Default Schema: {}", defaultSchema);

        try {
            initializeSchemaForTenant(tenantId, schemaName);
        } catch (Exception e) {
            log.error("Failed to initialize tenant schema: {}", e.getMessage(), e);
            // Don't prevent application startup, but log the error
        }

        log.info("=== Tenant Schema Initialization Complete ===");
    }

    /**
     * Initializes the schema for a specific tenant.
     *
     * @param tenantId   the tenant identifier
     * @param schemaName the schema name to create/check
     */
    private void initializeSchemaForTenant(String tenantId, String schemaName) {
        if (schemaManager.schemaExists(schemaName)) {
            log.info("✅ Schema '{}' already exists for tenant '{}'", schemaName, tenantId);
            logSchemaContents(schemaName);
            return;
        }

        log.warn("⚠️ Schema '{}' does not exist for tenant '{}' - creating...", schemaName, tenantId);

        try {
            String createdSchema = schemaManager.createSchema(tenantId);
            log.info("✅ Schema '{}' created successfully", createdSchema);
        } catch (Exception e) {
            log.error("❌ Failed to create schema '{}': {}", schemaName, e.getMessage());
            throw e;
        }
    }

    /**
     * Logs basic schema information for debugging.
     *
     * @param schemaName the schema to inspect
     */
    private void logSchemaContents(String schemaName) {
        try {
            log.debug("Schema '{}' exists - application ready to serve tenant requests", schemaName);
        } catch (Exception e) {
            log.debug("Could not log schema contents: {}", e.getMessage());
        }
    }

    /**
     * Checks if a tenant's schema exists.
     *
     * @param tenantId the tenant identifier
     * @return true if the schema exists, false otherwise
     */
    public boolean isSchemaInitialized(String tenantId) {
        String schemaName = TenantContext.getSchemaName(tenantId);
        return schemaManager.schemaExists(schemaName);
    }
}