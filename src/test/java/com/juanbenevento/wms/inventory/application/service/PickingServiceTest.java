package com.juanbenevento.wms.inventory.application.service;

import com.juanbenevento.wms.inventory.application.port.in.command.AllocateStockCommand;
import com.juanbenevento.wms.inventory.application.port.out.InventoryRepositoryPort;
import com.juanbenevento.wms.inventory.domain.model.InventoryItem;
import com.juanbenevento.wms.inventory.domain.model.InventoryStatus;
import com.juanbenevento.wms.warehouse.application.port.out.LocationRepositoryPort;
import com.juanbenevento.wms.catalog.domain.model.Product;
import com.juanbenevento.wms.shared.domain.valueobject.Dimensions;
import com.juanbenevento.wms.warehouse.domain.model.Location;
import com.juanbenevento.wms.warehouse.domain.model.ZoneType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PickingServiceTest {

    @Mock InventoryRepositoryPort inventoryRepository;
    @Mock LocationRepositoryPort locationRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks
    PickingService pickingService;

    @Test
    void shouldSplitInventoryWhenAllocationIsPartial() {
        // GIVEN
        String sku = "SKU-A";
        String locCode = "A-01-01"; // Usamos formato granular correcto

        // Producto dummy
        Product product = new Product(UUID.randomUUID(), sku, "P", "D", new Dimensions(1.0,1.0,1.0, 1.0), 1L);

        // Item Original: 10 unidades
        InventoryItem originalItem = new InventoryItem(
                "LPN-ORIGINAL", sku, product, 10.0, "B1",
                LocalDate.now(), InventoryStatus.AVAILABLE, locCode, 1L
        );

        // Ubicación Mock: Usamos la Factory de Rack
        Location location = Location.createRackPosition(
                locCode, "A", "01", "01",
                ZoneType.DRY_STORAGE, 100.0, 100.0
        );

        // Cargamos el item en la ubicación para que el estado sea consistente
        location.consolidateLoad(originalItem);

        // Mocks
        when(inventoryRepository.findAvailableStockForAllocation(sku)).thenAnswer(inv -> {
            List<InventoryItem> list = new java.util.ArrayList<>();
            list.add(originalItem);
            return list;
        });

        when(locationRepository.findByCode(locCode)).thenReturn(Optional.of(location));

        // Verificación de comportamiento en el save
        doAnswer(invocation -> {
            InventoryItem itemGuardado = invocation.getArgument(0);

            if (itemGuardado.getLpn().equals("LPN-ORIGINAL")) {
                // Si pedimos 4, al original le deben quedar 6
                if (itemGuardado.getQuantity() != 6.0) {
                    throw new RuntimeException("TEST FALLIDO: El servicio intentó guardar LPN-ORIGINAL con cantidad " + itemGuardado.getQuantity() + " (Se esperaba 6.0)");
                }
            }
            return itemGuardado;
        }).when(inventoryRepository).save(any(InventoryItem.class));

        // WHEN: Pedimos 4 unidades
        pickingService.allocateStock(new AllocateStockCommand(sku, 4.0));

        // THEN
        // Debe guardar el original (actualizado a 6) y el nuevo (creado con 4)
        verify(inventoryRepository, atLeast(2)).save(any());
    }
}