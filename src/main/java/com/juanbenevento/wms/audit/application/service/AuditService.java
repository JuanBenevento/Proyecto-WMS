package com.juanbenevento.wms.audit.application.service;

import com.juanbenevento.wms.audit.application.port.in.RetrieveAuditLogsUseCase;
import com.juanbenevento.wms.inventory.application.port.out.StockMovementLogRepositoryPort;
import com.juanbenevento.wms.audit.domain.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService implements RetrieveAuditLogsUseCase {

    private final StockMovementLogRepositoryPort repositoryPort;

    @Override
    public Page<AuditLog> getAuditLogs(
            String sku, 
            String lpn, 
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            Pageable pageable
    ) {
        return repositoryPort.searchLogs(sku, lpn, startDate, endDate, pageable);
    }
}

