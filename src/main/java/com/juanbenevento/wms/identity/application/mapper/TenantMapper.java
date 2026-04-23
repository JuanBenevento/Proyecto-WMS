package com.juanbenevento.wms.identity.application.mapper;

import com.juanbenevento.wms.identity.application.port.in.dto.TenantResponse;
import com.juanbenevento.wms.identity.domain.model.Tenant;
import com.juanbenevento.wms.identity.infrastructure.out.persistence.TenantEntity;
import org.springframework.stereotype.Component;

@Component
public class TenantMapper {

    /**
     * Maps Tenant domain model to TenantResponse DTO.
     *
     * @param tenant the tenant domain model
     * @return the tenant response DTO with schema information
     */
    public TenantResponse toTenantResponse(Tenant tenant) {
        if (tenant == null) return null;
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getStatus().name(),
                tenant.getContactEmail(),
                tenant.getCreatedAt()
        );
    }

    /**
     * Maps Tenant domain model to TenantResponse DTO with schema status.
     *
     * @param tenant the tenant domain model
     * @param schemaName the PostgreSQL schema name for this tenant
     * @param schemaStatus the schema creation status
     * @return the tenant response DTO with schema information
     */
    public TenantResponse toTenantResponse(Tenant tenant, String schemaName, String schemaStatus) {
        if (tenant == null) return null;
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getStatus().name(),
                tenant.getContactEmail(),
                tenant.getCreatedAt(),
                schemaName,
                schemaStatus
        );
    }

    public TenantEntity toTenantEntity(Tenant domain) {
        if (domain == null) return null;
        return TenantEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .status(domain.getStatus())
                .contactEmail(domain.getContactEmail())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public Tenant toTenantDomain(TenantEntity entity) {
        if (entity == null) return null;
        return new Tenant(
                entity.getId(),
                entity.getName(),
                entity.getStatus(),
                entity.getContactEmail(),
                entity.getCreatedAt()
        );
    }
}