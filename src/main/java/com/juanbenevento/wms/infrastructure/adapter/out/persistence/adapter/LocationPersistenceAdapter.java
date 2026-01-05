package com.juanbenevento.wms.infrastructure.adapter.out.persistence.adapter;

import com.juanbenevento.wms.application.mapper.InventoryMapper;
import com.juanbenevento.wms.application.mapper.LocationMapper;
import com.juanbenevento.wms.application.ports.out.LocationRepositoryPort;
import com.juanbenevento.wms.domain.model.InventoryItem;
import com.juanbenevento.wms.domain.model.Location;
import com.juanbenevento.wms.domain.model.ZoneType;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.entity.LocationEntity;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.repository.SpringDataInventoryRepository;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.repository.SpringDataLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LocationPersistenceAdapter implements LocationRepositoryPort {

    private final SpringDataLocationRepository locationRepository;
    private final SpringDataInventoryRepository inventoryRepository;
    private final LocationMapper mapper;
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

    private Location hydrateLocation(LocationEntity entity) {
        List<InventoryItem> items = inventoryRepository.findByLocationCode(entity.getLocationCode())
                .stream()
                .map(mapperInventory::toItemDomain)
                .toList();

        return mapper.toLocationDomain(entity, items);
    }
}