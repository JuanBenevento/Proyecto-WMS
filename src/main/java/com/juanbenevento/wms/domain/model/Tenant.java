package com.juanbenevento.wms.domain.model;

import com.juanbenevento.wms.domain.exception.DomainException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Tenant {
    private final String id;
    private String name;
    private TenantStatus status;
    private String contactEmail;
    private final LocalDateTime createdAt;

    public Tenant(String id, String name, TenantStatus status, String contactEmail, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.contactEmail = contactEmail;
        this.createdAt = createdAt;
        validateState();
    }

    // Factory Method: Creación limpia
    public static Tenant create(String id, String name, String email) {
        return new Tenant(
                id.toUpperCase(), // Regla de negocio: IDs en mayúsculas
                name,
                TenantStatus.ACTIVE, // Por defecto nacen activos
                email,
                LocalDateTime.now()
        );
    }

    // Comportamiento
    public void suspend() {
        this.status = TenantStatus.SUSPENDED;
    }

    public void activate() {
        this.status = TenantStatus.ACTIVE;
    }

    private void validateState() {
        if (id == null || id.isBlank()) throw new DomainException("El ID de la empresa es obligatorio");
        if (name == null || name.isBlank()) throw new DomainException("El nombre de la empresa es obligatorio");
    }
}