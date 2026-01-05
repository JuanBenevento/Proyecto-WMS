package com.juanbenevento.wms.application.mapper;

import com.juanbenevento.wms.application.ports.in.dto.TenantResponse;
import com.juanbenevento.wms.domain.model.Tenant;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.entity.TenantEntity;
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