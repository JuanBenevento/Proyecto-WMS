package com.juanbenevento.wms.shared.infrastructure.tenant;

import com.juanbenevento.wms.shared.domain.valueobject.WmsConstants;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantFilterAspect {

    private final EntityManager entityManager;

    @Before("execution(* com.juanbenevento.wms.application.service..*.*(..))")
    public void enableTenantFilter() {
        String currentTenant = TenantContext.getTenantId();

        if (currentTenant != null && !currentTenant.equals(WmsConstants.SYSTEM_TENANT)) {

            Session session = entityManager.unwrap(Session.class);

            session.enableFilter("tenantFilter")
                    .setParameter("tenantId", currentTenant);

            log.trace("🛡️ Filtro de Seguridad Activado para Tenant: {}", currentTenant);
        }
    }
}