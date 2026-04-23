package com.juanbenevento.wms.shared.infrastructure.config;

import com.juanbenevento.wms.shared.infrastructure.tenant.TenantContext;
import com.juanbenevento.wms.shared.infrastructure.tenant.SchemaIsolationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health indicator for multi-tenant isolation status.
 * 
 * Reports:
 * - Whether schema isolation is enabled
 * - Whether RLS is enabled
 * - Current tenant context status
 * 
 * Access: GET /actuator/health/tenant-isolation
 */
@Component("tenantIsolation")
@RequiredArgsConstructor
@Slf4j
public class TenantIsolationHealthIndicator implements HealthIndicator {

    private final SchemaIsolationValidator validator;

    @Override
    public Health health() {
        try {
            boolean isolationEnabled = Boolean.parseBoolean(
                System.getProperty("wms.tenant.isolation.enabled", "false"));
            boolean rlsEnabled = Boolean.parseBoolean(
                System.getProperty("wms.tenant.rls.enabled", "false"));
            
            String currentTenant = TenantContext.getTenantId();
            boolean contextSet = currentTenant != null;
            
            boolean isolationActive = contextSet && isolationEnabled;
            
            if (isolationEnabled && rlsEnabled) {
                return Health.up()
                    .withDetail("isolation", "schema-based with RLS")
                    .withDetail("currentTenant", currentTenant != null ? currentTenant : "system")
                    .withDetail("rls", "enabled")
                    .build();
            } else if (isolationEnabled) {
                return Health.up()
                    .withDetail("isolation", "schema-based (RLS disabled)")
                    .withDetail("currentTenant", currentTenant != null ? currentTenant : "system")
                    .withDetail("rls", "disabled")
                    .build();
            } else {
                return Health.up()
                    .withDetail("isolation", "legacy (tenant_id column)")
                    .withDetail("warning", "Schema isolation not enabled")
                    .build();
            }
        } catch (Exception e) {
            log.error("Error checking tenant isolation health", e);
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}