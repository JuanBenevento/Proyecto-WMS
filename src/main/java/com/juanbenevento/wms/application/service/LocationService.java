package com.juanbenevento.wms.application.service;

import com.juanbenevento.wms.application.mapper.LocationMapper;
import com.juanbenevento.wms.application.ports.in.command.CreateLocationCommand;
import com.juanbenevento.wms.application.ports.in.dto.LocationResponse;
import com.juanbenevento.wms.application.ports.in.usecases.ManageLocationUseCase;
import com.juanbenevento.wms.application.ports.out.LocationRepositoryPort;
import com.juanbenevento.wms.domain.exception.DomainException;
import com.juanbenevento.wms.domain.exception.LocationNotFoundException;
import com.juanbenevento.wms.domain.model.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService implements ManageLocationUseCase {

    private final LocationRepositoryPort locationRepository;
    private final LocationMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> getAllLocations() {
        return locationRepository.findAll().stream()
                .map(mapper::toLocationResponse)
                .toList();
    }

    @Override
    @Transactional
    public LocationResponse createLocation(CreateLocationCommand command) {
        if (locationRepository.findByCode(command.locationCode()).isPresent()) {
            throw new DomainException("La ubicación ya existe: " + command.locationCode());
        }

        Location newLocation = Location.createEmpty(
                command.locationCode(),
                command.zoneType(),
                command.maxWeight(),
                command.maxVolume()
        );

        Location saved = locationRepository.save(newLocation);
        return mapper.toLocationResponse(saved);
    }

    @Override
    @Transactional
    public LocationResponse updateLocation(String code, CreateLocationCommand command) {
        Location location = locationRepository.findByCode(code)
                .orElseThrow(() -> new LocationNotFoundException(code));

        if (command.maxWeight() < location.getCurrentWeight()) {
            throw new DomainException("No puedes reducir la capacidad máxima por debajo del peso actual ocupado.");
        }

        Location updated = new Location(
                code,
                command.zoneType(),
                command.maxWeight(),
                command.maxVolume(),
                location.getItems(),
                location.getVersion()
        );

        return mapper.toLocationResponse(locationRepository.save(updated));
    }

    @Override
    @Transactional
    public void deleteLocation(String code) {
        if (locationRepository.findByCode(code).isEmpty()) {
            throw new LocationNotFoundException(code);
        }

        if (locationRepository.hasInventory(code)) {
            throw new DomainException("No se puede eliminar la ubicación " + code + " porque tiene stock asociado.");
        }
        locationRepository.delete(code);
    }
}