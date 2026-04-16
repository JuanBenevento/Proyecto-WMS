package com.juanbenevento.wms.warehouse.application.mapper;

import com.juanbenevento.wms.inventory.application.mapper.InventoryMapper;
import com.juanbenevento.wms.inventory.application.port.in.dto.InventoryItemResponse;
import com.juanbenevento.wms.warehouse.application.port.in.dto.LocationResponse;
import com.juanbenevento.wms.warehouse.domain.model.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LocationDtoMapper {

    private final InventoryMapper inventoryMapper;

    public LocationResponse toLocationResponse(Location location) {
        if (location == null) return null;

        BigDecimal occupancyPercent = BigDecimal.ZERO;
        if (location.getMaxWeight() != null && location.getMaxWeight().compareTo(BigDecimal.ZERO) > 0) {
            occupancyPercent = location.getCurrentWeight()
                    .divide(location.getMaxWeight(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100.00"));
        }

        BigDecimal availableWeight = location.getMaxWeight().subtract(location.getCurrentWeight());

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
                availableWeight,
                location.getMaxVolume(),
                location.getCurrentVolume(),
                occupancyPercent.setScale(2, RoundingMode.HALF_UP),
                itemResponses
        );
    }
}