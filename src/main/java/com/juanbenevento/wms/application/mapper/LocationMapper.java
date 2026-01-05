package com.juanbenevento.wms.application.mapper;

import com.juanbenevento.wms.application.ports.in.dto.InventoryItemResponse;
import com.juanbenevento.wms.application.ports.in.dto.LocationResponse;
import com.juanbenevento.wms.domain.model.InventoryItem;
import com.juanbenevento.wms.domain.model.Location;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.entity.LocationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LocationMapper {

    private final InventoryMapper inventoryMapper;

    public LocationResponse toLocationResponse(Location location) {
        if (location == null) return null;

        double occupancy = (location.getMaxWeight() > 0)
                ? (location.getCurrentWeight() / location.getMaxWeight()) * 100.0
                : 0.0;

        List<InventoryItemResponse> itemResponses = (location.getItems() != null)
                ? location.getItems().stream().map(inventoryMapper::toItemResponse).toList()
                : Collections.emptyList();

        return new LocationResponse(
                location.getLocationCode(),
                location.getZoneType().name(),
                location.getMaxWeight(),
                location.getCurrentWeight(),
                location.getMaxWeight() - location.getCurrentWeight(),
                location.getMaxVolume(),
                location.getCurrentVolume(),
                Math.round(occupancy * 100.0) / 100.0,
                itemResponses
        );
    }

    public LocationEntity toLocationEntity(Location domain) {
        if (domain == null) return null;
        return LocationEntity.builder()
                .locationCode(domain.getLocationCode())
                .zoneType(domain.getZoneType())
                .maxWeight(domain.getMaxWeight())
                .maxVolume(domain.getMaxVolume())
                .currentWeight(domain.getCurrentWeight())
                .currentVolume(domain.getCurrentVolume())
                .version(domain.getVersion())
                .build();
    }

    public Location toLocationDomain(LocationEntity entity, List<InventoryItem> items) {
        if (entity == null) return null;
        return new Location(
                entity.getLocationCode(),
                entity.getZoneType(),
                entity.getMaxWeight(),
                entity.getMaxVolume(),
                items,
                entity.getVersion()
        );
    }
}