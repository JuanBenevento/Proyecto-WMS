package com.juanbenevento.wms.inventory.application.port.out;

import com.juanbenevento.wms.audit.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface StockMovementLogRepositoryPort {
    void save(AuditLog log);
    Page<AuditLog> searchLogs(
            String sku, 
            String lpn, 
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            Pageable pageable
    );
}

