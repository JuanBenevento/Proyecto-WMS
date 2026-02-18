package com.juanbenevento.wms.identity.application.mapper;

import com.juanbenevento.wms.identity.application.port.in.dto.TenantResponse;
import com.juanbenevento.wms.identity.domain.model.Tenant;
import com.juanbenevento.wms.identity.infrastructure.out.persistence.TenantEntity;
import org.springframework.stereotype.Component;

@Component
public class TenantMapper {

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