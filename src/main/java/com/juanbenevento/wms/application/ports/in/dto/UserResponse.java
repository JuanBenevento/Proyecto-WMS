package com.juanbenevento.wms.application.ports.in.dto;

public record UserResponse(
        Long id,
        String username,
        String role,
        String tenantId
) {}