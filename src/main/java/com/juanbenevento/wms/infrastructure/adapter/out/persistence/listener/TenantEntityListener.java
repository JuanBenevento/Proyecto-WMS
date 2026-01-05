package com.juanbenevento.wms.infrastructure.adapter.out.persistence.listener;

import com.juanbenevento.wms.infrastructure.adapter.out.persistence.entity.AuditableEntity;
import com.juanbenevento.wms.infrastructure.config.tenant.TenantContext;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TenantEntityListener {

    private static final String SUPER_ADMIN_TENANT = "SYSTEM";

    @PrePersist
    @PreUpdate
    @PreRemove
    public void setTenant(Object entity) {
        if (entity instanceof AuditableEntity auditableEntity) {

            String currentContextTenant = TenantContext.getTenantId();

            if (currentContextTenant == null || currentContextTenant.isBlank()) {
                log.error("⛔ SEGURIDAD: Intento de persistencia sin contexto de seguridad.");
                throw new IllegalStateException("Operación no permitida: Falta contexto de Tenant.");
            }

            if (SUPER_ADMIN_TENANT.equals(currentContextTenant)) {
                if (auditableEntity.getTenantId() == null) {
                    auditableEntity.setTenantId(SUPER_ADMIN_TENANT);
                }
            } else {
                auditableEntity.setTenantId(currentContextTenant);
            }
        }
    }
}