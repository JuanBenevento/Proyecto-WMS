package com.juanbenevento.wms.application.mapper;

import com.juanbenevento.wms.domain.model.AuditLog;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.entity.StockMovementLogEntity;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public StockMovementLogEntity toAuditLogEntity(AuditLog domain) {
        if (domain == null) return null;
        return StockMovementLogEntity.builder()
                .id(domain.id())
                .timestamp(domain.timestamp())
                .type(domain.type())
                .sku(domain.sku())
                .lpn(domain.lpn())
                .quantity(domain.quantity())
                .oldQuantity(domain.oldQuantity())
                .newQuantity(domain.newQuantity())
                .user(domain.user())
                .reason(domain.reason())
                .build();
    }

    public AuditLog toAuditLogDomain(StockMovementLogEntity entity) {
        if (entity == null) return null;
        return new AuditLog(
                entity.getId(),
                entity.getTimestamp(),
                entity.getType(),
                entity.getSku(),
                entity.getLpn(),
                entity.getQuantity(),
                entity.getOldQuantity(),
                entity.getNewQuantity(),
                entity.getUser(),
                entity.getReason()
        );
    }
}