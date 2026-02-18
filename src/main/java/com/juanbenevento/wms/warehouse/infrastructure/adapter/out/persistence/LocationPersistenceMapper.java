package com.juanbenevento.wms.warehouse.infrastructure.adapter.out.persistence;

import com.juanbenevento.wms.inventory.domain.model.InventoryItem;
import com.juanbenevento.wms.warehouse.domain.model.Location;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class LocationPersistenceMapper {
    public LocationEntity toLocationEntity(Location domain) {
        if (domain == null) return null;

        return LocationEntity.builder()
                .locationCode(domain.getLocationCode())
                .aisle(domain.getAisle())
                .column_name(domain.getColumn())
                .level_name(domain.getLevel())
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

        String aisle = entity.getAisle();
        String col = entity.getColumn_name();
        String lvl = entity.getLevel_name();

        if (aisle == null && entity.getLocationCode().contains("-")) {
            String[] parts = entity.getLocationCode().split("-");
            if (parts.length >= 3) {
                aisle = parts[0];
                col = parts[1];
                lvl = parts[2];
            }
        }

        return new Location(
                entity.getLocationCode(),
                aisle,
                col,
                lvl,
                entity.getZoneType(),
                entity.getMaxWeight(),
                entity.getMaxVolume(),
                items,
                entity.getVersion()
        );
    }

    public Location toLocationDomain(LocationEntity entity) {
        return toLocationDomain(entity, Collections.emptyList());
    }
}
