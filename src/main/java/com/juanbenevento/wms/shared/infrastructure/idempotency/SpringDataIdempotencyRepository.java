package com.juanbenevento.wms.shared.infrastructure.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataIdempotencyRepository extends JpaRepository<IdempotencyRecordEntity, String> {
}
