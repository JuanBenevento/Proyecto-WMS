package com.juanbenevento.wms.identity.infrastructure.out.persistence;

import com.juanbenevento.wms.identity.domain.model.TenantStatus;
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