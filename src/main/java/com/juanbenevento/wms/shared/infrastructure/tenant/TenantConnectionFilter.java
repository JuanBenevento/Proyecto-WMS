package com.juanbenevento.wms.shared.infrastructure.tenant;

import com.juanbenevento.wms.shared.domain.valueobject.WmsConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that sets the PostgreSQL search_path for each HTTP request based on the current tenant.
 *
 * <p>This filter ensures that all database queries are routed to the correct tenant schema
 * by executing {@code SET search_path TO tenant_{tenantId}} before request processing,
 * and {@code RESET search_path} after the request completes.
 *
 * <p>Flow:
 * <ol>
 *   <li>Extract tenant ID from TenantContext</li>
 *   <li>Build schema name using TenantSchemaManager pattern</li>
 *   <li>Execute SET search_path to route queries to tenant schema</li>
 *   <li>Proceed with filter chain</li>
 *   <li>In finally block: reset search_path to prevent tenant leakage</li>
 * </ol>
 *
 * <p>This approach works with HikariCP connection pool by resetting the session state
 * after each request, ensuring no cross-tenant contamination.
 *
 * @see TenantContext
 * @see TenantSchemaManager
 * @see WmsConstants#TENANT_SCHEMA_PREFIX
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantConnectionFilter extends OncePerRequestFilter {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Filter order - should run after authentication but before repository layer.
     * Using low priority to ensure TenantContext is set first.
     */
    public static final int FILTER_ORDER = 100;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String tenantId = TenantContext.getTenantId();
        
        // Skip search_path setting if no tenant context (system operations)
        if (tenantId == null) {
            log.trace("No tenant context, skipping search_path filter");
            filterChain.doFilter(request, response);
            return;
        }
        
        // Skip for system tenant
        if (WmsConstants.SYSTEM_TENANT.equals(tenantId)) {
            log.trace("System tenant, skipping search_path filter");
            filterChain.doFilter(request, response);
            return;
        }
        
        String schemaName = buildSchemaName(tenantId);
        
        try {
            // Set search_path to tenant schema FIRST, then public as fallback
            // This way: queries find tables in tenant_xxx if they exist, otherwise in public
            log.debug("Setting search_path to '{}, public' for tenant '{}'", schemaName, tenantId);
            jdbcTemplate.execute("SET search_path TO " + schemaName + ", public");
            
            // Proceed with the request
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            // Schema doesn't exist or other DB error - fallback to public only
            log.warn("Could not set tenant schema '{}': {}. Using public schema only.", 
                    schemaName, e.getMessage());
            
            // Fallback: only public schema
            jdbcTemplate.execute("SET search_path TO public");
            
            // Continue with public schema - queries will work
            filterChain.doFilter(request, response);
            
        } finally {
            // Clean up search_path to prevent tenant leakage
            // Explicitly reset to public (not RESET which uses postgresql.conf default)
            log.trace("Resetting search_path to public after request");
            try {
                jdbcTemplate.execute("SET search_path TO public");
            } catch (Exception e) {
                log.trace("Error resetting search_path: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Builds the schema name for a given tenant ID.
     * Follows the pattern: tenant_{normalizedTenantId}
     *
     * @param tenantId the tenant identifier
     * @return the fully qualified schema name (e.g., "tenant_acme_corp")
     */
    private String buildSchemaName(String tenantId) {
        // Use WmsConstants to normalize the tenant ID
        // Format: tenant_ + normalized_tenant_id
        String normalized = WmsConstants.normalizeTenantId(tenantId);
        return WmsConstants.TENANT_SCHEMA_PREFIX + normalized;
    }
}