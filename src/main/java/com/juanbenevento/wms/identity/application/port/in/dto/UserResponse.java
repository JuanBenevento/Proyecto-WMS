package com.juanbenevento.wms.identity.application.port.in.dto;

public record UserResponse(
        Long id,
        String username,
        String role,
        String tenantId
) {}