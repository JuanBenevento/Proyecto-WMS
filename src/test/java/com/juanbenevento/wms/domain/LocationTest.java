package com.juanbenevento.wms.domain;

import com.juanbenevento.wms.domain.exception.LocationCapacityExceededException;
import com.juanbenevento.wms.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LocationTest {

    private Product heavyProduct;
    private Product lightProduct;

    @BeforeEach
    void setUp() {
        Dimensions heavyDims = new Dimensions(2.0, 2.0, 2.0, 20.0);
        heavyProduct = new Product(UUID.randomUUID(), "SKU-HEAVY", "Heavy Item", "Desc", heavyDims, 1L);

        Dimensions lightDims = new Dimensions(2.0, 2.0, 2.0, 5.0);
        lightProduct = new Product(UUID.randomUUID(), "SKU-LIGHT", "Light Item", "Desc", lightDims, 1L);
    }

    @Test
    @DisplayName("Debe aceptar carga cuando hay capacidad suficiente")
    void shouldAcceptLoad_WhenCapacityIsEnough() {
        // GIVEN:
        Location loc = Location.createEmpty("A-01", ZoneType.DRY_STORAGE, 100.0, 100.0);

        // WHEN
        InventoryItem item = createItem(heavyProduct, 2.0, "A-01");
        loc.consolidateLoad(item);

        // THEN
        assertEquals(40.0, loc.getCurrentWeight());
        assertEquals(16.0, loc.getCurrentVolume()); // 2 * 8m3
        assertEquals(1, loc.getItems().size()); // Debe tener 1 item en la lista
    }

    @Test
    @DisplayName("Debe rechazar carga (Excepción) cuando excede capacidad")
    void shouldRejectLoad_WhenCapacityExceeded() {
        // GIVEN
        Location loc = Location.createEmpty("A-01", ZoneType.DRY_STORAGE, 30.0, 100.0);

        // WHEN
        InventoryItem item = createItem(heavyProduct, 2.0, "A-01");

        // THEN
        assertThrows(LocationCapacityExceededException.class, () -> {
            loc.consolidateLoad(item);
        });

        // AND
        assertEquals(0.0, loc.getCurrentWeight());
        assertTrue(loc.getItems().isEmpty());
    }

    @Test
    @DisplayName("Debe acumular peso correctamente al agregar múltiples items")
    void shouldAccumulateWeightCorrectly() {
        Location loc = Location.createEmpty("A-01", ZoneType.DRY_STORAGE, 100.0, 100.0);

        loc.consolidateLoad(createItem(heavyProduct, 2.0, "A-01"));

        loc.consolidateLoad(createItem(lightProduct, 2.0, "A-01"));

        assertEquals(50.0, loc.getCurrentWeight());
        assertEquals(2, loc.getItems().size());
    }

    @Test
    @DisplayName("Debe liberar espacio correctamente (Release Load)")
    void shouldReleaseLoadCorrectly() {
        Location loc = Location.createEmpty("A-01", ZoneType.DRY_STORAGE, 100.0, 100.0);

        // GIVEN
        InventoryItem item = createItem(heavyProduct, 2.0, "A-01");
        loc.consolidateLoad(item);
        assertEquals(40.0, loc.getCurrentWeight());

        // WHEN
        loc.releaseLoad(item);

        // THEN
        assertEquals(0.0, loc.getCurrentWeight());
        assertEquals(0.0, loc.getCurrentVolume());
        assertTrue(loc.getItems().isEmpty());
    }

    @Test
    @DisplayName("Debe sumar cantidad si el item (SKU + Batch) ya existe en la ubicación")
    void shouldMergeItemsIfSameSkuAndBatch() {
        Location loc = Location.createEmpty("A-01", ZoneType.DRY_STORAGE, 100.0, 100.0);

        // GIVEN
        InventoryItem item1 = createItem(lightProduct, 2.0, "A-01"); // Batch "BATCH-001"
        loc.consolidateLoad(item1);

        // WHEN
        InventoryItem item2 = createItem(lightProduct, 3.0, "A-01"); // Batch "BATCH-001"
        loc.consolidateLoad(item2);

        // THEN
        assertEquals(1, loc.getItems().size());
        assertEquals(5.0, loc.getItems().get(0).getQuantity());
        assertEquals(25.0, loc.getCurrentWeight()); // 5 * 5kg
    }

    // --- Helper para crear items rápido ---
    private InventoryItem createItem(Product product, Double qty, String locCode) {
        return new InventoryItem(
                "LPN-" + UUID.randomUUID(),
                product.getSku(),
                product,
                qty,
                "BATCH-001",
                LocalDate.now().plusDays(30),
                InventoryStatus.AVAILABLE,
                locCode,
                null
        );
    }
}