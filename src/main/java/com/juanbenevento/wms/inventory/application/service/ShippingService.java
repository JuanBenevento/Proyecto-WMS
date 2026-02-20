package com.juanbenevento.wms.inventory.application.service;

import com.juanbenevento.wms.inventory.application.port.in.command.ShipStockCommand;
import com.juanbenevento.wms.inventory.application.port.in.usecases.ShipStockUseCase;
import com.juanbenevento.wms.inventory.application.port.out.InventoryRepositoryPort;
import com.juanbenevento.wms.warehouse.application.port.out.LocationRepositoryPort;
import com.juanbenevento.wms.inventory.domain.event.StockShippedEvent;
import com.juanbenevento.wms.shared.domain.exception.DomainException;
import com.juanbenevento.wms.inventory.domain.model.InventoryItem;
import com.juanbenevento.wms.inventory.domain.model.InventoryStatus;
import com.juanbenevento.wms.warehouse.domain.model.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingService implements ShipStockUseCase {

    private final InventoryRepositoryPort inventoryRepository;
    private final LocationRepositoryPort locationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void shipStock(ShipStockCommand command) {
        List<InventoryItem> reservedItems = inventoryRepository.findReservedStock(command.sku());

        BigDecimal totalReserved = reservedItems.stream()
                .map(InventoryItem::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalReserved.compareTo(command.quantity()) < 0) {
            throw new DomainException("No hay suficiente stock RESERVADO para despachar. Reservado: " + totalReserved);
        }

        BigDecimal quantityToShip = command.quantity();

        for (InventoryItem item : reservedItems) {
            if (quantityToShip.compareTo(BigDecimal.ZERO) <= 0) break;

            Location location = locationRepository.findByCode(item.getLocationCode())
                    .orElseThrow(() -> new DomainException("Ubicación no encontrada"));

            BigDecimal currentQty = item.getQuantity();
            BigDecimal takenQty = currentQty.min(quantityToShip);

            location.releaseLoad(item);

            if (takenQty.compareTo(currentQty) >= 0) {
                item.setStatus(InventoryStatus.SHIPPED);
                item.setQuantity(takenQty);
            } else {
                item.setQuantity(currentQty.subtract(takenQty));
                location.consolidateLoad(item);
            }

            locationRepository.save(location);
            inventoryRepository.save(item);

            eventPublisher.publishEvent(new StockShippedEvent(
                    item.getProductSku(), takenQty, location.getLocationCode(),
                    getCurrentUser(), LocalDateTime.now()
            ));

            quantityToShip = quantityToShip.subtract(takenQty);
        }
    }

    private String getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }
}