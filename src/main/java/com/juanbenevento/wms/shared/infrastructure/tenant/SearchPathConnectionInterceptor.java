package com.juanbenevento.wms.shared.infrastructure.tenant;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * JDBC ConnectionInterceptor that sets PostgreSQL search_path on connection checkout.
 *
 * <p>This interceptor wraps HikariCP connections to automatically set the search_path
 * when a connection is retrieved from the pool, and reset it when the connection is returned.
 *
 * <p>Note: This approach requires Spring Boot 3.2+ with JDBC interceptors support.
 * For earlier versions, use TenantConnectionFilter (Servlet Filter approach) instead.
 *
 * <p>Flow:
 * <ol>
 *   <li>Connection checked out from HikariCP pool</li>
 *   <li>Interceptor executes SET search_path TO tenant_{tenantId}</li>
 *   <li>Application uses connection (all queries scoped to tenant schema)</li>
 *   <li>Connection returned to pool</li>
 *   <li>Interceptor executes RESET search_path</li>
 * </ol>
 *
 * @see TenantConnectionFilter
 * @see TenantContext
 */
@Component
@Slf4j
public class SearchPathConnectionInterceptor /*implements ConnectionInterceptor*/ {
    // Note: Full implementation requires Spring Boot 3.2+ ConnectionInterceptor interface
    // This is a placeholder showing the approach pattern
    
    // For Spring Boot 3.2+, implement ConnectionInterceptor:
    // - onCheckout(Connection): SET search_path
    // - onCheckin(Connection): RESET search_path
    
    /*
    Example implementation for Spring Boot 3.2+:
    
    @Override
    public void onCheckout(Connection connection, String reason, Throwable cause) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            String schemaName = TenantContext.getSchemaName(tenantId);
            try (var stmt = connection.createStatement()) {
                stmt.execute("SET search_path TO " + schemaName);
                log.debug("Set search_path to {} on connection checkout", schemaName);
            } catch (SQLException e) {
                log.error("Failed to set search_path: {}", e.getMessage());
                throw new RuntimeException("Failed to set tenant search_path", e);
            }
        }
    }
    
    @Override
    public void onCheckin(Connection connection, Throwable cause) {
        try (var stmt = connection.createStatement()) {
            stmt.execute("RESET search_path");
            log.debug("Reset search_path on connection checkin");
        } catch (SQLException e) {
            log.warn("Failed to reset search_path: {}", e.getMessage());
        }
    }
    */

    /**
     * Configures the HikariCP datasource with connection initialization SQL.
     *
     * <p>This can be configured in application.properties:
     * <pre>
     * spring.datasource.hikari.connection-init-sql=SET search_path TO public
     * </pre>
     *
     * <p>For tenant-specific schema, use TenantConnectionFilter instead
     * as it can dynamically set the search_path per request.
     *
     * @param dataSource the HikariDataSource to configure
     * @return the configured datasource
     */
    public static DataSource configureConnectionInitSql(HikariDataSource dataSource) {
        // Set a default search_path for all new connections
        // This ensures clean state when connections are created
        dataSource.setConnectionInitSql("SET search_path TO public");
        return dataSource;
    }
}