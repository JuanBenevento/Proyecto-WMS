package com.juanbenevento.wms.shared.infrastructure.tenant;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests de integración del {@link TenantAwareDataSource} (S1, S2).
 * Usa PostgreSQL real vía Testcontainers, sin contexto Spring, para probar
 * que el search_path se aplica SOBRE LA CONEXIÓN que sale del pool.
 */
@Testcontainers
class TenantAwareDataSourceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("wms_tenant_test")
            .withUsername("test")
            .withPassword("test");

    static HikariDataSource hikari;

    TenantAwareDataSource dataSource;

    @BeforeAll
    static void initPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        config.setMaximumPoolSize(4);
        hikari = new HikariDataSource(config);
    }

    @AfterAll
    static void closePool() {
        if (hikari != null) {
            hikari.close();
        }
    }

    @BeforeEach
    void setUp() {
        TenantContext.clear();
        dataSource = new TenantAwareDataSource(hikari, true);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        try (Connection conn = hikari.getConnection(); Statement st = conn.createStatement()) {
            st.execute("DROP SCHEMA IF EXISTS tenant_alpha CASCADE");
            st.execute("DROP SCHEMA IF EXISTS tenant_beta CASCADE");
            st.execute("SET search_path TO public");
        } catch (SQLException e) {
            // cleanup best-effort
        }
    }

    private String currentSearchPath(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SHOW search_path")) {
            rs.next();
            return rs.getString(1);
        }
    }

    @Test
    @DisplayName("S1: sin tenant en contexto, la conexión resuelve contra public")
    void noTenantContext_usesPublicSchema() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            assertTrue(currentSearchPath(conn).contains("public"),
                    "Sin tenant, search_path debe incluir public, fue: " + currentSearchPath(conn));
        }
    }

    @Test
    @DisplayName("S1: tenant SYSTEM usa public (operaciones de sistema)")
    void systemTenant_usesPublicSchema() throws SQLException {
        TenantContext.setTenantId("SYSTEM");
        try (Connection conn = dataSource.getConnection()) {
            assertTrue(currentSearchPath(conn).contains("public"));
            assertFalse(currentSearchPath(conn).contains("tenant_"));
        }
    }

    @Test
    @DisplayName("S1: con tenant y schema existente, search_path apunta AL SCHEMA DEL TENANT sin fallback a public")
    void tenantWithExistingSchema_setsSearchPathToTenantSchema() throws SQLException {
        createSchemaDirectly("tenant_alpha");

        TenantContext.setTenantId("alpha");
        try (Connection conn = dataSource.getConnection()) {
            String path = currentSearchPath(conn);
            assertTrue(path.contains("tenant_alpha"),
                    "search_path debe contener tenant_alpha, fue: " + path);
            assertFalse(path.contains("public"),
                    "search_path NO debe incluir fallback a public, fue: " + path);
        }
    }

    @Test
    @DisplayName("S2: schema del tenant inexistente → fail-closed, sin fallback silencioso a public")
    void tenantWithMissingSchema_failsClosed() {
        TenantContext.setTenantId("ghost");

        TenantSchemaMissingException ex = assertThrows(TenantSchemaMissingException.class,
                () -> dataSource.getConnection(),
                "Un tenant sin schema debe fallar en voz alta, no caer a public");
        assertTrue(ex.getMessage().toLowerCase().contains("schema"),
                "El error debe mencionar el schema faltante, fue: " + ex.getMessage());
    }

    @Test
    @DisplayName("S2: id de tenant con caracteres ilegales para identificador SQL → rechazado (whitelist)")
    void tenantIdWithIllegalIdentifierChars_rejected() {
        TenantContext.setTenantId("evil;drop table users");

        assertThrows(Exception.class, () -> dataSource.getConnection(),
                "Un tenantId que produzca un identificador SQL inválido debe ser rechazado");
    }

    @Test
    @DisplayName("S1: dos tenants secuenciales cada uno ve su propio schema (sin contaminación)")
    void sequentialConnections_eachSeesOwnSchema() throws SQLException {
        createSchemaWithData("tenant_alpha", "alpha", 111);
        createSchemaWithData("tenant_beta", "beta", 222);

        TenantContext.setTenantId("alpha");
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id FROM marker")) {
            assertTrue(rs.next());
            assertEquals(111, rs.getInt(1), "alpha debe ver su propia fila");
        }

        TenantContext.setTenantId("beta");
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id FROM marker")) {
            assertTrue(rs.next());
            assertEquals(222, rs.getInt(1), "beta debe ver su propia fila, no la de alpha");
        }
    }

    @Test
    @DisplayName("S3: isolationEnabled=false → passthrough explícito, sin fail-closed")
    void isolationDisabled_passesThroughWithoutFailClosed() throws SQLException {
        TenantAwareDataSource passthrough = new TenantAwareDataSource(hikari, false);

        TenantContext.setTenantId("ghost");
        try (Connection conn = passthrough.getConnection()) {
            assertTrue(currentSearchPath(conn).contains("public"),
                    "Con flag off el comportamiento es passthrough sobre public");
        }
    }

    @Test
    @DisplayName("S1: tenant SYSTEM después de un tenant no hereda su search_path")
    void systemAfterTenant_doesNotInheritSearchPath() throws SQLException {
        createSchemaDirectly("tenant_alpha");

        TenantContext.setTenantId("alpha");
        try (Connection ignored = dataSource.getConnection()) {
            // checkout con tenant alpha
        }

        TenantContext.setTenantId("SYSTEM");
        try (Connection conn = dataSource.getConnection()) {
            assertFalse(currentSearchPath(conn).contains("tenant_alpha"),
                    "La conexión del sistema no debe heredar el schema del tenant anterior");
        }
    }

    private void createSchemaDirectly(String schemaName) throws SQLException {
        try (Connection conn = hikari.getConnection(); Statement st = conn.createStatement()) {
            st.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
        }
    }

    private void createSchemaWithData(String schemaName, String tenantId, int markerId) throws SQLException {
        try (Connection conn = hikari.getConnection(); Statement st = conn.createStatement()) {
            st.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
            st.execute("SET search_path TO " + schemaName);
            st.execute("CREATE TABLE marker (id INT PRIMARY KEY)");
            st.execute("INSERT INTO marker VALUES (" + markerId + ")");
            st.execute("SET search_path TO public");
        }
    }
}
