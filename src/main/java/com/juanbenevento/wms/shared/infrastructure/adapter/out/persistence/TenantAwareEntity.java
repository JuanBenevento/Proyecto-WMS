package com.juanbenevento.wms.shared.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base class for entities in schema-isolated tenant environments.
 *
 * <p>This class provides audit fields for entities that operate within
 * tenant-specific PostgreSQL schemas. Unlike {@link AuditableEntity},
 * this class does NOT include a tenant_id column because tenant isolation
 * is handled at the database schema level via search_path.
 *
 * <p>Usage:
 * <pre>
 * {@code
 * @Entity
 * @Table(name = "inventory_items")
 * public class InventoryItem extends TenantAwareEntity {
 *     // ... fields and methods
 * }
 * }
 * </pre>
 *
 * <p>New entities should extend this class instead of AuditableEntity
 * when they are guaranteed to operate within a tenant schema context.
 *
 * @see AuditableEntity
 * @see com.juanbenevento.wms.shared.infrastructure.tenant.TenantContext
 * @see com.juanbenevento.wms.shared.infrastructure.tenant.TenantSchemaManager
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class, TenantEntityListener.class})
public abstract class TenantAwareEntity {

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedBy
    private String lastModifiedBy;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @Version
    @Column(nullable = false)
    private Long version;

    // NOTE: No tenantId column here. Isolation is handled via PostgreSQL schema (search_path).
    // Entities requiring a tenant_id column for cross-tenant lookups should use AuditableEntity instead.
}