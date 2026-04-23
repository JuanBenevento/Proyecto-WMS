package com.juanbenevento.wms.shared.infrastructure.tenant;

import com.juanbenevento.wms.shared.domain.valueobject.WmsConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.Mockito.*;

/**
 * Unit tests for TenantConnectionFilter.
 * Tests the filter behavior for setting and resetting search_path.
 */
@ExtendWith(MockitoExtension.class)
class TenantConnectionFilterTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private TenantConnectionFilter tenantConnectionFilter;

    @BeforeEach
    void setUp() {
        // Clean up before each test
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        TenantContext.clear();
    }

    @Nested
    @DisplayName("Search Path Setting Tests")
    class SearchPathSettingTests {

        @Test
        @DisplayName("Debe establecer search_path para tenant")
        void shouldSetSearchPathForTenant() throws Exception {
            // GIVEN
            String tenantId = "test_tenant";
            TenantContext.setTenantId(tenantId);

            // WHEN
            tenantConnectionFilter.doFilterInternal(request, response, filterChain);

            // THEN
            verify(jdbcTemplate).execute("SET search_path TO tenant_test_tenant");
            verify(jdbcTemplate).execute("RESET search_path");
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Debe ejecutar filterChain antes de reset")
        void shouldExecuteFilterChainBeforeReset() throws Exception {
            // GIVEN
            String tenantId = "acme_corp";
            TenantContext.setTenantId(tenantId);

            // WHEN
            tenantConnectionFilter.doFilterInternal(request, response, filterChain);

            // THEN - Verify order: SET -> doFilter -> RESET
            var inOrder = inOrder(jdbcTemplate, filterChain);
            inOrder.verify(jdbcTemplate).execute("SET search_path TO tenant_acme_corp");
            inOrder.verify(filterChain).doFilter(request, response);
            inOrder.verify(jdbcTemplate).execute("RESET search_path");
        }

        @Test
        @DisplayName("Debe normalizar nombre de tenant correctamente")
        void shouldNormalizeTenantNameCorrectly() throws Exception {
            // GIVEN - Tenant ID con espacios y mayúsculas
            TenantContext.setTenantId("ACME Corp");

            // WHEN
            tenantConnectionFilter.doFilterInternal(request, response, filterChain);

            // THEN
            verify(jdbcTemplate).execute("SET search_path TO tenant_acme_corp");
        }
    }

    @Nested
    @DisplayName("Null Tenant Context Tests")
    class NullTenantContextTests {

        @Test
        @DisplayName("Debe omitir filter si no hay tenant context")
        void shouldSkipFilterIfNoTenantContext() throws Exception {
            // GIVEN - No hay tenant ID

            // WHEN
            tenantConnectionFilter.doFilterInternal(request, response, filterChain);

            // THEN
            verify(jdbcTemplate, never()).execute(contains("search_path"));
            verify(filterChain).doFilter(request, response);
        }

        @Test
        @DisplayName("Debe omitir filter para SYSTEM tenant")
        void shouldSkipFilterForSystemTenant() throws Exception {
            // GIVEN
            TenantContext.setTenantId(WmsConstants.SYSTEM_TENANT);

            // WHEN
            tenantConnectionFilter.doFilterInternal(request, response, filterChain);

            // THEN
            verify(jdbcTemplate, never()).execute(contains("search_path"));
            verify(filterChain).doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("Cleanup Tests")
    class CleanupTests {

        @Test
        @DisplayName("Debe limpiar search_path en finally")
        void shouldCleanupSearchPathInFinally() throws Exception {
            // GIVEN
            String tenantId = "cleanup_test";
            TenantContext.setTenantId(tenantId);

            // WHEN
            tenantConnectionFilter.doFilterInternal(request, response, filterChain);

            // THEN - RESET debe ejecutarse siempre
            verify(jdbcTemplate, atLeastOnce()).execute("RESET search_path");
        }

        @Test
        @DisplayName("Debe limpiar search_path incluso si filterChain lanza excepción")
        void shouldCleanupSearchPathEvenIfFilterChainThrows() throws Exception {
            // GIVEN
            String tenantId = "exception_test";
            TenantContext.setTenantId(tenantId);
            doThrow(new RuntimeException("Test exception")).when(filterChain).doFilter(request, response);

            // WHEN/THEN
            try {
                tenantConnectionFilter.doFilterInternal(request, response, filterChain);
            } catch (RuntimeException ignored) {
                // Se espera la excepción
            }

            // THEN - RESET debe ejecutarse a pesar de la excepción
            verify(jdbcTemplate).execute("RESET search_path");
        }
    }
}