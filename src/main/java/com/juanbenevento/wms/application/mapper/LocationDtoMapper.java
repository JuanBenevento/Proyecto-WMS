package com.juanbenevento.wms.application.mapper;

import com.juanbenevento.wms.application.ports.in.dto.InventoryItemResponse;
import com.juanbenevento.wms.application.ports.in.dto.LocationResponse;
import com.juanbenevento.wms.domain.model.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LocationDtoMapper {

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
                location.getAisle(),
                location.getColumn(),
                location.getLevel(),
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
}