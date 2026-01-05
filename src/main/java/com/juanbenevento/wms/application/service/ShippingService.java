package com.juanbenevento.wms.application.service;

import com.juanbenevento.wms.application.ports.in.command.ShipStockCommand;
import com.juanbenevento.wms.application.ports.in.usecases.ShipStockUseCase;
import com.juanbenevento.wms.application.ports.out.InventoryRepositoryPort;
import com.juanbenevento.wms.application.ports.out.LocationRepositoryPort;
import com.juanbenevento.wms.domain.event.StockShippedEvent;
import com.juanbenevento.wms.domain.exception.DomainException;
import com.juanbenevento.wms.domain.model.InventoryItem;
import com.juanbenevento.wms.domain.model.InventoryStatus;
import com.juanbenevento.wms.domain.model.Location;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        double totalReserved = reservedItems.stream().mapToDouble(InventoryItem::getQuantity).sum();
        if (totalReserved < command.quantity()) {
            throw new DomainException("No hay suficiente stock RESERVADO para despachar. Reservado: " + totalReserved);
        }

        double quantityToShip = command.quantity();

        for (InventoryItem item : reservedItems) {
            if (quantityToShip <= 0) break;

            Location location = locationRepository.findByCode(item.getLocationCode()).orElseThrow();

            double currentQty = item.getQuantity();
            double takenQty = Math.min(currentQty, quantityToShip);

            // 1. Liberar espacio físico (DOMINIO RICO)
            location.releaseLoad(item);

            if (takenQty >= currentQty) {
                // Se va todo el pallet -> SHIPPED
                item.setStatus(InventoryStatus.SHIPPED);
                item.setQuantity(takenQty);
            } else {
                // Parcial -> Queda un remanente
                item.setQuantity(currentQty - takenQty);
                // Volvemos a consolidar el remanente en la ubicación
                location.consolidateLoad(item);
            }

            locationRepository.save(location);
            inventoryRepository.save(item);

            eventPublisher.publishEvent(new StockShippedEvent(
                    item.getProductSku(), takenQty, location.getLocationCode(),
                    getCurrentUser(), LocalDateTime.now()
            ));

            quantityToShip -= takenQty;
        }
    }

    private String getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }
}