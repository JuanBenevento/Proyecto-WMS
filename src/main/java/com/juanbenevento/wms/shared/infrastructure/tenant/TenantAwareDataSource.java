package com.juanbenevento.wms.shared.infrastructure.tenant;

import com.juanbenevento.wms.shared.domain.valueobject.WmsConstants;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * DataSource que garantiza aislamiento multi-tenant a nivel de CONEXIÓN (S1, S2).
 *
 * <p>El search_path se aplica en el checkout de cada conexión del pool, sobre la
 * MISMA conexión que ejecutará las queries del request — resolviendo el bug del
 * antiguo {@code TenantConnectionFilter} que ejecutaba {@code SET} sobre una
 * conexión distinta (y ajena) a la del request.
 *
 * <p>Comportamiento:
 * <ul>
 *   <li>Sin tenant en {@link TenantContext} o con {@code SYSTEM}: search_path = public.</li>
 *   <li>Con tenant: se verifica que el schema exista ({@code information_schema});
 *       si NO existe → {@link TenantSchemaMissingException} (fail-closed, sin fallback a public).</li>
 *   <li>El nombre del schema se valida contra whitelist {@code [a-z0-9_]+} antes de
 *       construir SQL (defensa contra inyección de identificadores).</li>
 *   <li>{@code isolationEnabled=false} → passthrough (solo para entornos que lo justifiquen).</li>
 * </ul>
 */
@Slf4j
public class TenantAwareDataSource implements DataSource {

    /** Identificador PostgreSQL seguro: inicia con letra, solo minúsculas/dígitos/guion bajo. */
    private static final Pattern SAFE_SCHEMA_IDENTIFIER = Pattern.compile("^[a-z][a-z0-9_]{0,62}$");

    private final DataSource target;
    private final boolean isolationEnabled;

    public TenantAwareDataSource(DataSource target, boolean isolationEnabled) {
        this.target = target;
        this.isolationEnabled = isolationEnabled;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = target.getConnection();
        if (!isolationEnabled) {
            return connection;
        }
        try {
            applyTenantSchema(connection);
            return connection;
        } catch (SQLException | RuntimeException e) {
            closeQuietly(connection);
            throw e;
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = target.getConnection(username, password);
        if (!isolationEnabled) {
            return connection;
        }
        try {
            applyTenantSchema(connection);
            return connection;
        } catch (SQLException | RuntimeException e) {
            closeQuietly(connection);
            throw e;
        }
    }

    /**
     * Aplica el search_path del tenant actual SOBRE la conexión recibida.
     * Fail-closed: si el schema del tenant no existe, lanza en vez de degradar a public.
     */
    private void applyTenantSchema(Connection connection) throws SQLException {
        String tenantId = TenantContext.getTenantId();

        if (tenantId == null || WmsConstants.SYSTEM_TENANT.equals(tenantId)) {
            setSearchPath(connection, "public");
            return;
        }

        String schemaName = buildAndValidateSchemaName(tenantId);

        if (!schemaExists(connection, schemaName)) {
            throw new TenantSchemaMissingException(
                    "Tenant schema missing: '" + schemaName + "' for tenant '" + tenantId
                            + "'. No fallback to public is allowed.");
        }

        // Solo el schema del tenant — SIN fallback a public (S2)
        setSearchPath(connection, schemaName);
        log.debug("search_path set to '{}' for tenant '{}'", schemaName, tenantId);
    }

    /**
     * Construye el nombre del schema desde el tenant ID y valida que sea un
     * identificador SQL seguro antes de concatenarlo en el SET.
     */
    private String buildAndValidateSchemaName(String tenantId) {
        String normalized = WmsConstants.normalizeTenantId(tenantId);
        String schemaName = WmsConstants.TENANT_SCHEMA_PREFIX + normalized;
        if (!SAFE_SCHEMA_IDENTIFIER.matcher(schemaName).matches()) {
            throw new IllegalArgumentException(
                    "Illegal tenant schema identifier rejected: '" + schemaName
                            + "' (tenant '" + tenantId + "'). Allowed: [a-z][a-z0-9_]*");
        }
        return schemaName;
    }

    private void setSearchPath(Connection connection, String schemaName) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("SET search_path TO " + schemaName);
        }
    }

    /**
     * Verifica existencia del schema en la MISMA conexión (postgresa no valida
     * schemas inexistentes en search_path — por eso hace falta esta consulta).
     */
    private boolean schemaExists(Connection connection, String schemaName) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name = ?)")) {
            ps.setString(1, schemaName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

    private void closeQuietly(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            log.trace("Error closing connection after failed tenant setup: {}", e.getMessage());
        }
    }

    // --- DataSource delegation ---

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return target.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        target.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        target.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return target.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() {
        return Logger.getLogger("TenantAwareDataSource");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return iface.cast(this);
        }
        return target.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this) || target.isWrapperFor(iface);
    }
}
