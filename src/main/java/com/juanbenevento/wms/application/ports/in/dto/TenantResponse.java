package com.juanbenevento.wms.application.ports.in.dto;

import java.time.LocalDateTime;

public record TenantResponse(
        String id,
        String name,
        String status,
        String contactEmail,
        LocalDateTime createdAt
) {}