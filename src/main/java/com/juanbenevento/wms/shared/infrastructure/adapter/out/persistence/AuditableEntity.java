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
 * Entidad base para todas las entidades del WMS que requieren auditoría.
 *
 * NOTA: La columna tenant_id se mantiene SOLO por compatibilidad hacia atrás
 * durante el período de migración. El aislamiento de tenants ahora es manejado
 * a nivel de base de datos mediante schemas (search_path), no por esta columna.
 *
 * En el futuro, esta columna será removida para tablas que usan aislamiento por schema.
 * Se mantiene para tablas compartidas entre tenants (lookups, catálogos).
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class, TenantEntityListener.class})
public abstract class AuditableEntity {

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

    /**
     * Columna mantenida solo para compatibilidad hacia atrás durante migración.
     * No es usada para aislamiento - el aislamiento es por schema (search_path).
     *
     * @deprecated El aislamiento de tenants ahora es por schema.
     *             Esta columna será removida en futuras versiones.
     */
    @Deprecated
    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @Version
    @Column(nullable = false)
    private Long version;
}