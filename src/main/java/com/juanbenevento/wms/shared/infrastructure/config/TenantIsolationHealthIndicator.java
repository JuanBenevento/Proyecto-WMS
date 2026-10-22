package com.juanbenevento.wms.shared.infrastructure.config;

import com.juanbenevento.wms.shared.infrastructure.tenant.TenantAwareDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Health indicator HONESTO del aislamiento multi-tenant (S4).
 *
 * <p>Reglas:
 * <ul>
 *   <li>{@code wms.tenant.isolation.enabled=false} → <b>DOWN</b>: no hay garantía
 *       de aislamiento; el detalle lo dice explícitamente.</li>
 *   <li>Flag true pero {@link TenantAwareDataSource} NO registrado en el pool →
 *       <b>DOWN</b>: configuración rota (el flag promete algo que no está cableado).</li>
 *   <li>Flag true y wrapper registrado → <b>UP</b>: S1+S2 activos.</li>
 *   <li>RLS se reporta {@code not-implemented}: no existe implementación en Java;
 *       el indicator no inventa estados.</li>
 * </ul>
 *
 * <p>Acceso: GET /actuator/health/tenant-isolation
 */
@Component("tenantIsolation")
@RequiredArgsConstructor
@Slf4j
public class TenantIsolationHealthIndicator implements HealthIndicator {

    private final Environment environment;
    private final ObjectProvider<DataSource> dataSourceProvider;

    @Override
    public Health health() {
        boolean isolationEnabled = environment.getProperty(
                "wms.tenant.isolation.enabled", Boolean.class, true);
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        boolean wrapperRegistered = dataSource instanceof TenantAwareDataSource;

        if (!isolationEnabled) {
            log.debug("Tenant isolation health: DOWN (isolation disabled)");
            return Health.down()
                    .withDetail("isolation", "disabled")
                    .withDetail("reason", "wms.tenant.isolation.enabled=false — no schema isolation guarantee")
                    .withDetail("rls", "not-implemented")
                    .build();
        }

        if (!wrapperRegistered) {
            log.warn("Tenant isolation health: DOWN (flag enabled but TenantAwareDataSource not registered)");
            return Health.down()
                    .withDetail("isolation", "enabled but wrapper missing")
                    .withDetail("reason", "TenantAwareDataSource is not the active DataSource — check TenantDataSourceConfiguration")
                    .withDetail("rls", "not-implemented")
                    .build();
        }

        return Health.up()
                .withDetail("isolation", "schema-based (fail-closed)")
                .withDetail("wrapper", "TenantAwareDataSource")
                .withDetail("rls", "not-implemented")
                .build();
    }

    /** Expone el estado como Status por si otros componentes quieren consultarlo. */
    public Status currentStatus() {
        return health().getStatus();
    }
}
