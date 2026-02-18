package com.juanbenevento.wms.warehouse.infrastructure.adapter.out.persistence;

import com.juanbenevento.wms.warehouse.domain.model.LayoutContent;
import com.juanbenevento.wms.warehouse.domain.model.WarehouseLayout;
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
