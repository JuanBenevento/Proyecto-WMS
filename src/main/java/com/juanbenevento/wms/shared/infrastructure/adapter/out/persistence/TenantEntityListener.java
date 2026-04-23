package com.juanbenevento.wms.shared.infrastructure.adapter.out.persistence;

import com.juanbenevento.wms.shared.domain.valueobject.WmsConstants;
import com.juanbenevento.wms.shared.infrastructure.tenant.TenantContext;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Listener para manejo de entidades en contexto multi-tenant.
 *
 * NOTA: La seteo de tenant_id ya NO es el mecanismo primario de aislamiento.
 *       El aislamiento ahora funciona mediante schemas de PostgreSQL
 *       (search_path configurado por SearchPathConnectionInterceptor).
 *
 *       Este listener mantiene la validación de contexto y setea tenant_id
 *       solo por compatibilidad hacia atrás durante la migración.
 *       En futuras versiones, esta lógica será removida.
 */
@Component
@Slf4j
public class TenantEntityListener {

    @PrePersist
    @PreUpdate
    @PreRemove
    public void setTenant(Object entity) {
        if (entity instanceof AuditableEntity auditableEntity) {

            String currentContextTenant = TenantContext.getTenantId();

            // Validación: contexto de tenant DEBE estar presente
            if (currentContextTenant == null || currentContextTenant.isBlank()) {
                log.error("⛔ SEGURIDAD: Intento de persistencia sin contexto de seguridad.");
                throw new IllegalStateException("Operación no permitida: Falta contexto de Tenant.");
            }

            // Seteo de tenant_id solo por compatibilidad hacia atrás durante migración.
            // El aislamiento real es por schema (search_path).
            if (WmsConstants.SYSTEM_TENANT.equals(currentContextTenant)) {
                if (auditableEntity.getTenantId() == null) {
                    auditableEntity.setTenantId(WmsConstants.SYSTEM_TENANT);
                    log.debug("🔧 [MIGRATION] tenantId configurado para SYSTEM_TENANT (legacy column)");
                }
            } else {
                if (auditableEntity.getTenantId() == null) {
                    auditableEntity.setTenantId(currentContextTenant);
                    log.debug("🔧 [MIGRATION] tenantId configurado para tenant: {} (legacy column)",
                            currentContextTenant);
                }
            }

            // Log de info para debugging durante transición
            log.trace("🔍 Validación de contexto completada para tenant: {} [schema-based isolation]",
                    currentContextTenant);
        }
    }
}