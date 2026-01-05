package com.juanbenevento.wms.application.service;

import com.juanbenevento.wms.application.mapper.InventoryMapper;
import com.juanbenevento.wms.application.ports.in.command.InventoryAdjustmentCommand;
import com.juanbenevento.wms.application.ports.in.command.PutAwayInventoryCommand;
import com.juanbenevento.wms.application.ports.in.command.ReceiveInventoryCommand;
import com.juanbenevento.wms.application.ports.in.dto.InventoryItemResponse;
import com.juanbenevento.wms.application.ports.out.InventoryRepositoryPort;
import com.juanbenevento.wms.application.ports.out.LocationRepositoryPort;
import com.juanbenevento.wms.application.ports.out.ProductRepositoryPort;
import com.juanbenevento.wms.domain.event.InventoryAdjustedEvent;
import com.juanbenevento.wms.domain.event.StockReceivedEvent;
import com.juanbenevento.wms.domain.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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

    @InjectMocks
    private InternalOperationsService inventoryService;
    @InjectMocks
    private InboundService inboundService;

    @Test
    void shouldReceiveInventorySuccessfully() {
        String sku = "TV-LG-65";
        String locationCode = "A-01-01";
        ReceiveInventoryCommand command = new ReceiveInventoryCommand(sku, 10.0, locationCode, "BATCH-001", LocalDate.now().plusYears(1));

        Product mockProduct = new Product(UUID.randomUUID(), sku, "TV", "Desc", new Dimensions(10.0,10.0,10.0,5.0), 1L);

        Location mockLocation = Location.createEmpty(locationCode, ZoneType.DRY_STORAGE, 100000.0, 100000.0);

        when(productRepository.findBySku(sku)).thenReturn(Optional.of(mockProduct));
        when(locationRepository.findByCode(locationCode)).thenReturn(Optional.of(mockLocation));

        when(mapper.toItemResponse(any())).thenReturn(new InventoryItemResponse("LPN-1", sku, "TV", 10.0, "AVAILABLE", "B1", null, locationCode));

        InventoryItemResponse result = inboundService.receiveInventory(command);

        assertNotNull(result);
        verify(eventPublisher).publishEvent(any(StockReceivedEvent.class));
        verify(inventoryRepository).save(any(InventoryItem.class));
    }

    @Test
    void shouldMovePhysicalLoad_WhenPutAwayLocationChanges() {
        String lpn = "LPN-123";
        String oldLocCode = "DOCK";
        String newLocCode = "A-01";
        double quantity = 10.0;

        Product product = new Product(UUID.randomUUID(), "SKU-1", "P", "D", new Dimensions(1.0,1.0,1.0, 5.0), 1L);

        InventoryItem item = new InventoryItem(lpn, "SKU-1", product, quantity, "B1", LocalDate.now(), InventoryStatus.IN_QUALITY_CHECK, oldLocCode, 1L);

        Location oldLoc = Location.createEmpty(oldLocCode, ZoneType.DOCK_DOOR, 1000.0, 1000.0);
        oldLoc.consolidateLoad(item);

        Location newLoc = Location.createEmpty(newLocCode, ZoneType.DRY_STORAGE, 1000.0, 1000.0);

        when(inventoryRepository.findByLpn(lpn)).thenReturn(Optional.of(item));
        when(locationRepository.findByCode(oldLocCode)).thenReturn(Optional.of(oldLoc));
        when(locationRepository.findByCode(newLocCode)).thenReturn(Optional.of(newLoc));

        // Ejecución
        inventoryService.putAwayInventory(new PutAwayInventoryCommand(lpn, newLocCode));

        // Verificación
        assertEquals(0.0, oldLoc.getCurrentWeight(), "El peso debió salir del DOCK");
        assertEquals(50.0, newLoc.getCurrentWeight(), "El peso debió entrar en A-01 (10u * 5kg)");
        assertEquals(InventoryStatus.AVAILABLE, item.getStatus(), "El estado debió cambiar a AVAILABLE");

        verify(locationRepository, times(2)).save(any(Location.class));
    }

    @Test
    void shouldProcessAdjustmentAndPublishEvent() {
        String lpn = "LPN-TEST";
        InventoryAdjustmentCommand command = new InventoryAdjustmentCommand(lpn, 8.0, "Rotura");

        Product product = new Product(UUID.randomUUID(), "SKU-1", "P", "D", new Dimensions(1.0,1.0,1.0, 1.0), 1L);
        InventoryItem item = new InventoryItem(lpn, "SKU-1", product, 10.0, "B1", LocalDate.now(), InventoryStatus.AVAILABLE, "A-01", 1L);

        Location loc = Location.createEmpty("A-01", ZoneType.DRY_STORAGE, 1000.0, 1000.0);
        loc.consolidateLoad(item);

        when(inventoryRepository.findByLpn(lpn)).thenReturn(Optional.of(item));
        when(locationRepository.findByCode("A-01")).thenReturn(Optional.of(loc));

        inventoryService.processInventoryAdjustment(command);

        assertEquals(8.0, item.getQuantity());
        assertEquals(8.0, loc.getCurrentWeight());

        verify(eventPublisher).publishEvent(any(InventoryAdjustedEvent.class));
    }
}