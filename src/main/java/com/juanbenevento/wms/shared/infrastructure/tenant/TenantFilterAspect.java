package com.juanbenevento.wms.shared.infrastructure.tenant;

import com.juanbenevento.wms.shared.domain.valueobject.WmsConstants;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

/**
 * Aspecto para filtro de búsqueda de tenant basado en Hibernate.
 *
 * @deprecated Este filtro de Hibernate ya NO es el mecanismo primario de aislamiento.
 *             El aislamiento ahora se maneja a nivel de base de datos mediante
 *             {@link SearchPathConnectionInterceptor} que establece search_path por sesión.
 *             Este aspecto se mantiene solo para compatibilidad hacia atrás.
 *
 *             Si ve este warning en logs, indica que hay código que aún llama este aspecto.
 *             La arquitectura actual no requiere este filtro para funcionar.
 */
@Deprecated
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantFilterAspect {

    private final EntityManager entityManager;

    /**
     * @deprecated Este método ya no es necesario para el aislamiento de tenants.
     *             Mantenido solo para compatibilidad con código existente.
     */
    @Deprecated
    @Before("execution(* com.juanbenevento.wms.application.service..*.*(..))")
    public void enableTenantFilter() {
        String currentTenant = TenantContext.getTenantId();

        if (currentTenant != null && !currentTenant.equals(WmsConstants.SYSTEM_TENANT)) {

            Session session = entityManager.unwrap(Session.class);

            session.enableFilter("tenantFilter")
                    .setParameter("tenantId", currentTenant);

            log.warn("🛡️ [DEPRECATED] Filtro de Hibernate Activado para Tenant: {}. " +
                    "Este código es obsoleto - el aislamiento ahora es por schema (search_path).",
                    currentTenant);
        }
    }
}