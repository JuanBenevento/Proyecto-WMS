package com.juanbenevento.wms.warehouse.application.service;

import com.juanbenevento.wms.shared.domain.exception.DomainException;
import com.juanbenevento.wms.warehouse.application.mapper.LocationDtoMapper;
import com.juanbenevento.wms.warehouse.application.port.in.command.CreateLocationCommand;
import com.juanbenevento.wms.warehouse.application.port.in.dto.LocationResponse;
import com.juanbenevento.wms.warehouse.application.port.out.LocationRepositoryPort;
import com.juanbenevento.wms.warehouse.domain.exception.LocationNotFoundException;
import com.juanbenevento.wms.warehouse.domain.model.Location;
import com.juanbenevento.wms.warehouse.domain.model.ZoneType;
import com.juanbenevento.wms.inventory.application.port.in.dto.InventoryItemResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepositoryPort locationRepository;

    @Mock
    private LocationDtoMapper mapper;

    @InjectMocks
    private LocationService locationService;

    private Location testLocation;
    private LocationResponse testResponse;

    @BeforeEach
    void setUp() {
        testLocation = Location.createRackPosition(
                "A-01-01",
                "A",
                "01",
                "01",
                ZoneType.DRY_STORAGE,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(10)
        );

        testResponse = new LocationResponse(
                "A-01-01",
                "A", "01", "01",
                ZoneType.DRY_STORAGE.name(),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                List.of()
        );
    }

    @Test
    @DisplayName("Debe retornar todas las ubicaciones")
    void getAllLocations_ReturnsAllLocations() {
        // Given
        when(locationRepository.findAll()).thenReturn(List.of(testLocation));
        when(mapper.toLocationResponse(testLocation)).thenReturn(testResponse);

        // When
        List<LocationResponse> result = locationService.getAllLocations();

        // Then
        assertEquals(1, result.size());
        assertEquals("A-01-01", result.get(0).locationCode());
        verify(locationRepository).findAll();
    }

    @Test
    @DisplayName("Debe crear ubicación exitosamente")
    void createLocation_Success() {
        // Given
        CreateLocationCommand command = new CreateLocationCommand(
                "B-02-03",
                ZoneType.DRY_STORAGE,
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(5)
        );

        when(locationRepository.findByCode("B-02-03")).thenReturn(Optional.empty());
        when(locationRepository.save(any(Location.class))).thenAnswer(i -> i.getArgument(0));
        when(mapper.toLocationResponse(any(Location.class))).thenReturn(testResponse);

        // When
        LocationResponse result = locationService.createLocation(command);

        // Then
        assertNotNull(result);
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la ubicación ya existe")
    void createLocation_AlreadyExists() {
        // Given
        CreateLocationCommand command = new CreateLocationCommand(
                "A-01-01",
                ZoneType.DRY_STORAGE,
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(5)
        );

        when(locationRepository.findByCode("A-01-01")).thenReturn(Optional.of(testLocation));

        // When/Then
        DomainException exception = assertThrows(
                DomainException.class,
                () -> locationService.createLocation(command)
        );

        assertTrue(exception.getMessage().contains("ya existe"));
        verify(locationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar LocationNotFoundException cuando la ubicación no existe")
    void getLocationByCode_NotFound() {
        // Given
        when(locationRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        // When/Then
        assertThrows(
                LocationNotFoundException.class,
                () -> locationService.getLocationByCode("INVALID")
        );
    }

    @Test
    @DisplayName("Debe eliminar ubicación exitosamente")
    void deleteLocation_Success() {
        // Given
        when(locationRepository.findByCode("A-01-01")).thenReturn(Optional.of(testLocation));
        when(locationRepository.hasInventory("A-01-01")).thenReturn(false);

        // When
        locationService.deleteLocation("A-01-01");

        // Then
        verify(locationRepository).delete("A-01-01");
    }

    @Test
    @DisplayName("Debe lanzar excepción al eliminar ubicación con inventario")
    void deleteLocation_WithInventory() {
        // Given
        when(locationRepository.findByCode("A-01-01")).thenReturn(Optional.of(testLocation));
        when(locationRepository.hasInventory("A-01-01")).thenReturn(true);

        // When/Then
        DomainException exception = assertThrows(
                DomainException.class,
                () -> locationService.deleteLocation("A-01-01")
        );

        assertTrue(exception.getMessage().contains("stock asociado"));
        verify(locationRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Debe crear ubicación operacional sin parseo de código")
    void createLocation_OperationalZone() {
        // Given
        CreateLocationCommand command = new CreateLocationCommand(
                "RECEIVING-1",
                ZoneType.RECEIVING_AREA,
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(50)
        );

        when(locationRepository.findByCode("RECEIVING-1")).thenReturn(Optional.empty());
        when(locationRepository.save(any(Location.class))).thenAnswer(i -> i.getArgument(0));
        when(mapper.toLocationResponse(any(Location.class))).thenReturn(testResponse);

        // When
        LocationResponse result = locationService.createLocation(command);

        // Then
        assertNotNull(result);
        verify(locationRepository).save(any(Location.class));
    }
}
