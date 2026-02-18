package com.juanbenevento.wms.warehouse.infrastructure.adapter.out.persistence;

import com.juanbenevento.wms.inventory.application.mapper.InventoryMapper;
import com.juanbenevento.wms.warehouse.application.port.out.LocationRepositoryPort;
import com.juanbenevento.wms.inventory.domain.model.InventoryItem;
import com.juanbenevento.wms.warehouse.domain.model.Location;
import com.juanbenevento.wms.warehouse.domain.model.ZoneType;
import com.juanbenevento.wms.inventory.infrastructure.out.persistence.SpringDataInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LocationPersistenceAdapter implements LocationRepositoryPort {

    private final SpringDataLocationRepository locationRepository;
    private final SpringDataInventoryRepository inventoryRepository;
    private final LocationPersistenceMapper mapper;
    private final InventoryMapper mapperInventory;

    @Override
    public Location save(Location location) {
        LocationEntity entity = mapper.toLocationEntity(location);
        LocationEntity saved = locationRepository.save(entity);

        return hydrateLocation(saved);
    }

    @Override
    public Optional<Location> findByCode(String code) {
        return locationRepository.findByLocationCode(code)
                .map(this::hydrateLocation);
    }

    @Override
    public List<Location> findAll() {
        return locationRepository.findAll().stream()
                .map(this::hydrateLocation)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String locationCode) {
        locationRepository.deleteById(locationCode);
    }

    @Override
    public boolean hasInventory(String locationCode) {
        return !inventoryRepository.findByLocationCode(locationCode).isEmpty();
    }

    @Override
    public List<Location> findAvailableLocations(ZoneType zone, Double weightNeeded, Double volumeNeeded) {
        return locationRepository.findCandidates(zone, weightNeeded, volumeNeeded)
                .stream()
                .map(this::hydrateLocation)
                .collect(Collectors.toList());
    }

    @Override
    public List<Location> findByCodeStartingWith(String prefix) {
        return locationRepository.findByCodeStartingWith(prefix).stream()
                .map(mapper::toLocationDomain)
                .toList();
    }

    @Override
    public List<Location> findChildrenOfRack(String rackCode) {
        return locationRepository.findChildrenOfRack(rackCode).stream()
                .map(entity -> mapper.toLocationDomain(entity, Collections.emptyList()))
                .toList();
    }

    private Location hydrateLocation(LocationEntity entity) {
        List<InventoryItem> items = inventoryRepository.findByLocationCode(entity.getLocationCode())
                .stream()
                .map(mapperInventory::toItemDomain)
                .toList();

        return mapper.toLocationDomain(entity, items);
    }
}