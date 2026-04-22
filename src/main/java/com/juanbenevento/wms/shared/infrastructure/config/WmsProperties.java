package com.juanbenevento.wms.shared.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for WMS multi-tenant isolation.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "wms")
public class WmsProperties {

    private final Tenant tenant = new Tenant();

    @Data
    public static class Tenant {
        /**
         * Enable schema-based tenant isolation.
         * When true, each tenant gets their own PostgreSQL schema.
         */
        private boolean isolationEnabled = false;

        /**
         * Enable Row-Level Security policies.
         * Defense-in-depth for audit compliance.
         */
        private boolean rlsEnabled = false;

        /**
         * Prefix for tenant schema names.
         * Default: "tenant_"
         * Final format: tenant_{tenantId}
         */
        private String schemaPrefix = "tenant_";
    }
}