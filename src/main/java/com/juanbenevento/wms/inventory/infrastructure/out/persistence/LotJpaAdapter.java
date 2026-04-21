package com.juanbenevento.wms.inventory.infrastructure.out.persistence;

import com.juanbenevento.wms.inventory.application.port.out.LotRepository;
import com.juanbenevento.wms.inventory.domain.model.Lot;
import com.juanbenevento.wms.inventory.domain.model.LotStatus;
import com.juanbenevento.wms.inventory.domain.model.TemperatureRange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * JPA adapter for LotRepository.
 * Uses in-memory storage for now (replace with JPA entity when schema is finalized).
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
@Slf4j
@Repository
public class LotJpaAdapter implements LotRepository {

    private final Map<String, Lot> storage = new HashMap<>();

    @Override
    public Lot save(Lot lot) {
        log.debug("Saving lot: {}", lot.getLotNumber());
        storage.put(lot.getLotNumber(), lot);
        return lot;
    }

    @Override
    public Optional<Lot> findByLotNumber(String lotNumber) {
        return Optional.ofNullable(storage.get(lotNumber));
    }

    @Override
    public List<Lot> findByProductSku(String productSku) {
        return storage.values().stream()
                .filter(lot -> lot.getProductSku().equals(productSku))
                .toList();
    }

    @Override
    public List<Lot> findByStatus(LotStatus status) {
        return storage.values().stream()
                .filter(lot -> lot.getStatus() == status)
                .toList();
    }

    @Override
    public List<Lot> findExpiringWithin(int days) {
        long threshold = LocalDate.now().plusDays(days).toEpochDay();
        return storage.values().stream()
                .filter(lot -> lot.getExpiryDate() != null)
                .filter(lot -> lot.getExpiryDate().toEpochDay() <= threshold)
                .filter(lot -> lot.getStatus() == LotStatus.ACTIVE)
                .sorted(Comparator.comparing(lot -> lot.getExpiryDate()))
                .toList();
    }

    @Override
    public List<Lot> findActiveLotsForAllocation(String productSku) {
        return storage.values().stream()
                .filter(lot -> lot.getProductSku().equals(productSku))
                .filter(lot -> lot.getStatus() == LotStatus.ACTIVE)
                .toList();
    }

    @Override
    public long countByStatus(LotStatus status) {
        return storage.values().stream()
                .filter(lot -> lot.getStatus() == status)
                .count();
    }
}