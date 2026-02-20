package com.juanbenevento.wms.warehouse.domain.model;

import com.juanbenevento.wms.catalog.domain.model.Product;
import com.juanbenevento.wms.inventory.domain.model.InventoryItem;
import com.juanbenevento.wms.inventory.domain.model.InventoryStatus;
import com.juanbenevento.wms.shared.domain.exception.LocationCapacityExceededException;
import com.juanbenevento.wms.shared.domain.valueobject.Dimensions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LocationTest {
    private Product heavyProduct;
    private Product lightProduct;

    @BeforeEach
    void setUp() {
        Dimensions heavyDims = new Dimensions(
                new BigDecimal("2.0"), new BigDecimal("2.0"), new BigDecimal("2.0"), new BigDecimal("20.0"));
        heavyProduct = new Product(UUID.randomUUID(), "SKU-HEAVY", "Heavy Item", "Desc", heavyDims, 1L);

        Dimensions lightDims = new Dimensions(
                new BigDecimal("2.0"), new BigDecimal("2.0"), new BigDecimal("2.0"), new BigDecimal("5.0"));
        lightProduct = new Product(UUID.randomUUID(), "SKU-LIGHT", "Light Item", "Desc", lightDims, 1L);
    }

    @Test
    @DisplayName("Debe crear correctamente una posición de Rack (Storage)")
    void shouldCreateRackPositionCorrectly() {
        Location loc = Location.createRackPosition(
                "A-05-01", "A", "05", "01",
                ZoneType.DRY_STORAGE, new BigDecimal("1000.0"), new BigDecimal("200.0")
        );

        assertNotNull(loc);
        assertTrue(loc.isStorage());
        assertFalse(loc.isOperational());
        assertEquals("A", loc.getAisle());
        assertEquals("01", loc.getLevel());
    }

    @Test
    @DisplayName("Debe crear correctamente una zona operativa (Floor)")
    void shouldCreateOperationalAreaCorrectly() {
        Location loc = Location.createOperationalArea(
                "REC-01", ZoneType.RECEIVING_AREA, new BigDecimal("5000.0"), new BigDecimal("10000.0")
        );

        assertTrue(loc.isOperational());
        assertFalse(loc.isStorage());
        assertEquals("FLOOR", loc.getLevel());
        assertNull(loc.getAisle());
    }

    @Test
    @DisplayName("Debe aceptar carga cuando hay capacidad suficiente")
    void shouldAcceptLoad_WhenCapacityIsEnough() {
        Location loc = Location.createRackPosition(
                "A-01-01", "A", "01", "01",
                ZoneType.DRY_STORAGE, new BigDecimal("100.0"), new BigDecimal("100.0")
        );

        // WHEN
        InventoryItem item = createItem(heavyProduct, new BigDecimal("2.0"), "A-01-01");
        loc.consolidateLoad(item);

        // THEN
        assertEquals(0, loc.getCurrentWeight().compareTo(new BigDecimal("40.0")));
        assertEquals(0, loc.getCurrentVolume().compareTo(new BigDecimal("16.0"))); // 2un * 8m3 = 16m3
        assertEquals(1, loc.getItems().size());
    }

    @Test
    @DisplayName("Debe rechazar carga (Excepción) cuando excede capacidad")
    void shouldRejectLoad_WhenCapacityExceeded() {
        Location loc = Location.createRackPosition(
                "A-01-01", "A", "01", "01",
                ZoneType.DRY_STORAGE, new BigDecimal("30.0"), new BigDecimal("100.0")
        );

        InventoryItem item = createItem(heavyProduct, new BigDecimal("2.0"), "A-01-01");

        // THEN
        assertThrows(LocationCapacityExceededException.class, () -> loc.consolidateLoad(item));
        assertEquals(0, loc.getCurrentWeight().compareTo(BigDecimal.ZERO));
        assertTrue(loc.getItems().isEmpty());
    }

    @Test
    @DisplayName("Debe sumar cantidad (Merge) si el item (SKU + Batch) ya existe")
    void shouldMergeItemsIfSameSkuAndBatch() {
        Location loc = Location.createRackPosition(
                "A-01-01", "A", "01", "01",
                ZoneType.DRY_STORAGE, new BigDecimal("100.0"), new BigDecimal("100.0")
        );

        // GIVEN:
        loc.consolidateLoad(createItem(lightProduct, new BigDecimal("2.0"), "A-01-01"));

        // WHEN:
        loc.consolidateLoad(createItem(lightProduct, new BigDecimal("3.0"), "A-01-01"));

        // THEN:
        assertEquals(1, loc.getItems().size());
        assertEquals(0, loc.getItems().get(0).getQuantity().compareTo(new BigDecimal("5.0")));
        assertEquals(0, loc.getCurrentWeight().compareTo(new BigDecimal("25.0"))); // 5 * 5kg
    }

    private InventoryItem createItem(Product product, BigDecimal qty, String locCode) {
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