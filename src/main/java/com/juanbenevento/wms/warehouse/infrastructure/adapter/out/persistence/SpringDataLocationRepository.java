package com.juanbenevento.wms.warehouse.infrastructure.adapter.out.persistence;

import com.juanbenevento.wms.warehouse.domain.model.ZoneType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SpringDataLocationRepository extends JpaRepository<LocationEntity, String> {

    Optional<LocationEntity> findByLocationCode(String locationCode);

    @Query("SELECT l FROM LocationEntity l WHERE UPPER(l.locationCode) LIKE UPPER(CONCAT(:prefix, '%'))")
    List<LocationEntity> findByCodeStartingWith(@Param("prefix") String prefix);

    // 2. Heatmap: Busca los hijos de un Rack (ej: "A-01" -> "A-01-01")
    @Query("SELECT l FROM LocationEntity l WHERE l.locationCode LIKE CONCAT(:rackCode, '-%')")
    List<LocationEntity> findChildrenOfRack(@Param("rackCode") String rackCode);

    @Query("""
        SELECT l FROM LocationEntity l
        WHERE l.zoneType = :zone
        AND (l.maxWeight - COALESCE(l.currentWeight, 0)) >= :requiredWeight
        AND (l.maxVolume - COALESCE(l.currentVolume, 0)) >= :requiredVolume
        ORDER BY l.locationCode ASC
    """)
    List<LocationEntity> findCandidates(
            @Param("zone") ZoneType zone,
            @Param("requiredWeight") BigDecimal requiredWeight,
            @Param("requiredVolume") BigDecimal requiredVolume
    );
}