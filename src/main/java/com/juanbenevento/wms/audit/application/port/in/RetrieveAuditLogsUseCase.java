package com.juanbenevento.wms.audit.application.port.in;

import com.juanbenevento.wms.audit.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface RetrieveAuditLogsUseCase {
    Page<AuditLog> getAuditLogs(
            String sku,
            String lpn,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );
}

