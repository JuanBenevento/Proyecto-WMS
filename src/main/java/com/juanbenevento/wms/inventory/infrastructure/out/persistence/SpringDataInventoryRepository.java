package com.juanbenevento.wms.inventory.infrastructure.out.persistence;

import com.juanbenevento.wms.inventory.domain.model.InventoryStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataInventoryRepository extends JpaRepository<InventoryItemEntity, String> {
    List<InventoryItemEntity> findByProductSku(String productSku);
    List<InventoryItemEntity> findByLocationCode(String locationCode);
    List<InventoryItemEntity> findByProductSkuAndStatusOrderByExpiryDateAsc(String productSku, InventoryStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")})
    @Query("SELECT i " +
            "FROM InventoryItemEntity i " +
            "WHERE i.productSku = :sku " +
            "AND i.status = :status " +
            "ORDER BY i.expiryDate ASC")
    List<InventoryItemEntity> findAndLockByProductSku(@Param("sku") String sku, @Param("status") InventoryStatus status);
}
