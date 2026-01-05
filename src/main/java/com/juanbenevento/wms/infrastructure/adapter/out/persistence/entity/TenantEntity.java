package com.juanbenevento.wms.infrastructure.adapter.out.persistence.entity;

import com.juanbenevento.wms.domain.model.TenantStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantEntity {
    @Id
    private String id;
    private String name;
    @Enumerated(EnumType.STRING)
    private TenantStatus status;
    private String contactEmail;
    private LocalDateTime createdAt;
}