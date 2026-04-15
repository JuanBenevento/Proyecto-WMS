package com.juanbenevento.wms.inventory.application.service;

import com.juanbenevento.wms.inventory.application.port.in.command.AllocateStockCommand;
import com.juanbenevento.wms.inventory.application.port.out.InventoryRepositoryPort;
import com.juanbenevento.wms.inventory.domain.model.InventoryItem;
import com.juanbenevento.wms.inventory.domain.model.InventoryStatus;
import com.juanbenevento.wms.warehouse.application.port.out.LocationRepositoryPort;
import com.juanbenevento.wms.catalog.domain.model.Product;
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
        String sku = "SKU-A";
        String locCode = "A-01-01";

        Product product = new Product(UUID.randomUUID(), sku, "P", "D",
                new Dimensions(new BigDecimal("1.0"), new BigDecimal("1.0"), new BigDecimal("1.0"), new BigDecimal("1.0")), 1L);

        InventoryItem originalItem = InventoryItem.createReceived(
                Lpn.fromRaw("LPN-ORIGINAL"), sku, product, new BigDecimal("10.0"),
                BatchNumber.of("B1"), LocalDate.now(), locCode
        );

        Location location = Location.createRackPosition(
                locCode, "A", "01", "01",
                ZoneType.DRY_STORAGE, new BigDecimal("100.0"), new BigDecimal("100.0")
        );

        location.consolidateLoad(originalItem);

        when(inventoryRepository.findAvailableStockForAllocation(sku)).thenAnswer(inv -> {
            List<InventoryItem> list = new java.util.ArrayList<>();
            list.add(originalItem);
            return list;
        });

        when(locationRepository.findByCode(locCode)).thenReturn(Optional.of(location));

        doAnswer(invocation -> {
            InventoryItem itemGuardado = invocation.getArgument(0);
            if (itemGuardado.getLpn().getValue().equals("LPN-ORIGINAL")) {
                // Validación con compareTo para BigDecimal
                if (itemGuardado.getQuantity().compareTo(new BigDecimal("6.0")) != 0) {
                    throw new RuntimeException("Error: Cantidad esperada 6.0 pero fue " + itemGuardado.getQuantity());
                }
            }
            return itemGuardado;
        }).when(inventoryRepository).save(any(InventoryItem.class));

        pickingService.allocateStock(new AllocateStockCommand(sku, new BigDecimal("4.0")));

        verify(inventoryRepository, atLeast(2)).save(any());
    }
}