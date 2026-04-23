package com.juanbenevento.wms.identity.application.port.in.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for tenant information.
 *
 * @param id           the tenant identifier
 * @param name         the tenant's company name
 * @param status       the tenant status (ACTIVE, SUSPENDED)
 * @param contactEmail the contact email for the tenant
 * @param createdAt    when the tenant was created
 * @param schemaName   the PostgreSQL schema name for this tenant (tenant_{id})
 * @param schemaStatus the status of the tenant's schema (CREATED, PENDING, ERROR)
 */
public record TenantResponse(
        String id,
        String name,
        String status,
        String contactEmail,
        LocalDateTime createdAt,
        String schemaName,
        String schemaStatus
) {
    /**
     * Convenience constructor for backward compatibility without schema info.
     */
    public TenantResponse(String id, String name, String status, String contactEmail, LocalDateTime createdAt) {
        this(id, name, status, contactEmail, createdAt, null, null);
    }
}