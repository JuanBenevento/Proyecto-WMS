package com.juanbenevento.wms.shared.infrastructure.config;

import com.juanbenevento.wms.shared.infrastructure.tenant.TenantAwareDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Tests del health indicator de aislamiento tenant (S4).
 * Debe reflejar el estado REAL: DOWN cuando el aislamiento no puede
 * garantizarse (flag apagado o wrapper ausente), UP solo con S1+S2 activos.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TenantIsolationHealthIndicatorTest {

    @Mock
    private Environment environment;

    @Mock
    private ObjectProvider<DataSource> dataSourceProvider;

    @Mock
    private DataSource plainDataSource;

    private TenantIsolationHealthIndicator indicator() {
        return new TenantIsolationHealthIndicator(environment, dataSourceProvider);
    }

    private void setFlag(boolean value) {
        when(environment.getProperty("wms.tenant.isolation.enabled", Boolean.class, true))
                .thenReturn(value);
    }

    @Test
    @DisplayName("S4: flag apagado → DOWN (no puede garantizar aislamiento)")
    void isolationDisabled_reportsDown() {
        setFlag(false);
        when(dataSourceProvider.getIfAvailable()).thenReturn(plainDataSource);

        var health = indicator().health();

        assertEquals(org.springframework.boot.actuate.health.Status.DOWN, health.getStatus(),
                "Aislamiento deshabilitado debe reportar DOWN, no UP con warning");
    }

    @Test
    @DisplayName("S4: flag prendido pero wrapper ausente → DOWN (config rota)")
    void isolationEnabledButWrapperMissing_reportsDown() {
        setFlag(true);
        when(dataSourceProvider.getIfAvailable()).thenReturn(plainDataSource);

        var health = indicator().health();

        assertEquals(org.springframework.boot.actuate.health.Status.DOWN, health.getStatus(),
                "Flag true sin TenantAwareDataSource registrado es una configuración rota → DOWN");
    }

    @Test
    @DisplayName("S4: flag prendido y wrapper registrado → UP")
    void isolationEnabledAndWrapperPresent_reportsUp() {
        setFlag(true);
        when(dataSourceProvider.getIfAvailable())
                .thenReturn(new TenantAwareDataSource(plainDataSource, true));

        var health = indicator().health();

        assertEquals(org.springframework.boot.actuate.health.Status.UP, health.getStatus(),
                "Con flag true y wrapper registrado el aislamiento está activo → UP");
    }

    @Test
    @DisplayName("S4: RLS reportado como not-implemented (no hay implementación)")
    void rlsReportedAsNotImplemented() {
        setFlag(true);
        when(dataSourceProvider.getIfAvailable())
                .thenReturn(new TenantAwareDataSource(plainDataSource, true));

        var health = indicator().health();

        assertEquals("not-implemented", health.getDetails().get("rls"),
                "No existe implementación de RLS en Java — el indicator no debe mentir con 'enabled/disabled'");
    }
}
