package com.juanbenevento.wms.warehouse.application.mapper;

import com.juanbenevento.wms.warehouse.application.port.in.dto.WarehouseLayoutResponse;
import com.juanbenevento.wms.warehouse.domain.model.WarehouseLayout;
import org.springframework.stereotype.Component;

@Component
public class LayoutDtoMapper {
    public WarehouseLayoutResponse toResponse(WarehouseLayout domain){
        if (domain == null) return null;
        return new WarehouseLayoutResponse(
                domain.getId(),
                domain.getTenantId(),
                domain.getContent().rawJson(),
                domain.getVersion(),
                domain.getUpdatedAt()
        );
    }
}
