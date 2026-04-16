package com.juanbenevento.wms.shared.infrastructure.adapter.out.persistence;

import com.juanbenevento.wms.shared.domain.valueobject.WmsConstants;
import com.juanbenevento.wms.shared.infrastructure.tenant.TenantContext;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TenantEntityListener {

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

            if (WmsConstants.SYSTEM_TENANT.equals(currentContextTenant)) {
                if (auditableEntity.getTenantId() == null) {
                    auditableEntity.setTenantId(WmsConstants.SYSTEM_TENANT);
                }
            } else {
                auditableEntity.setTenantId(currentContextTenant);
            }
        }
    }
}