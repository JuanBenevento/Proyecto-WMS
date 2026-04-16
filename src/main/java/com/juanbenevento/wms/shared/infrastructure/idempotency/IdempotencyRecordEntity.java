package com.juanbenevento.wms.shared.infrastructure.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdempotencyRecordEntity {

    @Id
    @Column(name = "idempotency_key", nullable = false, updatable = false, length = 36)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public IdempotencyRecordEntity(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("La llave de idempotencia no puede ser nula");
        }
        this.idempotencyKey = idempotencyKey;
        this.createdAt = LocalDateTime.now();
    }
}
