package com.juanbenevento.wms.inventory.application.service;

import com.juanbenevento.wms.inventory.application.mapper.InventoryMapper;
import com.juanbenevento.wms.inventory.application.port.in.command.InventoryAdjustmentCommand;
import com.juanbenevento.wms.inventory.application.port.in.command.PutAwayInventoryCommand;
import com.juanbenevento.wms.inventory.application.port.in.command.ReceiveInventoryCommand;
import com.juanbenevento.wms.inventory.application.port.in.dto.InventoryItemResponse;
import com.juanbenevento.wms.inventory.application.port.out.InventoryRepositoryPort;
import com.juanbenevento.wms.inventory.domain.model.InventoryItem;
import com.juanbenevento.wms.inventory.domain.model.InventoryStatus;
import com.juanbenevento.wms.warehouse.application.port.out.LocationRepositoryPort;
import com.juanbenevento.wms.catalog.application.port.out.ProductRepositoryPort;
import com.juanbenevento.wms.catalog.domain.model.Product;
import com.juanbenevento.wms.inventory.domain.event.InventoryAdjustedEvent;
import com.juanbenevento.wms.inventory.domain.event.StockReceivedEvent;
import com.juanbenevento.wms.shared.domain.valueobject.BatchNumber;
import com.juanbenevento.wms.shared.domain.valueobject.Dimensions;
import com.juanbenevento.wms.shared.domain.valueobject.Lpn;
import com.juanbenevento.wms.warehouse.domain.model.Location;
import com.juanbenevento.wms.warehouse.domain.model.ZoneType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {
    @Mock private InventoryRepositoryPort inventoryRepository;
    @Mock private ProductRepositoryPort productRepository;
    @Mock private LocationRepositoryPort locationRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private InventoryMapper mapper;

    @InjectMocks private InternalOperationsService internalOperationsService;
    @InjectMocks private InboundService inboundService;

    @Test
    void shouldReceiveInventorySuccessfully() {
        String sku = "TV-LG-65";
        String locationCode = "A-01-01";
        // Command con BigDecimal
        ReceiveInventoryCommand command = new ReceiveInventoryCommand(sku, new BigDecimal("10.0"), locationCode, "BATCH-001", LocalDate.now().plusYears(1));

        Product mockProduct = new Product(UUID.randomUUID(), sku, "TV", "Desc",
                new Dimensions(new BigDecimal("10.0"), new BigDecimal("10.0"), new BigDecimal("10.0"), new BigDecimal("5.0")), 1L);

        Location mockLocation = Location.createRackPosition(
                locationCode, "A", "01", "01",
                ZoneType.DRY_STORAGE, new BigDecimal("100000.0"), new BigDecimal("100000.0")
        );

        when(productRepository.findBySku(sku)).thenReturn(Optional.of(mockProduct));
        when(locationRepository.findByCode(locationCode)).thenReturn(Optional.of(mockLocation));
        when(mapper.toItemResponse(any())).thenReturn(new InventoryItemResponse("LPN-1", sku, "TV", new BigDecimal("10.0"), "AVAILABLE", "B1", null, locationCode));

        InventoryItemResponse result = inboundService.receiveInventory(command);

        assertNotNull(result);
        verify(eventPublisher).publishEvent(any(StockReceivedEvent.class));
        verify(inventoryRepository).save(any(InventoryItem.class));
    }

    @Test
    void shouldMovePhysicalLoad_WhenPutAwayLocationChanges() {
        String lpn = "LPN-123";
        String oldLocCode = "DOCK-01";
        String newLocCode = "A-01-01";
        BigDecimal quantity = new BigDecimal("10.0");

        Product product = new Product(UUID.randomUUID(), "SKU-1", "P", "D",
                new Dimensions(new BigDecimal("1.0"), new BigDecimal("1.0"), new BigDecimal("1.0"), new BigDecimal("5.0")), 1L);

        InventoryItem item = InventoryItem.createReceived(Lpn.fromRaw(lpn), "SKU-1", product, quantity, BatchNumber.of("B1"), LocalDate.now(), oldLocCode);

        Location oldLoc = Location.createOperationalArea(oldLocCode, ZoneType.DOCK_DOOR, new BigDecimal("1000.0"), new BigDecimal("1000.0"));
        oldLoc.consolidateLoad(item);

        Location newLoc = Location.createRackPosition(newLocCode, "A", "01", "01", ZoneType.DRY_STORAGE, new BigDecimal("1000.0"), new BigDecimal("1000.0"));

        when(inventoryRepository.findByLpn(lpn)).thenReturn(Optional.of(item));
        when(locationRepository.findByCode(oldLocCode)).thenReturn(Optional.of(oldLoc));
        when(locationRepository.findByCode(newLocCode)).thenReturn(Optional.of(newLoc));

        internalOperationsService.putAwayInventory(new PutAwayInventoryCommand(lpn, newLocCode));

        // Comparación con compareTo vía BigDecimal.ZERO
        assertEquals(0, oldLoc.getCurrentWeight().compareTo(BigDecimal.ZERO), "El peso debió salir del DOCK");
        assertEquals(0, newLoc.getCurrentWeight().compareTo(new BigDecimal("50.0")), "El peso debió entrar (10u * 5kg)");
        assertEquals(InventoryStatus.AVAILABLE, item.getStatus());

        verify(locationRepository, times(2)).save(any(Location.class));
    }

    @Test
    void shouldProcessAdjustmentAndPublishEvent() {
        String lpn = "LPN-TEST";
        String locCode = "A-01-01";
        InventoryAdjustmentCommand command = new InventoryAdjustmentCommand(lpn, new BigDecimal("8.0"), "Rotura");

        Product product = new Product(UUID.randomUUID(), "SKU-1", "P", "D",
                new Dimensions(new BigDecimal("1.0"), new BigDecimal("1.0"), new BigDecimal("1.0"), new BigDecimal("1.0")), 1L);
        InventoryItem item = InventoryItem.createReceived(Lpn.fromRaw(lpn), "SKU-1", product, new BigDecimal("10.0"), BatchNumber.of("B1"), LocalDate.now(), locCode);

        Location loc = Location.createRackPosition(locCode, "A", "01", "01", ZoneType.DRY_STORAGE, new BigDecimal("1000.0"), new BigDecimal("1000.0"));
        loc.consolidateLoad(item);

        when(inventoryRepository.findByLpn(lpn)).thenReturn(Optional.of(item));
        when(locationRepository.findByCode(locCode)).thenReturn(Optional.of(loc));

        internalOperationsService.processInventoryAdjustment(command);

        assertEquals(0, item.getQuantity().compareTo(new BigDecimal("8.0")));
        assertEquals(0, loc.getCurrentWeight().compareTo(new BigDecimal("8.0")));

        verify(eventPublisher).publishEvent(any(InventoryAdjustedEvent.class));
    }
}