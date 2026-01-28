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
        Dimensions heavyDims = new Dimensions(2.0, 2.0, 2.0, 20.0); // 20kg
        heavyProduct = new Product(UUID.randomUUID(), "SKU-HEAVY", "Heavy Item", "Desc", heavyDims, 1L);

        Dimensions lightDims = new Dimensions(2.0, 2.0, 2.0, 5.0); // 5kg
        lightProduct = new Product(UUID.randomUUID(), "SKU-LIGHT", "Light Item", "Desc", lightDims, 1L);
    }

    @Test
    @DisplayName("Debe crear correctamente una posición de Rack (Storage)")
    void shouldCreateRackPositionCorrectly() {
        Location loc = Location.createRackPosition(
                "A-05-01", "A", "05", "01",
                ZoneType.DRY_STORAGE, 1000.0, 200.0
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
        // WHEN
        Location loc = Location.createOperationalArea(
                "REC-01", ZoneType.RECEIVING_AREA, 5000.0, 10000.0
        );

        // THEN
        assertTrue(loc.isOperational());
        assertFalse(loc.isStorage());
        assertEquals("FLOOR", loc.getLevel()); // Verificamos el default
        assertNull(loc.getAisle()); // Verificamos que no tiene pasillo
    }

    // --- TESTS DE CAPACIDAD Y LÓGICA DE NEGOCIO ---

    @Test
    @DisplayName("Debe aceptar carga cuando hay capacidad suficiente")
    void shouldAcceptLoad_WhenCapacityIsEnough() {
        // GIVEN: Una ubicación de Rack vacía con 100kg de capacidad
        Location loc = Location.createRackPosition(
                "A-01-01", "A", "01", "01",
                ZoneType.DRY_STORAGE, 100.0, 100.0
        );

        // WHEN: Agregamos 2 items pesados (20kg * 2 = 40kg)
        InventoryItem item = createItem(heavyProduct, 2.0, "A-01-01");
        loc.consolidateLoad(item);

        // THEN
        assertEquals(40.0, loc.getCurrentWeight());
        assertEquals(16.0, loc.getCurrentVolume()); // 2un * 8m3 = 16m3
        assertEquals(1, loc.getItems().size());
    }

    @Test
    @DisplayName("Debe rechazar carga (Excepción) cuando excede capacidad")
    void shouldRejectLoad_WhenCapacityExceeded() {
        // GIVEN: Ubicación pequeña (30kg max)
        Location loc = Location.createRackPosition(
                "A-01-01", "A", "01", "01",
                ZoneType.DRY_STORAGE, 30.0, 100.0
        );

        // WHEN: Intentamos meter 40kg (2 * 20kg)
        InventoryItem item = createItem(heavyProduct, 2.0, "A-01-01");

        // THEN: Lanza excepción de dominio
        assertThrows(LocationCapacityExceededException.class, () -> {
            loc.consolidateLoad(item);
        });

        // AND: El estado no debe cambiar (transaccionalidad en memoria)
        assertEquals(0.0, loc.getCurrentWeight());
        assertTrue(loc.getItems().isEmpty());
    }

    @Test
    @DisplayName("Debe acumular peso correctamente al agregar múltiples items distintos")
    void shouldAccumulateWeightCorrectly() {
        Location loc = Location.createRackPosition(
                "A-01-01", "A", "01", "01",
                ZoneType.DRY_STORAGE, 100.0, 100.0
        );

        // Agregamos Heavy (40kg)
        loc.consolidateLoad(createItem(heavyProduct, 2.0, "A-01-01"));

        // Agregamos Light (10kg)
        loc.consolidateLoad(createItem(lightProduct, 2.0, "A-01-01"));

        // Total = 50kg
        assertEquals(50.0, loc.getCurrentWeight());
        assertEquals(2, loc.getItems().size());
    }

    @Test
    @DisplayName("Debe liberar espacio correctamente (Release Load)")
    void shouldReleaseLoadCorrectly() {
        Location loc = Location.createRackPosition(
                "A-01-01", "A", "01", "01",
                ZoneType.DRY_STORAGE, 100.0, 100.0
        );

        // GIVEN
        InventoryItem item = createItem(heavyProduct, 2.0, "A-01-01");
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
    @DisplayName("Debe sumar cantidad (Merge) si el item (SKU + Batch) ya existe")
    void shouldMergeItemsIfSameSkuAndBatch() {
        Location loc = Location.createRackPosition(
                "A-01-01", "A", "01", "01",
                ZoneType.DRY_STORAGE, 100.0, 100.0
        );

        // GIVEN: Entran 2 unidades Light
        InventoryItem item1 = createItem(lightProduct, 2.0, "A-01-01");
        loc.consolidateLoad(item1);

        // WHEN: Entran 3 unidades Light (Mismo SKU y Batch por el helper)
        InventoryItem item2 = createItem(lightProduct, 3.0, "A-01-01");
        loc.consolidateLoad(item2);

        // THEN: Solo 1 objeto InventoryItem, pero con cantidad 5
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