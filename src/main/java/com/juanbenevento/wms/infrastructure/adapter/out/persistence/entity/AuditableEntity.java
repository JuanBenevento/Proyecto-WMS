package com.juanbenevento.wms.infrastructure.adapter.out.persistence.entity;

import com.juanbenevento.wms.infrastructure.adapter.out.persistence.listener.TenantEntityListener;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
// FASE 1: Listeners para Auditoría y Seguridad Multitenant
@EntityListeners({AuditingEntityListener.class, TenantEntityListener.class})
// FASE 1: Definición del Filtro de Hibernate para Seguridad en Lectura
@FilterDef(
        name = "tenantFilter",
        parameters = @ParamDef(name = "tenantId", type = String.class)
)
@Filter(
        name = "tenantFilter",
        condition = "tenant_id = :tenantId"
)
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

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @Version
    @Column(nullable = false)
    private Long version;
}