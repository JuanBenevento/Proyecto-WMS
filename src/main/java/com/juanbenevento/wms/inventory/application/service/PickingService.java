package com.juanbenevento.wms.inventory.application.service;

import com.juanbenevento.wms.inventory.application.port.in.command.AllocateStockCommand;
import com.juanbenevento.wms.inventory.application.port.in.usecases.AllocateStockUseCase;
import com.juanbenevento.wms.inventory.application.port.out.InventoryRepositoryPort;
import com.juanbenevento.wms.warehouse.application.port.out.LocationRepositoryPort;
import com.juanbenevento.wms.inventory.domain.event.StockReservedEvent;
import com.juanbenevento.wms.shared.domain.exception.DomainException;
import com.juanbenevento.wms.inventory.domain.model.InventoryItem;
import com.juanbenevento.wms.inventory.domain.model.InventoryStatus;
import com.juanbenevento.wms.warehouse.domain.model.Location;
import com.juanbenevento.wms.shared.domain.valueobject.WmsConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PickingService implements AllocateStockUseCase {
    private final InventoryRepositoryPort inventoryRepository;
    private final LocationRepositoryPort locationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void allocateStock(AllocateStockCommand command) {
        log.info("Iniciando asignación segura para SKU: {} Cantidad: {}", command.sku(), command.quantity());

        List<InventoryItem> availableItems = inventoryRepository.findAvailableStockForAllocation(command.sku());

        BigDecimal quantityNeeded = command.quantity();

        BigDecimal totalAvailable = availableItems.stream()
                .map(InventoryItem::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAvailable.compareTo(quantityNeeded) < 0) {
            throw new DomainException("Stock insuficiente. Disponible: " + totalAvailable + ", Solicitado: " + quantityNeeded);
        }

        for (InventoryItem item : availableItems) {
            if (quantityNeeded.compareTo(BigDecimal.ZERO) <= 0) break;

            Location location = locationRepository.findByCode(item.getLocationCode())
                    .orElseThrow(() -> new DomainException("Ubicación no encontrada: " + item.getLocationCode()));

            BigDecimal currentQty = item.getQuantity();
            BigDecimal quantityToTake = currentQty.min(quantityNeeded);

            if (currentQty.compareTo(quantityToTake) == 0) {
                item.setStatus(InventoryStatus.RESERVED);
                inventoryRepository.save(item);
            }
            else {
                location.releaseLoad(item);

                item.setQuantity(currentQty.subtract(quantityToTake));
                location.consolidateLoad(item);
                inventoryRepository.save(item);

                InventoryItem reservedPart = new InventoryItem(
                        generatePickingLpn(),
                        item.getProductSku(),
                        item.getProduct(),
                        quantityToTake,
                        item.getBatchNumber(),
                        item.getExpiryDate(),
                        InventoryStatus.RESERVED,
                        item.getLocationCode(),
                        null
                );

                location.consolidateLoad(reservedPart);
                inventoryRepository.save(reservedPart);
                locationRepository.save(location);
            }

            quantityNeeded = quantityNeeded.subtract(quantityToTake);
        }

        eventPublisher.publishEvent(new StockReservedEvent(
                command.sku(), command.quantity(), getCurrentUser(), LocalDateTime.now()
        ));
    }

    private String generatePickingLpn() {
        return WmsConstants.PICK_PREFIX + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }
}