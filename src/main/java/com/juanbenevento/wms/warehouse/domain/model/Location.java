package com.juanbenevento.wms.warehouse.domain.model;

import com.juanbenevento.wms.inventory.domain.model.InventoryItem;
import com.juanbenevento.wms.shared.domain.exception.LocationCapacityExceededException;
import lombok.Getter;

import java.math.BigDecimal;
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
    private final BigDecimal maxWeight;
    private final BigDecimal maxVolume;
    private final List<InventoryItem> items;
    private BigDecimal currentWeight;
    private BigDecimal currentVolume;
    private final Long version;

    public Location(String locationCode,
                    String aisle, String column, String level,
                    ZoneType zoneType, BigDecimal maxWeight, BigDecimal maxVolume,
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
                                              ZoneType zone, BigDecimal maxW, BigDecimal maxV) {
        return new Location(code, aisle, col, level, zone, maxW, maxV, new ArrayList<>(), null);
    }

    public static Location createOperationalArea(String code, ZoneType zone, BigDecimal maxW, BigDecimal maxV) {
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
        BigDecimal incomingWeight = newItem.calculateTotalWeight();
        BigDecimal incomingVolume = newItem.calculateTotalVolume();

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

    private boolean hasSpaceFor(BigDecimal extraWeight, BigDecimal extraVolume) {
        BigDecimal projectedWeight = this.currentWeight.add(extraWeight);
        BigDecimal projectedVolume = this.currentVolume.add(extraVolume);

        return projectedWeight.compareTo(this.maxWeight) <= 0 &&
                projectedVolume.compareTo(this.maxVolume) <= 0;
    }

    private void recalculateTotals() {
        this.currentWeight = items.stream()
                .map(InventoryItem::calculateTotalWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.currentVolume = items.stream()
                .map(InventoryItem::calculateTotalVolume)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}