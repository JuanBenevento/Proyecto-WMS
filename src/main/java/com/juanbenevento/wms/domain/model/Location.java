package com.juanbenevento.wms.domain.model;

import com.juanbenevento.wms.domain.exception.LocationCapacityExceededException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class Location {

    private final String locationCode;
    private final ZoneType zoneType;
    private final Double maxWeight;
    private final Double maxVolume;
    private final List<InventoryItem> items;
    private Double currentWeight;
    private Double currentVolume;
    private final Long version;

    public Location(String locationCode, ZoneType zoneType, Double maxWeight, Double maxVolume,
                    List<InventoryItem> items, Long version) {
        this.locationCode = locationCode;
        this.zoneType = zoneType;
        this.maxWeight = maxWeight;
        this.maxVolume = maxVolume;
        this.items = (items != null) ? new ArrayList<>(items) : new ArrayList<>();
        this.version = version;
        recalculateTotals();
    }

    // --- FACTORY METHOD (DDD) ---
    // Permite crear ubicaciones nuevas de forma semántica sin pasar nulls manualmente
    public static Location createEmpty(String locationCode, ZoneType zoneType, Double maxWeight, Double maxVolume) {
        return new Location(locationCode, zoneType, maxWeight, maxVolume, new ArrayList<>(), null);
    }

    // --- LÓGICA DE NEGOCIO ---

    public void consolidateLoad(InventoryItem newItem) {
        Double incomingWeight = newItem.calculateTotalWeight();
        Double incomingVolume = newItem.calculateTotalVolume();

        if (!hasSpaceFor(incomingWeight, incomingVolume)) {
            throw new LocationCapacityExceededException(
                    this.locationCode,
                    incomingWeight,
                    incomingVolume,
                    this.maxWeight,
                    this.maxVolume
            );
        }

        Optional<InventoryItem> existing = items.stream()
                .filter(i -> i.getProductSku().equals(newItem.getProductSku()) &&
                        i.getBatchNumber().equals(newItem.getBatchNumber()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().addQuantity(newItem.getQuantity());
        } else {
            items.add(newItem);
        }

        recalculateTotals();
    }

    public void releaseLoad(InventoryItem item) {
        Double weightToRelease = item.calculateTotalWeight();
        Double volumeToRelease = item.calculateTotalVolume();

        this.items.removeIf(i -> i.getLpn().equals(item.getLpn()));

        this.currentWeight = Math.max(0.0, this.currentWeight - weightToRelease);
        this.currentVolume = Math.max(0.0, this.currentVolume - volumeToRelease);

        recalculateTotals();
    }

    private boolean hasSpaceFor(Double extraWeight, Double extraVolume) {
        return (this.currentWeight + extraWeight <= this.maxWeight) &&
                (this.currentVolume + extraVolume <= this.maxVolume);
    }

    private void recalculateTotals() {
        this.currentWeight = items.stream().mapToDouble(InventoryItem::calculateTotalWeight).sum();
        this.currentVolume = items.stream().mapToDouble(InventoryItem::calculateTotalVolume).sum();
    }
}