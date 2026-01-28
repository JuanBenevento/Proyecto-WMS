package com.juanbenevento.wms.application.mapper;

import com.juanbenevento.wms.application.ports.in.dto.WarehouseLayoutResponse;
import com.juanbenevento.wms.domain.model.WarehouseLayout;
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
