package com.juanbenevento.wms.application.mapper;

import com.juanbenevento.wms.application.ports.in.dto.InventoryItemResponse;
import com.juanbenevento.wms.domain.model.InventoryItem;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.entity.InventoryItemEntity;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    // Domain -> DTO (Response)
    public InventoryItemResponse toItemResponse(InventoryItem item) {
        if (item == null) return null;
        return new InventoryItemResponse(
                item.getLpn(),
                item.getProductSku(),
                (item.getProduct() != null) ? item.getProduct().getName() : "Desconocido",
                item.getQuantity(),
                (item.getStatus() != null) ? item.getStatus().name() : "N/A",
                item.getBatchNumber(),
                item.getExpiryDate(),
                item.getLocationCode()
        );
    }

    // Domain -> Entity (DB)
    public InventoryItemEntity toItemEntity(InventoryItem domain) {
        if (domain == null) return null;
        return InventoryItemEntity.builder()
                .lpn(domain.getLpn())
                .productSku(domain.getProductSku())
                .quantity(domain.getQuantity())
                .batchNumber(domain.getBatchNumber())
                .expiryDate(domain.getExpiryDate())
                .status(domain.getStatus())
                .locationCode(domain.getLocationCode())
                .version(domain.getVersion())
                .build();
    }

    // Entity -> Domain
    public InventoryItem toItemDomain(InventoryItemEntity entity) {
        if (entity == null) return null;
        return new InventoryItem(
                entity.getLpn(),
                entity.getProductSku(),
                null,
                entity.getQuantity(),
                entity.getBatchNumber(),
                entity.getExpiryDate(),
                entity.getStatus(),
                entity.getLocationCode(),
                entity.getVersion()
        );
    }
}