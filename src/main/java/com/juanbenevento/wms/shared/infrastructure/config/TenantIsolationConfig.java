package com.juanbenevento.wms.shared.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Configuration properties for schema-based multi-tenant isolation.
 *
 * This class provides type-safe access to tenant isolation settings
 * that can be configured via application.properties or environment variables.
 *
 * Configuration properties:
 * - wms.tenant.isolation.enabled: Enable/disable schema isolation
 * - wms.tenant.isolation.rls.enabled: Enable/disable Row-Level Security
 * - wms.tenant.schema.prefix: Prefix for tenant schema names
 *
 * Usage in code:
 * <pre>
 * {@code
 * @Autowired
 * private TenantIsolationConfig config;
 *
 * public void someMethod() {
 *     if (config.isEnabled()) {
 *         // Use schema isolation
 *     }
 * }
 * }
 * </pre>
 */
@Configuration
@ConfigurationProperties(prefix = "wms.tenant")
public class TenantIsolationConfig {

    private boolean isolationEnabled;
    private boolean rlsEnabled;
    private String schemaPrefix = "tenant_";

    /**
     * Creates a new TenantIsolationConfig.
     *
     * @param environment the Spring environment for binding properties
     * @return a new TenantIsolationConfig instance with properties bound
     */
    @Bean
    public static TenantIsolationConfig tenantIsolationConfig(Environment environment) {
        Binder binder = Binder.get(environment);
        return binder.bind("wms.tenant", TenantIsolationConfig.class)
                .orElseGet(TenantIsolationConfig::new);
    }

    /**
     * Default constructor.
     */
    public TenantIsolationConfig() {
    }

    /**
     * Checks if schema-based isolation is enabled.
     *
     * @return true if isolation is enabled, false otherwise
     */
    public boolean isIsolationEnabled() {
        return isolationEnabled;
    }

    /**
     * Sets whether schema-based isolation is enabled.
     *
     * @param isolationEnabled the enabled flag
     */
    public void setIsolationEnabled(boolean isolationEnabled) {
        this.isolationEnabled = isolationEnabled;
    }

    /**
     * Convenience method for checking isolation status.
     *
     * @return true if isolation is enabled
     */
    public boolean isEnabled() {
        return isolationEnabled;
    }

    /**
     * Checks if Row-Level Security is enabled.
     *
     * @return true if RLS is enabled, false otherwise
     */
    public boolean isRlsEnabled() {
        return rlsEnabled;
    }

    /**
     * Sets whether Row-Level Security is enabled.
     *
     * @param rlsEnabled the RLS enabled flag
     */
    public void setRlsEnabled(boolean rlsEnabled) {
        this.rlsEnabled = rlsEnabled;
    }

    /**
     * Gets the schema prefix for tenant schemas.
     *
     * @return the schema prefix (default: "tenant_")
     */
    public String getSchemaPrefix() {
        return schemaPrefix;
    }

    /**
     * Sets the schema prefix for tenant schemas.
     *
     * @param schemaPrefix the prefix to use before tenant ID
     */
    public void setSchemaPrefix(String schemaPrefix) {
        this.schemaPrefix = schemaPrefix;
    }

    /**
     * Generates the full schema name for a tenant.
     *
     * @param tenantId the tenant identifier
     * @return the full schema name (e.g., "tenant_abc123")
     */
    public String getSchemaName(String tenantId) {
        return schemaPrefix + tenantId;
    }

    @Override
    public String toString() {
        return "TenantIsolationConfig{" +
                "isolationEnabled=" + isolationEnabled +
                ", rlsEnabled=" + rlsEnabled +
                ", schemaPrefix='" + schemaPrefix + '\'' +
                '}';
    }
}