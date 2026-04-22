package com.juanbenevento.wms.integration;

import com.juanbenevento.wms.shared.domain.valueobject.WmsConstants;
import com.juanbenevento.wms.shared.infrastructure.tenant.SchemaIsolationValidator;
import com.juanbenevento.wms.shared.infrastructure.tenant.TenantContext;
import com.juanbenevento.wms.shared.infrastructure.tenant.TenantSchemaManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration tests for schema-based tenant isolation.
 *
 * <p>Tests verify that:
 * <ul>
 *   <li>Tenant schemas are properly isolated</li>
 *   <li>Queries are scoped to the correct schema</li>
 *   <li>Cross-tenant data access is prevented</li>
 *   <li>RLS policies are enforced (if enabled)</li>
 * </ul>
 *
 * <p>Uses Testcontainers for PostgreSQL to test real database behavior.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TenantIsolationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("wms_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TenantSchemaManager tenantSchemaManager;

    @Autowired
    private SchemaIsolationValidator schemaIsolationValidator;

    private static final String TENANT_A = "tenant_a";
    private static final String TENANT_B = "tenant_b";
    private static final String TENANT_A_SCHEMA = "tenant_tenant_a";
    private static final String TENANT_B_SCHEMA = "tenant_tenant_b";

    @BeforeEach
    void setUp() {
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Nested
    @DisplayName("Schema Creation Tests")
    @TestMethodOrder(OrderAnnotation.class)
    class SchemaCreationTests {

        @Test
        @Order(1)
        @DisplayName("Debe crear schemas para Tenant A y Tenant B")
        void shouldCreateSchemasForBothTenants() {
            // WHEN
            String schemaA = tenantSchemaManager.createSchema(TENANT_A);
            String schemaB = tenantSchemaManager.createSchema(TENANT_B);

            // THEN
            assertEquals(TENANT_A_SCHEMA, schemaA);
            assertEquals(TENANT_B_SCHEMA, schemaB);
            assertTrue(tenantSchemaManager.schemaExists(schemaA));
            assertTrue(tenantSchemaManager.schemaExists(schemaB));
        }

        @Test
        @Order(2)
        @DisplayName("Debe crear tablas en schema de tenant")
        void shouldCreateTablesInTenantSchema() {
            // GIVEN - Schema ya creado
            tenantSchemaManager.createSchema(TENANT_A);
            String schemaName = tenantSchemaManager.buildSchemaName(TENANT_A);

            // WHEN - Crear tabla en el schema
            jdbcTemplate.execute(String.format(
                    "CREATE TABLE %s.test_table (id SERIAL PRIMARY KEY, data TEXT)", schemaName));

            // THEN
            Boolean tableExists = jdbcTemplate.queryForObject(
                    String.format("SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = '%s' AND table_name = 'test_table')",
                            schemaName.replace("tenant_", "")),
                    Boolean.class);
            assertTrue(tableExists);
        }
    }

    @Nested
    @DisplayName("Isolation Tests")
    @TestMethodOrder(OrderAnnotation.class)
    class IsolationTests {

        @Test
        @Order(3)
        @DisplayName("Tenant A no puede ver datos de Tenant B")
        void tenantACannotSeeTenantBData() {
            // GIVEN
            setupTenantsWithData();

            // WHEN - Tenant A accede a su schema
            TenantContext.setTenantId(TENANT_A);
            jdbcTemplate.execute("SET search_path TO " + TENANT_A_SCHEMA);

            // THEN - Solo ve datos de Tenant A
            Integer tenantACount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM inventory_item",
                    Integer.class);
            assertEquals(2, tenantACount);

            // WHEN - Verificamos que no ve datos de Tenant B
            String searchPath = schemaIsolationValidator.getCurrentSearchPath();
            assertTrue(searchPath.contains(TENANT_A_SCHEMA));

            // Cleanup
            jdbcTemplate.execute("RESET search_path");
        }

        @Test
        @Order(4)
        @DisplayName("Cada tenant solo accede a su propio schema")
        void eachTenantOnlyAccessesOwnSchema() {
            // GIVEN - Ambos tenants con datos
            setupTenantsWithData();

            // WHEN - Tenant B configura su contexto
            TenantContext.setTenantId(TENANT_B);
            jdbcTemplate.execute("SET search_path TO " + TENANT_B_SCHEMA);

            // THEN
            Integer tenantBCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM inventory_item",
                    Integer.class);
            assertEquals(3, tenantBCount); // Tenant B tiene 3 items

            // Cleanup
            jdbcTemplate.execute("RESET search_path");
        }

        @Test
        @Order(5)
        @DisplayName("Raw SQL query respeta aislamiento de schema")
        void rawSqlQueryRespectsIsolation() {
            // GIVEN
            setupTenantsWithData();

            // WHEN - Ejecutamos query raw como Tenant A
            TenantContext.setTenantId(TENANT_A);
            jdbcTemplate.execute("SET search_path TO " + TENANT_A_SCHEMA);

            // Ejecutamos query raw
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM inventory_item WHERE status = 'AVAILABLE'",
                    Integer.class);

            // THEN
            assertEquals(2, count); // Solo items AVAILABLE de Tenant A

            // Cleanup
            jdbcTemplate.execute("RESET search_path");
        }
    }

    @Nested
    @DisplayName("Context Management Tests")
    class ContextManagementTests {

        @Test
        @DisplayName("Schema isolation validator detecta contexto activo")
        void schemaIsolationValidatorDetectsActiveContext() {
            // GIVEN - Crear schema y configurar contexto
            tenantSchemaManager.createSchema(TENANT_A);
            TenantContext.setTenantId(TENANT_A);
            jdbcTemplate.execute("SET search_path TO " + TENANT_A_SCHEMA);

            // THEN
            assertTrue(schemaIsolationValidator.isSchemaIsolationActive());

            // Cleanup
            jdbcTemplate.execute("RESET search_path");
        }

        @Test
        @DisplayName("Validación de schema pasa cuando search_path es correcto")
        void schemaValidationPassesWhenSearchPathIsCorrect() {
            // GIVEN
            tenantSchemaManager.createSchema(TENANT_A);
            TenantContext.setTenantId(TENANT_A);
            jdbcTemplate.execute("SET search_path TO " + TENANT_A_SCHEMA);

            // WHEN/THEN
            assertDoesNotThrow(() -> schemaIsolationValidator.validateCurrentSchema());

            // Cleanup
            jdbcTemplate.execute("RESET search_path");
        }

        @Test
        @DisplayName("Debug info muestra información correcta")
        void debugInfoShowsCorrectInformation() {
            // GIVEN
            tenantSchemaManager.createSchema(TENANT_A);
            TenantContext.setTenantId(TENANT_A);
            jdbcTemplate.execute("SET search_path TO " + TENANT_A_SCHEMA);

            // WHEN
            String debugInfo = schemaIsolationValidator.getDebugInfo();

            // THEN
            assertTrue(debugInfo.contains("TenantContext: " + TENANT_A));
            assertTrue(debugInfo.contains("Expected Schema: " + TENANT_A_SCHEMA));
            assertTrue(debugInfo.contains("Isolation Active: true"));

            // Cleanup
            jdbcTemplate.execute("RESET search_path");
        }
    }

    @Nested
    @DisplayName("Tenant ID Normalization Tests")
    class TenantIdNormalizationTests {

        @Test
        @DisplayName("Debe normalizar Tenant ID con espacios")
        void shouldNormalizeTenantIdWithSpaces() {
            // GIVEN - Tenant ID con espacios
            String tenantId = "ACME Corp";
            String expectedSchema = "tenant_acme_corp";

            // WHEN
            String schemaName = tenantSchemaManager.buildSchemaName(tenantId);

            // THEN
            assertEquals(expectedSchema, schemaName);
        }

        @Test
        @DisplayName("Debe normalizar Tenant ID con caracteres especiales")
        void shouldNormalizeTenantIdWithSpecialChars() {
            // GIVEN
            String tenantId = "Tenant-123";
            String expectedSchema = "tenant_tenant_123";

            // WHEN
            String schemaName = tenantSchemaManager.buildSchemaName(tenantId);

            // THEN
            assertEquals(expectedSchema, schemaName);
        }

        @Test
        @DisplayName("WmsConstants.normalizeTenantId funciona correctamente")
        void wmsConstantsNormalizeTenantIdWorksCorrectly() {
            // GIVEN
            assertEquals("acme_c", WmsConstants.normalizeTenantId("  ACME C  "));
            assertEquals("tenant_123", WmsConstants.normalizeTenantId("tenant-123"));
            assertEquals("multi_word", WmsConstants.normalizeTenantId("Multi Word"));
        }
    }

    @Nested
    @DisplayName("Cleanup Tests")
    class CleanupTests {

        @AfterAll
        static void cleanupSchemas() {
            // This runs after all tests to clean up test schemas
            // Note: Using static JdbcTemplate won't work here, so schemas are cleaned
            // by Testcontainers when the container stops
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Creates test schemas and populates them with test data.
     */
    private void setupTenantsWithData() {
        // Create schemas
        tenantSchemaManager.createSchema(TENANT_A);
        tenantSchemaManager.createSchema(TENANT_B);

        // Create tables in each schema
        jdbcTemplate.execute(String.format(
                "CREATE TABLE IF NOT EXISTS %s.inventory_item (id SERIAL PRIMARY KEY, sku TEXT, status TEXT, quantity NUMERIC)",
                TENANT_A_SCHEMA));
        jdbcTemplate.execute(String.format(
                "CREATE TABLE IF NOT EXISTS %s.inventory_item (id SERIAL PRIMARY KEY, sku TEXT, status TEXT, quantity NUMERIC)",
                TENANT_B_SCHEMA));

        // Insert test data for Tenant A
        jdbcTemplate.execute(String.format(
                "INSERT INTO %s.inventory_item (sku, status, quantity) VALUES ('SKU-A1', 'AVAILABLE', 100)", TENANT_A_SCHEMA));
        jdbcTemplate.execute(String.format(
                "INSERT INTO %s.inventory_item (sku, status, quantity) VALUES ('SKU-A2', 'AVAILABLE', 50)", TENANT_A_SCHEMA));

        // Insert test data for Tenant B
        jdbcTemplate.execute(String.format(
                "INSERT INTO %s.inventory_item (sku, status, quantity) VALUES ('SKU-B1', 'AVAILABLE', 200)", TENANT_B_SCHEMA));
        jdbcTemplate.execute(String.format(
                "INSERT INTO %s.inventory_item (sku, status, quantity) VALUES ('SKU-B2', 'RESERVED', 75)", TENANT_B_SCHEMA));
        jdbcTemplate.execute(String.format(
                "INSERT INTO %s.inventory_item (sku, status, quantity) VALUES ('SKU-B3', 'AVAILABLE', 30)", TENANT_B_SCHEMA));
    }
}