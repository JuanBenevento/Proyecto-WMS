package com.juanbenevento.wms.shared.infrastructure.tenant;

import com.juanbenevento.wms.shared.domain.valueobject.WmsConstants;
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
import org.springframework.jdbc.core.RowMapper;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SchemaIsolationValidator.
 * Tests validation and debugging methods for tenant schema isolation.
 */
@ExtendWith(MockitoExtension.class)
class SchemaIsolationValidatorTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private SchemaIsolationValidator schemaIsolationValidator;

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
    @DisplayName("Schema Validation Tests")
    class SchemaValidationTests {

        @Test
        @DisplayName("Debe pasar validación cuando search_path coincide con schema esperado")
        void shouldPassValidationWhenSearchPathMatchesExpectedSchema() {
            // GIVEN
            String tenantId = "test_tenant";
            TenantContext.setTenantId(tenantId);

            when(jdbcTemplate.queryForObject(
                    eq("SHOW search_path"),
                    eq(String.class)
            )).thenReturn("tenant_test_tenant");

            // WHEN/THEN - No exception should be thrown
            assertDoesNotThrow(() -> schemaIsolationValidator.validateCurrentSchema());
        }

        @Test
        @DisplayName("Debe lanzar excepción si tenant context no está configurado")
        void shouldThrowExceptionIfTenantContextNotConfigured() {
            // GIVEN - No tenant ID

            // WHEN/THEN
            assertThrows(IllegalStateException.class, () -> {
                schemaIsolationValidator.validateCurrentSchema();
            });
        }

        @Test
        @DisplayName("Debe omitir validación para SYSTEM tenant")
        void shouldOmitValidationForSystemTenant() {
            // GIVEN
            TenantContext.setTenantId(WmsConstants.SYSTEM_TENANT);

            // WHEN/THEN - No exception should be thrown
            assertDoesNotThrow(() -> schemaIsolationValidator.validateCurrentSchema());
        }

        @Test
        @DisplayName("Debe lanzar SecurityException si aislamiento está comprometido")
        void shouldThrowSecurityExceptionIfIsolationCompromised() {
            // GIVEN
            String tenantId = "test_tenant";
            TenantContext.setTenantId(tenantId);

            // search_path no coincide con lo esperado
            when(jdbcTemplate.query(
                    eq("SHOW search_path"),
                    any(RowMapper.class)
            )).thenReturn(List.of("public")); // Espera tenant_test_tenant pero obtiene public

            // WHEN/THEN
            SecurityException exception = assertThrows(SecurityException.class, () -> {
                schemaIsolationValidator.validateCurrentSchema();
            });

            assertTrue(exception.getMessage().contains("AISlamiento DE SCHEMA COMPROMETIDO"));
        }
    }

    @Nested
    @DisplayName("Isolation Status Tests")
    class IsolationStatusTests {

        @Test
        @DisplayName("Debe retornar true si aislamiento está activo")
        void shouldReturnTrueIfIsolationIsActive() {
            // GIVEN
            TenantContext.setTenantId("active_tenant");

            when(jdbcTemplate.query(
                    eq("SHOW search_path"),
                    any(RowMapper.class)
            )).thenReturn(List.of("tenant_active_tenant"));

            // WHEN
            boolean result = schemaIsolationValidator.isSchemaIsolationActive();

            // THEN
            assertTrue(result);
        }

        @Test
        @DisplayName("Debe retornar false si aislamiento no está activo")
        void shouldReturnFalseIfIsolationIsNotActive() {
            // GIVEN
            TenantContext.setTenantId("inactive_tenant");

            when(jdbcTemplate.query(
                    eq("SHOW search_path"),
                    any(RowMapper.class)
            )).thenReturn(List.of("public")); // No coincide

            // WHEN
            boolean result = schemaIsolationValidator.isSchemaIsolationActive();

            // THEN
            assertFalse(result);
        }

        @Test
        @DisplayName("Debe retornar true para SYSTEM tenant")
        void shouldReturnTrueForSystemTenant() {
            // GIVEN
            TenantContext.setTenantId(WmsConstants.SYSTEM_TENANT);

            // WHEN
            boolean result = schemaIsolationValidator.isSchemaIsolationActive();

            // THEN
            assertTrue(result);
        }

        @Test
        @DisplayName("Debe retornar true si no hay tenant ID")
        void shouldReturnTrueIfNoTenantId() {
            // GIVEN - No hay tenant ID

            // WHEN
            boolean result = schemaIsolationValidator.isSchemaIsolationActive();

            // THEN
            assertTrue(result);
        }

        @Test
        @DisplayName("Debe retornar false si ocurre error")
        void shouldReturnFalseIfErrorOccurs() {
            // GIVEN
            TenantContext.setTenantId("error_tenant");

            when(jdbcTemplate.query(
                    eq("SHOW search_path"),
                    any(RowMapper.class)
            )).thenThrow(new RuntimeException("Connection error"));

            // WHEN
            boolean result = schemaIsolationValidator.isSchemaIsolationActive();

            // THEN
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Current Search Path Tests")
    class CurrentSearchPathTests {

        @Test
        @DisplayName("Debe obtener search_path actual")
        void shouldGetCurrentSearchPath() {
            // GIVEN
            when(jdbcTemplate.query(
                    eq("SHOW search_path"),
                    any(RowMapper.class)
            )).thenReturn(List.of("tenant_test_schema, public"));

            // WHEN
            String result = schemaIsolationValidator.getCurrentSearchPath();

            // THEN
            assertEquals("tenant_test_schema, public", result);
        }

        @Test
        @DisplayName("Debe retornar null si no hay resultados")
        void shouldReturnNullIfNoResults() {
            // GIVEN
            when(jdbcTemplate.query(
                    eq("SHOW search_path"),
                    any(RowMapper.class)
            )).thenReturn(Collections.emptyList());

            // WHEN
            String result = schemaIsolationValidator.getCurrentSearchPath();

            // THEN
            assertNull(result);
        }

        @Test
        @DisplayName("Debe retornar null si hay excepción")
        void shouldReturnNullIfExceptionOccurs() {
            // GIVEN
            when(jdbcTemplate.query(
                    eq("SHOW search_path"),
                    any(RowMapper.class)
            )).thenThrow(new RuntimeException("DB error"));

            // WHEN
            String result = schemaIsolationValidator.getCurrentSearchPath();

            // THEN
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Expected Schema Tests")
    class ExpectedSchemaTests {

        @Test
        @DisplayName("Debe obtener schema esperado")
        void shouldGetExpectedSchema() {
            // GIVEN
            TenantContext.setTenantId("expected_tenant");

            // WHEN
            String result = schemaIsolationValidator.getExpectedSchema();

            // THEN
            assertEquals("tenant_expected_tenant", result);
        }

        @Test
        @DisplayName("Debe lanzar excepción si no hay tenant ID")
        void shouldThrowExceptionIfNoTenantId() {
            // GIVEN - No hay tenant ID

            // WHEN/THEN
            assertThrows(IllegalStateException.class, () -> {
                schemaIsolationValidator.getExpectedSchema();
            });
        }
    }

    @Nested
    @DisplayName("Schema Existence Tests")
    class SchemaExistenceTests {

        @Test
        @DisplayName("Debe retornar true si schema existe")
        void shouldReturnTrueIfSchemaExists() {
            // GIVEN
            when(jdbcTemplate.queryForObject(
                    any(String.class),
                    eq(Integer.class),
                    any()
            )).thenReturn(1);

            // WHEN
            boolean result = schemaIsolationValidator.schemaExists("tenant_test_schema");

            // THEN
            assertTrue(result);
        }

        @Test
        @DisplayName("Debe retornar false si schema no existe")
        void shouldReturnFalseIfSchemaDoesNotExist() {
            // GIVEN
            when(jdbcTemplate.queryForObject(
                    any(String.class),
                    eq(Integer.class),
                    any()
            )).thenReturn(0);

            // WHEN
            boolean result = schemaIsolationValidator.schemaExists("nonexistent_schema");

            // THEN
            assertFalse(result);
        }

        @Test
        @DisplayName("Debe retornar false si hay excepción")
        void shouldReturnFalseIfExceptionOccurs() {
            // GIVEN
            when(jdbcTemplate.queryForObject(
                    any(String.class),
                    eq(Integer.class),
                    any()
            )).thenThrow(new RuntimeException("DB error"));

            // WHEN
            boolean result = schemaIsolationValidator.schemaExists("error_schema");

            // THEN
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Debug Info Tests")
    class DebugInfoTests {

        @Test
        @DisplayName("Debe retornar información de debug completa")
        void shouldReturnCompleteDebugInfo() {
            // GIVEN
            TenantContext.setTenantId("debug_tenant");

            when(jdbcTemplate.query(
                    eq("SHOW search_path"),
                    any(RowMapper.class)
            )).thenReturn(List.of("tenant_debug_tenant"));

            when(jdbcTemplate.query(
                    any(String.class),
                    any(RowMapper.class)
            )).thenReturn(List.of("tenant_debug_tenant", "tenant_other_tenant"));

            // WHEN
            String debugInfo = schemaIsolationValidator.getDebugInfo();

            // THEN
            assertTrue(debugInfo.contains("=== Schema Isolation Debug Info ==="));
            assertTrue(debugInfo.contains("TenantContext: debug_tenant"));
            assertTrue(debugInfo.contains("Expected Schema: tenant_debug_tenant"));
            assertTrue(debugInfo.contains("Current search_path: tenant_debug_tenant"));
            assertTrue(debugInfo.contains("Isolation Active: true"));
            assertTrue(debugInfo.contains("tenant_debug_tenant"));
        }

        @Test
        @DisplayName("Debe mostrar not set si no hay tenant")
        void shouldShowNotSetIfNoTenant() {
            // GIVEN - No hay tenant ID

            // WHEN
            String debugInfo = schemaIsolationValidator.getDebugInfo();

            // THEN
            assertTrue(debugInfo.contains("TenantContext: (not set)"));
        }
    }
}