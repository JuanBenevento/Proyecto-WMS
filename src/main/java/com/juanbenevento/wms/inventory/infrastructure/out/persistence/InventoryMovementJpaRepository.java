package com.juanbenevento.wms.inventory.infrastructure.out.persistence;

import com.juanbenevento.wms.inventory.domain.model.InventoryMovement;
import com.juanbenevento.wms.inventory.domain.model.MovementType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for {@link InventoryMovementEntity}
 * (feature real-persistence / B1).
 *
 * <p><b>Append-only by construction:</b> extends the base {@link Repository}
 * stereotype — not {@code CrudRepository}/{@code JpaRepository} — so no
 * update/delete methods exist on this port to the database. Movements are an
 * immutable audit log (same contract as the original port javadoc).</p>
 */
public interface InventoryMovementJpaRepository extends Repository<InventoryMovementEntity, UUID> {

    InventoryMovementEntity save(InventoryMovementEntity entity);

    Optional<InventoryMovementEntity> findById(UUID movementId);

    List<InventoryMovementEntity> findByLotNumberOrderByTimestampAsc(String lotNumber);

    List<InventoryMovementEntity> findByTypeOrderByTimestampAsc(MovementType type);

    /** Exclusive bounds (ts &gt; from AND ts &lt; to) — same semantics as the
     * original in-memory adapter's isAfter/isBefore filtering. */
    @Query("SELECT m FROM InventoryMovementEntity m "
            + "WHERE m.timestamp > :from AND m.timestamp < :to "
            + "ORDER BY m.timestamp ASC")
    List<InventoryMovementEntity> findBetweenExclusive(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    long countByLotNumber(String lotNumber);
}
