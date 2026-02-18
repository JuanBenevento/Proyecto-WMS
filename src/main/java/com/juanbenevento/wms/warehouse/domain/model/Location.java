package com.juanbenevento.wms.warehouse.domain.model;

import com.juanbenevento.wms.inventory.domain.model.InventoryItem;
import com.juanbenevento.wms.shared.domain.exception.LocationCapacityExceededException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class Location {
    private final String locationCode;
    private final String aisle;
    private final String column;
    private final String level;
    private final ZoneType zoneType;
    private final Double maxWeight;
    private final Double maxVolume;
    private final List<InventoryItem> items;
    private Double currentWeight;
    private Double currentVolume;
    private final Long version;

    public Location(String locationCode,
                    String aisle, String column, String level,
                    ZoneType zoneType, Double maxWeight, Double maxVolume,
                    List<InventoryItem> items, Long version) {
        this.locationCode = locationCode;
        this.aisle = aisle;
        this.column = column;
        this.level = level;
        this.zoneType = zoneType;
        this.maxWeight = maxWeight;
        this.maxVolume = maxVolume;
        this.items = (items != null) ? new ArrayList<>(items) : new ArrayList<>();
        this.version = version;

        recalculateTotals();
    }

    // --- FACTORY METHODS (DDD) ---
    public static Location createRackPosition(String code, String aisle, String col, String level,
                                              ZoneType zone, Double maxW, Double maxV) {
        return new Location(code, aisle, col, level, zone, maxW, maxV, new ArrayList<>(), null);
    }

    public static Location createOperationalArea(String code, ZoneType zone, Double maxW, Double maxV) {
        return new Location(code, null, null, "FLOOR", zone, maxW, maxV, new ArrayList<>(), null);
    }

    // --- LÓGICA DE NEGOCIO Y COMPORTAMIENTO ---

    public boolean isOperational() {
        return zoneType == ZoneType.RECEIVING_AREA ||
                zoneType == ZoneType.DISPATCH_AREA ||
                zoneType == ZoneType.DOCK_DOOR ||
                zoneType == ZoneType.PICKING_AREA ||
                zoneType == ZoneType.YARD;
    }

    public boolean isStorage() {
        return !isOperational();
    }

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
        boolean removed = this.items.removeIf(i -> i.getLpn().equals(item.getLpn()));

        if (removed) {
            recalculateTotals();
        }
    }

    // --- MÉTODOS PRIVADOS DE SOPORTE ---

    private boolean hasSpaceFor(Double extraWeight, Double extraVolume) {
        return (this.currentWeight + extraWeight <= this.maxWeight) &&
                (this.currentVolume + extraVolume <= this.maxVolume);
    }

    private void recalculateTotals() {
        this.currentWeight = items.stream().mapToDouble(InventoryItem::calculateTotalWeight).sum();
        this.currentVolume = items.stream().mapToDouble(InventoryItem::calculateTotalVolume).sum();
    }
}