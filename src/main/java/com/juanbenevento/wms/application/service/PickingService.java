package com.juanbenevento.wms.application.service;

import com.juanbenevento.wms.application.ports.in.command.AllocateStockCommand;
import com.juanbenevento.wms.application.ports.in.usecases.AllocateStockUseCase;
import com.juanbenevento.wms.application.ports.out.InventoryRepositoryPort;
import com.juanbenevento.wms.application.ports.out.LocationRepositoryPort;
import com.juanbenevento.wms.domain.event.StockReservedEvent;
import com.juanbenevento.wms.domain.exception.DomainException;
import com.juanbenevento.wms.domain.model.InventoryItem;
import com.juanbenevento.wms.domain.model.InventoryStatus;
import com.juanbenevento.wms.domain.model.Location;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional // La transacción mantiene el LOCK de base de datos
    public void allocateStock(AllocateStockCommand command) {
        log.info("Iniciando asignación segura para SKU: {} Cantidad: {}", command.sku(), command.quantity());

        List<InventoryItem> availableItems = inventoryRepository.findAvailableStockForAllocation(command.sku());

        double quantityNeeded = command.quantity();
        double totalAvailable = availableItems.stream().mapToDouble(InventoryItem::getQuantity).sum();

        if (totalAvailable < quantityNeeded) {
            throw new DomainException("Stock insuficiente. Disponible: " + totalAvailable + ", Solicitado: " + quantityNeeded);
        }

        // 3. Algoritmo de Asignación (FEFO ya garantizado por el orden de la query)
        for (InventoryItem item : availableItems) {
            if (quantityNeeded <= 0) break;

            Location location = locationRepository.findByCode(item.getLocationCode()).orElseThrow();
            double currentQty = item.getQuantity();
            double quantityToTake = Math.min(currentQty, quantityNeeded);

            // Caso A: Tomamos todo el pallet/caja
            if (currentQty == quantityToTake) {
                item.setStatus(InventoryStatus.RESERVED);
                // No necesitamos cambiar peso en Location porque el item sigue ahí físicamente, solo cambió de dueño lógico.
                inventoryRepository.save(item); // Incrementa @Version automáticamente
            }
            // Caso B: Tomamos una parte (Split)
            else {
                // Sacamos el item original de la ubicación para recalcular pesos
                location.releaseLoad(item);

                // Reducimos cantidad del original
                item.setQuantity(currentQty - quantityToTake);
                location.consolidateLoad(item); // Vuelve a entrar con menos peso
                inventoryRepository.save(item);

                // Creamos el nuevo item RESERVADO (el "hijo")
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

                location.consolidateLoad(reservedPart); // Entra el hijo a la ubicación
                inventoryRepository.save(reservedPart);
                locationRepository.save(location); // Guardamos estado final de la ubicación
            }

            quantityNeeded -= quantityToTake;
        }

        // 4. Evento de éxito
        eventPublisher.publishEvent(new StockReservedEvent(
                command.sku(), command.quantity(), getCurrentUser(), LocalDateTime.now()
        ));
    }

    private String generatePickingLpn() {
        return "PICK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }
}