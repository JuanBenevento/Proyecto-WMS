package com.juanbenevento.wms.shared.infrastructure.tenant;

import com.juanbenevento.wms.shared.domain.valueobject.WmsConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TenantContext.
 * Tests the ThreadLocal-based tenant context management.
 */
@ExtendWith(MockitoExtension.class)
class TenantContextTest {

    @Mock
    private TenantSchemaManager schemaManager;

    @AfterEach
    void tearDown() {
        // Clean up after each test to prevent tenant leakage
        TenantContext.clear();
    }

    @Nested
    @DisplayName("Tenant ID Management Tests")
    class TenantIdManagementTests {

        @Test
        @DisplayName("Debe establecer y obtener tenant ID")
        void shouldSetAndGetTenantId() {
            // GIVEN
            String tenantId = "test-tenant";

            // WHEN
            TenantContext.setTenantId(tenantId);

            // THEN
            assertEquals(tenantId, TenantContext.getTenantId());
            assertTrue(TenantContext.isSet());
        }

        @Test
        @DisplayName("Debe retornar null cuando no hay tenant ID")
        void shouldReturnNullWhenNoTenantId() {
            // WHEN
            String result = TenantContext.getTenantId();

            // THEN
            assertNull(result);
        }

        @Test
        @DisplayName("Debe retornar false cuando no hay tenant ID")
        void shouldReturnFalseWhenNoTenantId() {
            // WHEN
            boolean result = TenantContext.isSet();

            // THEN
            assertFalse(result);
        }

        @Test
        @DisplayName("Debe limpiar tenant ID correctamente")
        void shouldClearTenantIdCorrectly() {
            // GIVEN
            TenantContext.setTenantId("test-tenant");
            assertNotNull(TenantContext.getTenantId());

            // WHEN
            TenantContext.clear();

            // THEN
            assertNull(TenantContext.getTenantId());
            assertFalse(TenantContext.isSet());
        }

        @Test
        @DisplayName("Debe limpiar solo tenant ID preservando schema")
        void shouldClearOnlyTenantIdPreservingSchema() {
            // GIVEN
            TenantContext.setTenantId("test-tenant");

            // WHEN
            TenantContext.clearTenantId();

            // THEN
            assertNull(TenantContext.getTenantId());
            assertFalse(TenantContext.isSet());
        }
    }

    @Nested
    @DisplayName("Schema Name Tests")
    class SchemaNameTests {

        @Test
        @DisplayName("Debe retornar schema name en formato correcto")
        void shouldReturnSchemaNameInCorrectFormat() {
            // GIVEN
            TenantContext.setTenantId("acme_corp");

            // WHEN
            String schemaName = TenantContext.getSchemaName();

            // THEN
            assertEquals("tenant_acme_corp", schemaName);
        }

        @Test
        @DisplayName("Debe normalizar tenant ID al construir schema name")
        void shouldNormalizeTenantIdWhenBuildingSchemaName() {
            // GIVEN - Mayúsculas y espacios
            TenantContext.setTenantId("ACME Corp");

            // WHEN
            String schemaName = TenantContext.getSchemaName();

            // THEN
            assertEquals("tenant_acme_corp", schemaName);
        }

        @Test
        @DisplayName("Debe retornar null para schema si no hay tenant")
        void shouldReturnNullForSchemaIfNoTenant() {
            // GIVEN
            assertFalse(TenantContext.isSet());

            // WHEN
            String schema = TenantContext.getCurrentSchema();

            // THEN
            assertNull(schema);
        }

        @Test
        @DisplayName("Debe lanzar excepción si no hay tenant ID")
        void shouldThrowExceptionIfNoTenantId() {
            // GIVEN - No hay tenant ID establecido

            // WHEN/THEN
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                TenantContext.getSchemaName();
            });

            assertEquals("Tenant ID not set in TenantContext", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Static Schema Name Helper Tests")
    class StaticSchemaNameHelperTests {

        @Test
        @DisplayName("Debe construir schema name desde tenant ID estático")
        void shouldBuildSchemaNameFromStaticTenantId() {
            // GIVEN
            String tenantId = "static_test";

            // WHEN
            String schemaName = TenantContext.getSchemaName(tenantId);

            // THEN
            assertEquals("tenant_static_test", schemaName);
        }

        @Test
        @DisplayName("Debe lanzar excepción para tenant ID null")
        void shouldThrowExceptionForNullTenantId() {
            // WHEN/THEN
            assertThrows(IllegalArgumentException.class, () -> {
                TenantContext.getSchemaName(null);
            });
        }

        @Test
        @DisplayName("Debe lanzar excepción para tenant ID vacío")
        void shouldThrowExceptionForEmptyTenantId() {
            // WHEN/THEN
            assertThrows(IllegalArgumentException.class, () -> {
                TenantContext.getSchemaName("");
            });
        }

        @Test
        @DisplayName("Debe lanzar excepción para tenant ID en blanco")
        void shouldThrowExceptionForBlankTenantId() {
            // WHEN/THEN
            assertThrows(IllegalArgumentException.class, () -> {
                TenantContext.getSchemaName("   ");
            });
        }
    }

    @Nested
    @DisplayName("Schema Creation Tests")
    class SchemaCreationTests {

        @Test
        @DisplayName("Debe crear schema a través del manager")
        void shouldCreateSchemaThroughManager() {
            // GIVEN
            TenantContext.setTenantId("new-tenant");
            String expectedSchema = "tenant_new_tenant";

            when(schemaManager.createSchema("new-tenant")).thenReturn(expectedSchema);

            // WHEN
            String result = TenantContext.getOrCreateSchema(schemaManager);

            // THEN
            assertEquals(expectedSchema, result);
            verify(schemaManager).createSchema("new-tenant");
        }

        @Test
        @DisplayName("Debe lanzar excepción si no hay tenant ID al crear schema")
        void shouldThrowExceptionIfNoTenantIdWhenCreatingSchema() {
            // GIVEN - No hay tenant ID

            // WHEN/THEN
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                TenantContext.getOrCreateSchema(schemaManager);
            });

            assertEquals("Cannot create schema: tenant ID not set", exception.getMessage());
            verify(schemaManager, never()).createSchema(any());
        }
    }

    @Nested
    @DisplayName("Clear Methods Tests")
    class ClearMethodsTests {

        @Test
        @DisplayName("Debe limpiar schema preservando tenant ID")
        void shouldClearSchemaPreservingTenantId() {
            // GIVEN
            TenantContext.setTenantId("test-tenant");
            assertNotNull(TenantContext.getCurrentSchema());

            // WHEN
            TenantContext.clearSchema();

            // THEN
            assertNull(TenantContext.getCurrentSchema());
            assertNotNull(TenantContext.getTenantId()); // Tenant ID preserved
        }

        @Test
        @DisplayName("Debe limpiar todo completamente")
        void shouldClearEverythingCompletely() {
            // GIVEN
            TenantContext.setTenantId("test-tenant");

            // WHEN
            TenantContext.clear();

            // THEN
            assertNull(TenantContext.getTenantId());
            assertNull(TenantContext.getCurrentSchema());
            assertFalse(TenantContext.isSet());
        }
    }
}