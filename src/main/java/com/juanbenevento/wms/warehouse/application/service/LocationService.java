package com.juanbenevento.wms.warehouse.application.service;

import com.juanbenevento.wms.warehouse.application.mapper.LocationDtoMapper;
import com.juanbenevento.wms.warehouse.application.port.in.command.CreateLocationCommand;
import com.juanbenevento.wms.warehouse.application.port.in.dto.LocationResponse;
import com.juanbenevento.wms.warehouse.application.port.in.usecases.ManageLocationUseCase;
import com.juanbenevento.wms.warehouse.application.port.out.LocationRepositoryPort;
import com.juanbenevento.wms.shared.domain.exception.DomainException;
import com.juanbenevento.wms.warehouse.domain.exception.LocationNotFoundException;
import com.juanbenevento.wms.warehouse.domain.model.Location;
import com.juanbenevento.wms.warehouse.domain.model.ZoneType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService implements ManageLocationUseCase {

    private final LocationRepositoryPort locationRepository;
    private final LocationDtoMapper mapper;

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

        Location newLocation;

        if (isOperationalZone(command.zoneType())) {
            newLocation = Location.createOperationalArea(
                    command.locationCode(),
                    command.zoneType(),
                    command.maxWeight(),
                    command.maxVolume()
            );
        } else {
            LocationStructure struct = parseLocationCode(command.locationCode());

            newLocation = Location.createRackPosition(
                    command.locationCode(),
                    struct.aisle,
                    struct.column,
                    struct.level,
                    command.zoneType(),
                    command.maxWeight(),
                    command.maxVolume()
            );
        }

        Location saved = locationRepository.save(newLocation);
        return mapper.toLocationResponse(saved);
    }

    @Override
    @Transactional
    public LocationResponse updateLocation(String code, CreateLocationCommand command) {
        Location existingLocation = locationRepository.findByCode(code)
                .orElseThrow(() -> new LocationNotFoundException(code));

        if (command.maxWeight().compareTo(existingLocation.getCurrentWeight()) < 0) {
            throw new DomainException(String.format(
                    "No es posible reducir la capacidad máxima (%s kg) por debajo del peso actual ocupado (%s kg).",
                    command.maxWeight().toPlainString(),
                    existingLocation.getCurrentWeight().toPlainString()
            ));
        }

        if (command.maxVolume().compareTo(existingLocation.getCurrentVolume()) < 0) {
            throw new DomainException("No es posible reducir el volumen máximo por debajo del volumen actual ocupado.");
        }

        Location updated = new Location(
                code,
                existingLocation.getAisle(),
                existingLocation.getColumn(),
                existingLocation.getLevel(),
                command.zoneType(),
                command.maxWeight(), // BigDecimal
                command.maxVolume(), // BigDecimal
                existingLocation.getItems(),
                existingLocation.getVersion()
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

    @Override
    @Transactional(readOnly = true)
    public LocationResponse getLocationByCode(String code) {
        Location location = locationRepository.findByCode(code)
                .orElseThrow(() -> new LocationNotFoundException(code));

        return mapper.toLocationResponse(location);
    }

    private boolean isOperationalZone(ZoneType type) {
        return type == ZoneType.RECEIVING_AREA ||
                type == ZoneType.DISPATCH_AREA ||
                type == ZoneType.DOCK_DOOR ||
                type == ZoneType.PICKING_AREA ||
                type == ZoneType.YARD;
    }

    private record LocationStructure(String aisle, String column, String level) {}

    private LocationStructure parseLocationCode(String code) {
        if (code == null) return new LocationStructure(null, null, "01");

        String[] parts = code.split("-");

        String aisle = parts.length > 0 ? parts[0] : "GEN";
        String column = parts.length > 1 ? parts[1] : "01";
        String level = parts.length > 2 ? parts[2] : "01";

        return new LocationStructure(aisle, column, level);
    }
}