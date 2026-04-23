package com.juanbenevento.wms.shared.infrastructure.tenant;

import com.juanbenevento.wms.shared.domain.valueobject.WmsConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TenantSchemaManager.
 * Tests schema lifecycle operations (create, drop, exists) and normalization logic.
 */
@ExtendWith(MockitoExtension.class)
class TenantSchemaManagerTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private TenantSchemaManager tenantSchemaManager;

    private static final String TEST_SCHEMA = "tenant_test_schema";

    @Nested
    @DisplayName("Schema Creation Tests")
    class SchemaCreationTests {

        @Test
        @DisplayName("Debe crear schema con nombre correcto")
        void shouldCreateSchemaWithCorrectName() {
            // GIVEN
            String tenantId = "test_schema";
            String expectedSchema = "tenant_test_schema";

            when(jdbcTemplate.queryForObject(
                    any(String.class),
                    eq(Boolean.class),
                    any()
            )).thenReturn(false);

            // WHEN
            String result = tenantSchemaManager.createSchema(tenantId);

            // THEN
            assertEquals(expectedSchema, result);
            verify(jdbcTemplate).execute("CREATE SCHEMA IF NOT EXISTS " + expectedSchema);
        }

        @Test
        @DisplayName("Debe retornar schema existente sin recrear")
        void shouldReturnExistingSchemaWithoutRecreating() {
            // GIVEN
            String tenantId = "test_schema";

            when(jdbcTemplate.queryForObject(
                    any(String.class),
                    eq(Boolean.class),
                    any()
            )).thenReturn(true);

            // WHEN
            String result = tenantSchemaManager.createSchema(tenantId);

            // THEN
            assertEquals("tenant_test_schema", result);
            verify(jdbcTemplate, never()).execute(any(String.class));
        }

        @Test
        @DisplayName("Debe normalizar tenant ID con espacios y caracteres especiales")
        void shouldNormalizeTenantIdWithSpacesAndSpecialChars() {
            // GIVEN - tenant ID con espacios, mayúsculas y guiones
            String tenantId = "ACME Corp";
            String expectedSchema = "tenant_acme_corp";

            when(jdbcTemplate.queryForObject(
                    any(String.class),
                    eq(Boolean.class),
                    any()
            )).thenReturn(false);

            // WHEN
            String result = tenantSchemaManager.createSchema(tenantId);

            // THEN
            assertEquals(expectedSchema, result);
        }

        @Test
        @DisplayName("Debe ejecutar GRANT statements después de crear schema")
        void shouldExecuteGrantStatementsAfterCreatingSchema() {
            // GIVEN
            String tenantId = "grant_test";
            String expectedSchema = "tenant_grant_test";

            when(jdbcTemplate.queryForObject(
                    any(String.class),
                    eq(Boolean.class),
                    any()
            )).thenReturn(false);

            // WHEN
            tenantSchemaManager.createSchema(tenantId);

            // THEN - Verify grant statements were executed
            verify(jdbcTemplate).execute(contains("GRANT USAGE ON SCHEMA " + expectedSchema));
            verify(jdbcTemplate).execute(contains("GRANT ALL PRIVILEGES ON ALL TABLES"));
            verify(jdbcTemplate).execute(contains("GRANT ALL PRIVILEGES ON ALL SEQUENCES"));
        }
    }

    @Nested
    @DisplayName("Schema Drop Tests")
    class SchemaDropTests {

        @Test
        @DisplayName("Debe ejecutar DROP SCHEMA CASCADE")
        void shouldExecuteDropSchemaCascade() {
            // GIVEN
            String tenantId = "drop_test";
            String expectedSchema = "tenant_drop_test";

            when(jdbcTemplate.queryForObject(
                    any(String.class),
                    eq(Boolean.class),
                    any()
            )).thenReturn(true);

            // WHEN
            tenantSchemaManager.dropSchema(tenantId);

            // THEN
            verify(jdbcTemplate).execute("DROP SCHEMA " + expectedSchema + " CASCADE");
        }

        @Test
        @DisplayName("Debe ignorar si schema no existe")
        void shouldIgnoreIfSchemaDoesNotExist() {
            // GIVEN
            String tenantId = "nonexistent";

            when(jdbcTemplate.queryForObject(
                    any(String.class),
                    eq(Boolean.class),
                    any()
            )).thenReturn(false);

            // WHEN
            tenantSchemaManager.dropSchema(tenantId);

            // THEN - No DROP executed
            verify(jdbcTemplate, never()).execute(startsWith("DROP SCHEMA"));
        }
    }

    @Nested
    @DisplayName("Schema Existence Tests")
    class SchemaExistenceTests {

        @Test
        @DisplayName("Debe retornar true cuando schema existe")
        void shouldReturnTrueWhenSchemaExists() {
            // GIVEN
            when(jdbcTemplate.queryForObject(
                    any(String.class),
                    eq(Boolean.class),
                    eq(TEST_SCHEMA)
            )).thenReturn(true);

            // WHEN
            boolean result = tenantSchemaManager.schemaExists(TEST_SCHEMA);

            // THEN
            assertTrue(result);
        }

        @Test
        @DisplayName("Debe retornar false cuando schema no existe")
        void shouldReturnFalseWhenSchemaDoesNotExist() {
            // GIVEN
            when(jdbcTemplate.queryForObject(
                    any(String.class),
                    eq(Boolean.class),
                    eq(TEST_SCHEMA)
            )).thenReturn(false);

            // WHEN
            boolean result = tenantSchemaManager.schemaExists(TEST_SCHEMA);

            // THEN
            assertFalse(result);
        }

        @Test
        @DisplayName("Debe verificar existencia por tenant ID")
        void shouldCheckExistenceByTenantId() {
            // GIVEN
            String tenantId = "test_schema";

            when(jdbcTemplate.queryForObject(
                    any(String.class),
                    eq(Boolean.class),
                    any()
            )).thenReturn(true);

            // WHEN
            boolean result = tenantSchemaManager.schemaExistsForTenant(tenantId);

            // THEN
            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("Schema Name Building Tests")
    class SchemaNameBuildingTests {

        @Test
        @DisplayName("Debe construir nombre de schema correcto")
        void shouldBuildCorrectSchemaName() {
            // GIVEN
            String tenantId = "acme_corp";

            // WHEN
            String result = tenantSchemaManager.buildSchemaName(tenantId);

            // THEN
            assertEquals("tenant_acme_corp", result);
        }

        @Test
        @DisplayName("Debe convertir tenant ID a minúsculas")
        void shouldConvertTenantIdToLowercase() {
            // GIVEN
            String tenantId = "ACME_CORP";

            // WHEN
            String result = tenantSchemaManager.buildSchemaName(tenantId);

            // THEN
            assertEquals("tenant_acme_corp", result);
        }

        @Test
        @DisplayName("Debe reemplazar espacios con guiones bajos")
        void shouldReplaceSpacesWithUnderscores() {
            // GIVEN
            String tenantId = "ACME Corp";

            // WHEN
            String result = tenantSchemaManager.buildSchemaName(tenantId);

            // THEN
            assertEquals("tenant_acme_corp", result);
        }

        @Test
        @DisplayName("Debe reemplazar guiones con guiones bajos")
        void shouldReplaceDashesWithUnderscores() {
            // GIVEN
            String tenantId = "tenant-123";

            // WHEN
            String result = tenantSchemaManager.buildSchemaName(tenantId);

            // THEN
            assertEquals("tenant_tenant_123", result);
        }
    }

    @Nested
    @DisplayName("Schema Name Normalization Tests")
    class SchemaNameNormalizationTests {

        @Test
        @DisplayName("Debe normalizar schema name a minúsculas")
        void shouldNormalizeSchemaNameToLowercase() {
            // GIVEN
            String schemaName = "TENANT_TEST";

            // WHEN
            String result = tenantSchemaManager.normalizeSchemaName(schemaName);

            // THEN
            assertEquals("tenant_test", result);
        }

        @Test
        @DisplayName("Debe agregar prefijo si no existe")
        void shouldAddPrefixIfNotExists() {
            // GIVEN
            String schemaName = "test_schema";

            // WHEN
            String result = tenantSchemaManager.normalizeSchemaName(schemaName);

            // THEN
            assertEquals("tenant_test_schema", result);
        }

        @Test
        @DisplayName("Debe mantener prefijo si ya existe")
        void shouldKeepPrefixIfAlreadyExists() {
            // GIVEN
            String schemaName = "tenant_test_schema";

            // WHEN
            String result = tenantSchemaManager.normalizeSchemaName(schemaName);

            // THEN
            assertEquals("tenant_test_schema", result);
        }

        @Test
        @DisplayName("Debe lanzar excepción para schema null")
        void shouldThrowExceptionForNullSchema() {
            // WHEN/THEN
            assertThrows(IllegalArgumentException.class, () -> {
                tenantSchemaManager.normalizeSchemaName(null);
            });
        }

        @Test
        @DisplayName("Debe lanzar excepción para schema vacío")
        void shouldThrowExceptionForEmptySchema() {
            // WHEN/THEN
            assertThrows(IllegalArgumentException.class, () -> {
                tenantSchemaManager.normalizeSchemaName("");
            });
        }

        @Test
        @DisplayName("Debe lanzar excepción para schema en blanco")
        void shouldThrowExceptionForBlankSchema() {
            // WHEN/THEN
            assertThrows(IllegalArgumentException.class, () -> {
                tenantSchemaManager.normalizeSchemaName("   ");
            });
        }
    }
}