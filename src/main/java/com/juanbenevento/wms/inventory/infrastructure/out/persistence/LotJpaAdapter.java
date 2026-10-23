package com.juanbenevento.wms.inventory.infrastructure.out.persistence;

import com.juanbenevento.wms.inventory.application.port.out.LotRepository;
import com.juanbenevento.wms.inventory.domain.model.Lot;
import com.juanbenevento.wms.inventory.domain.model.LotStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for {@link LotRepository} — backed by PostgreSQL through
 * {@link LotJpaRepository} (feature real-persistence / B1). Replaces the
 * former in-memory HashMap implementation.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class LotJpaAdapter implements LotRepository {

    private final LotJpaRepository repository;

    @Override
    public Lot save(Lot lot) {
        log.debug("Persisting lot: {}", lot.getLotNumber());
        repository.save(LotEntity.from(lot));
        return lot;
    }

    @Override
    public Optional<Lot> findByLotNumber(String lotNumber) {
        return repository.findById(lotNumber).map(LotEntity::toDomain);
    }

    @Override
    public List<Lot> findByProductSku(String productSku) {
        return repository.findByProductSku(productSku).stream()
                .map(LotEntity::toDomain)
                .toList();
    }

    @Override
    public List<Lot> findByStatus(LotStatus status) {
        return repository.findByStatus(status).stream()
                .map(LotEntity::toDomain)
                .toList();
    }

    @Override
    public List<Lot> findExpiringWithin(int days) {
        LocalDate threshold = LocalDate.now().plusDays(days);
        return repository
                .findExpiringWithin(threshold, LotStatus.ACTIVE)
                .stream()
                .map(LotEntity::toDomain)
                .toList();
    }

    @Override
    public List<Lot> findActiveLotsForAllocation(String productSku) {
        return repository.findByProductSkuAndStatus(productSku, LotStatus.ACTIVE).stream()
                .map(LotEntity::toDomain)
                .toList();
    }

    @Override
    public long countByStatus(LotStatus status) {
        return repository.countByStatus(status);
    }
}
