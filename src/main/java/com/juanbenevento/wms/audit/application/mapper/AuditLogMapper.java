package com.juanbenevento.wms.audit.application.mapper;

import com.juanbenevento.wms.audit.domain.AuditLog;
import com.juanbenevento.wms.audit.infrastructure.out.persistence.StockMovementLogEntity;
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