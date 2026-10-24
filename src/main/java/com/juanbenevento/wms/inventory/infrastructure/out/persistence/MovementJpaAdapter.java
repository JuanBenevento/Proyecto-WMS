package com.juanbenevento.wms.inventory.infrastructure.out.persistence;

import com.juanbenevento.wms.inventory.application.port.out.MovementRepository;
import com.juanbenevento.wms.inventory.domain.model.InventoryMovement;
import com.juanbenevento.wms.inventory.domain.model.MovementType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA adapter for {@link MovementRepository} — backed by PostgreSQL through
 * {@link InventoryMovementJpaRepository} (feature real-persistence / B1).
 * Movements remain append-only: the underlying repository exposes no
 * update/delete operations.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MovementJpaAdapter implements MovementRepository {

    private final InventoryMovementJpaRepository repository;

    @Override
    public InventoryMovement save(InventoryMovement movement) {
        log.debug("Persisting movement: {} for lot {}",
                movement.getMovementId(), movement.getLotNumber());
        repository.save(InventoryMovementEntity.from(movement));
        return movement;
    }

    @Override
    public Optional<InventoryMovement> findById(UUID movementId) {
        return repository.findById(movementId).map(InventoryMovementEntity::toDomain);
    }

    @Override
    public List<InventoryMovement> findByLotNumber(String lotNumber) {
        return repository.findByLotNumberOrderByTimestampAsc(lotNumber).stream()
                .map(InventoryMovementEntity::toDomain)
                .toList();
    }

    @Override
    public List<InventoryMovement> findByType(MovementType type) {
        return repository.findByTypeOrderByTimestampAsc(type).stream()
                .map(InventoryMovementEntity::toDomain)
                .toList();
    }

    @Override
    public List<InventoryMovement> findByTimestampBetween(LocalDateTime from, LocalDateTime to) {
        return repository.findBetweenExclusive(from, to).stream()
                .map(InventoryMovementEntity::toDomain)
                .toList();
    }

    @Override
    public long countByLotNumber(String lotNumber) {
        return repository.countByLotNumber(lotNumber);
    }
}
