package com.juanbenevento.wms.inventory.infrastructure.out.persistence;

import com.juanbenevento.wms.inventory.domain.model.LotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for {@link LotEntity}
 * (feature real-persistence / B1).
 */
public interface LotJpaRepository extends JpaRepository<LotEntity, String> {

    List<LotEntity> findByProductSku(String productSku);

    List<LotEntity> findByStatus(LotStatus status);

    /**
     * Explicit JPQL instead of derived query name: Spring Data's parser cannot
     * resolve {@code findExpiryDateLessThanEqualAndStatusOrderByExpiryDateAsc}
     * ("No property 'asc' found for type 'LocalDate'").
     */
    @Query("SELECT l FROM LotEntity l "
            + "WHERE l.expiryDate <= :threshold AND l.status = :status "
            + "ORDER BY l.expiryDate ASC")
    List<LotEntity> findExpiringWithin(@Param("threshold") LocalDate threshold,
            @Param("status") LotStatus status);

    List<LotEntity> findByProductSkuAndStatus(String productSku, LotStatus status);

    long countByStatus(LotStatus status);
}
