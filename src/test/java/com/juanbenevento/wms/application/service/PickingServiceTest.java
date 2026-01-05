package com.juanbenevento.wms.application.service;

import com.juanbenevento.wms.application.ports.in.command.AllocateStockCommand;
import com.juanbenevento.wms.application.ports.out.InventoryRepositoryPort;
import com.juanbenevento.wms.application.ports.out.LocationRepositoryPort;
import com.juanbenevento.wms.domain.model.*;
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

    @InjectMocks PickingService pickingService;

    @Test
    void shouldSplitInventoryWhenAllocationIsPartial() {
        // GIVEN
        String sku = "SKU-A";
        String locCode = "A-01";

        Product product = new Product(UUID.randomUUID(), sku, "P", "D", new Dimensions(1.0,1.0,1.0, 1.0), 1L);

        InventoryItem originalItem = new InventoryItem("LPN-ORIGINAL", sku, product, 10.0, "B1", LocalDate.now(), InventoryStatus.AVAILABLE, locCode, 1L);

        Location location = Location.createEmpty(locCode, ZoneType.DRY_STORAGE, 100.0, 100.0);
        location.consolidateLoad(originalItem);

        when(inventoryRepository.findAvailableStockForAllocation(sku)).thenAnswer(inv -> {
            List<InventoryItem> list = new java.util.ArrayList<>();
            list.add(originalItem);
            return list;
        });

        when(locationRepository.findByCode(locCode)).thenReturn(Optional.of(location));

        doAnswer(invocation -> {
            InventoryItem itemGuardado = invocation.getArgument(0);

            if (itemGuardado.getLpn().equals("LPN-ORIGINAL")) {
                if (itemGuardado.getQuantity() != 6.0) {
                    throw new RuntimeException("TEST FALLIDO: El servicio intentó guardar LPN-ORIGINAL con cantidad " + itemGuardado.getQuantity() + " (Se esperaba 6.0)");
                }
            }
            return itemGuardado;
        }).when(inventoryRepository).save(any(InventoryItem.class));

        // WHEN
        pickingService.allocateStock(new AllocateStockCommand(sku, 4.0));

        // THEN
        verify(inventoryRepository, atLeast(2)).save(any());
    }
}