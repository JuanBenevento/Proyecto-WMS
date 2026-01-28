package com.juanbenevento.wms.infrastructure.adapter.out.persistence.mapper;

import com.juanbenevento.wms.domain.model.LayoutContent;
import com.juanbenevento.wms.domain.model.WarehouseLayout;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.entity.WarehouseLayoutEntity;
import org.springframework.stereotype.Component;

@Component
public class WarehouseLayoutMapper {
    public WarehouseLayout toDomain(WarehouseLayoutEntity entity){
        if (entity == null) return null;
        return new WarehouseLayout(
                entity.getId(),
                entity.getTenantId(),
                new LayoutContent(entity.getLayoutJson()),
                entity.getVersion(),
                entity.getUpdateAt()
        );
    }

    public WarehouseLayoutEntity toEntity(WarehouseLayout domain) {
        if (domain == null) return null;
        return WarehouseLayoutEntity.builder()
                .id(domain.getId())
                .tenantId(domain.getTenantId())
                .layoutJson(domain.getContent().rawJson())
                .version(domain.getVersion())
                .updateAt(domain.getUpdatedAt())
                .build();

    }
}
