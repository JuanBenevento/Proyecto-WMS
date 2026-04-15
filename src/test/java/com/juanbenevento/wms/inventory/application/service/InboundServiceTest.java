package com.juanbenevento.wms.inventory.application.service;

import com.juanbenevento.wms.catalog.application.port.out.ProductRepositoryPort;
import com.juanbenevento.wms.catalog.domain.model.Product;
import com.juanbenevento.wms.inventory.application.mapper.InventoryMapper;
import com.juanbenevento.wms.inventory.application.port.in.command.ReceiveInventoryCommand;
import com.juanbenevento.wms.inventory.application.port.in.dto.InventoryItemResponse;
import com.juanbenevento.wms.inventory.application.port.out.InventoryRepositoryPort;
import com.juanbenevento.wms.catalog.domain.exception.ProductNotFoundException;
import com.juanbenevento.wms.inventory.domain.model.InventoryItem;
import com.juanbenevento.wms.warehouse.application.port.out.LocationRepositoryPort;
import com.juanbenevento.wms.warehouse.domain.exception.LocationNotFoundException;
import com.juanbenevento.wms.warehouse.domain.model.Location;
import com.juanbenevento.wms.warehouse.domain.model.ZoneType;
import com.juanbenevento.wms.shared.domain.valueobject.Dimensions;
import com.juanbenevento.wms.inventory.domain.event.StockReceivedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InboundServiceTest {

    @Mock
    private InventoryRepositoryPort inventoryRepository;

    @Mock
    private ProductRepositoryPort productRepository;

    @Mock
    private LocationRepositoryPort locationRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private InventoryMapper mapper;

    @InjectMocks
    private InboundService inboundService;

    private Product testProduct;
    private Location testLocation;

    @BeforeEach
    void setUp() {
        testProduct = Product.create(
                "SKU-TEST",
                "Producto Test",
                "Test description",
                new Dimensions(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.TEN)
        );

        testLocation = Location.createOperationalArea(
                "RECEIVING-1",
                ZoneType.RECEIVING_AREA,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(100)
        );
    }

    @Test
    @DisplayName("Debe recibir inventario exitosamente cuando producto y ubicación existen")
    void receiveInventory_Success() {
        // Given - Command: productSku, quantity, locationCode, batchNumber, expiryDate
        LocalDate expiryDate = LocalDate.now().plusDays(30);
        ReceiveInventoryCommand command = new ReceiveInventoryCommand(
                "SKU-TEST",
                BigDecimal.valueOf(10),
                "RECEIVING-1",
                "BATCH-001",
                expiryDate
        );

        InventoryItemResponse expectedResponse = new InventoryItemResponse(
                "LPN-TEST1234",
                "SKU-TEST",
                "Producto Test",
                BigDecimal.valueOf(10),
                "IN_QUALITY_CHECK",
                "BATCH-001",
                expiryDate,
                "RECEIVING-1"
        );

        when(productRepository.findBySku("SKU-TEST")).thenReturn(Optional.of(testProduct));
        when(locationRepository.findByCode("RECEIVING-1")).thenReturn(Optional.of(testLocation));
        when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(i -> i.getArgument(0));
        when(locationRepository.save(any(Location.class))).thenAnswer(i -> i.getArgument(0));
        when(mapper.toItemResponse(any(InventoryItem.class))).thenReturn(expectedResponse);

        // When
        InventoryItemResponse result = inboundService.receiveInventory(command);

        // Then
        assertNotNull(result);
        assertEquals("SKU-TEST", result.sku());
        assertEquals(BigDecimal.valueOf(10), result.quantity());
        assertEquals("RECEIVING-1", result.locationCode());

        // Verify repository calls
        verify(inventoryRepository).save(any(InventoryItem.class));
        verify(locationRepository).save(any(Location.class));
        verify(eventPublisher).publishEvent(any(StockReceivedEvent.class));
    }

    @Test
    @DisplayName("Debe lanzar ProductNotFoundException cuando el producto no existe")
    void receiveInventory_ProductNotFound() {
        // Given
        ReceiveInventoryCommand command = new ReceiveInventoryCommand(
                "INVALID-SKU",
                BigDecimal.valueOf(10),
                "RECEIVING-1",
                "BATCH-001",
                LocalDate.now().plusDays(30)
        );

        when(productRepository.findBySku("INVALID-SKU")).thenReturn(Optional.empty());

        // When/Then
        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> inboundService.receiveInventory(command)
        );

        assertTrue(exception.getMessage().contains("INVALID-SKU"));
        verify(inventoryRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Debe lanzar LocationNotFoundException cuando la ubicación no existe")
    void receiveInventory_LocationNotFound() {
        // Given
        ReceiveInventoryCommand command = new ReceiveInventoryCommand(
                "SKU-TEST",
                BigDecimal.valueOf(10),
                "INVALID-LOC",
                "BATCH-001",
                LocalDate.now().plusDays(30)
        );

        when(productRepository.findBySku("SKU-TEST")).thenReturn(Optional.of(testProduct));
        when(locationRepository.findByCode("INVALID-LOC")).thenReturn(Optional.empty());

        // When/Then
        LocationNotFoundException exception = assertThrows(
                LocationNotFoundException.class,
                () -> inboundService.receiveInventory(command)
        );

        assertTrue(exception.getMessage().contains("INVALID-LOC"));
        verify(inventoryRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Debe generar LPN y BatchNumber correctamente")
    void receiveInventory_GeneratesLpnAndBatchNumber() {
        // Given
        LocalDate expiryDate = LocalDate.now().plusDays(60);
        ReceiveInventoryCommand command = new ReceiveInventoryCommand(
                "SKU-TEST",
                BigDecimal.valueOf(5),
                "RECEIVING-1",
                "BATCH-NEW-001",
                expiryDate
        );

        InventoryItemResponse response = new InventoryItemResponse(
                "LPN-12345678",
                "SKU-TEST",
                "Producto Test",
                BigDecimal.valueOf(5),
                "IN_QUALITY_CHECK",
                "BATCH-NEW-001",
                expiryDate,
                "RECEIVING-1"
        );

        when(productRepository.findBySku("SKU-TEST")).thenReturn(Optional.of(testProduct));
        when(locationRepository.findByCode("RECEIVING-1")).thenReturn(Optional.of(testLocation));
        when(inventoryRepository.save(any(InventoryItem.class))).thenAnswer(i -> i.getArgument(0));
        when(locationRepository.save(any(Location.class))).thenAnswer(i -> i.getArgument(0));
        when(mapper.toItemResponse(any(InventoryItem.class))).thenReturn(response);

        // When
        InventoryItemResponse result = inboundService.receiveInventory(command);

        // Then
        ArgumentCaptor<InventoryItem> itemCaptor = ArgumentCaptor.forClass(InventoryItem.class);
        verify(mapper).toItemResponse(itemCaptor.capture());

        InventoryItem capturedItem = itemCaptor.getValue();
        assertNotNull(capturedItem.getLpn());
        assertNotNull(capturedItem.getBatchNumber());
        assertEquals("BATCH-NEW-001", capturedItem.getBatchNumber().getValue());
    }
}
