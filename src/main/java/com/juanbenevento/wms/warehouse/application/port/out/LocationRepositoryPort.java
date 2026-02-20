package com.juanbenevento.wms.warehouse.application.port.out;

import com.juanbenevento.wms.warehouse.domain.model.Location;
import com.juanbenevento.wms.warehouse.domain.model.ZoneType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface LocationRepositoryPort {
    Location save(Location location);
    Optional<Location> findByCode(String code);
    List<Location> findAll();
    void delete(String locationCode);
    boolean hasInventory(String locationCode);
    List<Location> findAvailableLocations(ZoneType zone, BigDecimal weightNeeded, BigDecimal volumeNeeded);
    List<Location> findByCodeStartingWith(String prefix); // Para el Search
    List<Location> findChildrenOfRack(String rackCode);
}