package com.juanbenevento.wms.inventory.application.mapper;

import com.juanbenevento.wms.inventory.application.port.in.dto.InventoryItemResponse;
import com.juanbenevento.wms.inventory.domain.model.InventoryItem;
import com.juanbenevento.wms.inventory.infrastructure.out.persistence.InventoryItemEntity;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    // Domain -> DTO (Response)
    public InventoryItemResponse toItemResponse(InventoryItem item) {
        if (item == null) return null;
        return new InventoryItemResponse(
                item.getLpn().getValue(),  // Lpn -> String
                item.getProductSku(),
                (item.getProduct() != null) ? item.getProduct().getName() : "Desconocido",
                item.getQuantity(),
                (item.getStatus() != null) ? item.getStatus().name() : "N/A",
                item.getBatchNumber().getValue(),  // BatchNumber -> String
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

    // Entity -> Domain (reconstrucción sin Product)
    public InventoryItem toItemDomain(InventoryItemEntity entity) {
        if (entity == null) return null;
        return InventoryItem.fromRepository(
                entity.getLpn(),
                entity.getProductSku(),
                entity.getQuantity(),
                entity.getBatchNumber(),
                entity.getExpiryDate(),
                entity.getStatus(),
                entity.getLocationCode(),
                entity.getVersion()
        );
    }
}
