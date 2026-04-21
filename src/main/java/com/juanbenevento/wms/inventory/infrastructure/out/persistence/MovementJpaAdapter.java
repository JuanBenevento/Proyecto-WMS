package com.juanbenevento.wms.inventory.infrastructure.out.persistence;

import com.juanbenevento.wms.inventory.application.port.out.MovementRepository;
import com.juanbenevento.wms.inventory.domain.model.InventoryMovement;
import com.juanbenevento.wms.inventory.domain.model.MovementType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JPA adapter for MovementRepository.
 * Movements are append-only (immutable audit log).
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
@Slf4j
@Repository
public class MovementJpaAdapter implements MovementRepository {

    private final Map<UUID, InventoryMovement> storage = new HashMap<>();

    @Override
    public InventoryMovement save(InventoryMovement movement) {
        log.debug("Saving movement: {} for lot {}",
                movement.getMovementId(), movement.getLotNumber());
        storage.put(movement.getMovementId(), movement);
        return movement;
    }

    @Override
    public Optional<InventoryMovement> findById(UUID movementId) {
        return Optional.ofNullable(storage.get(movementId));
    }

    @Override
    public List<InventoryMovement> findByLotNumber(String lotNumber) {
        return storage.values().stream()
                .filter(m -> m.getLotNumber().equals(lotNumber))
                .sorted(Comparator.comparing(InventoryMovement::getTimestamp))
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryMovement> findByType(MovementType type) {
        return storage.values().stream()
                .filter(m -> m.getType() == type)
                .sorted(Comparator.comparing(InventoryMovement::getTimestamp))
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryMovement> findByTimestampBetween(LocalDateTime from, LocalDateTime to) {
        return storage.values().stream()
                .filter(m -> {
                    LocalDateTime ts = m.getTimestamp();
                    return ts.isAfter(from) && ts.isBefore(to);
                })
                .sorted(Comparator.comparing(InventoryMovement::getTimestamp))
                .collect(Collectors.toList());
    }

    @Override
    public long countByLotNumber(String lotNumber) {
        return storage.values().stream()
                .filter(m -> m.getLotNumber().equals(lotNumber))
                .count();
    }
}