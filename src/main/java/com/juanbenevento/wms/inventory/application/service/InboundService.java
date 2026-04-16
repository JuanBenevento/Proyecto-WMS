package com.juanbenevento.wms.inventory.application.service;

import com.juanbenevento.wms.inventory.application.mapper.InventoryMapper;
import com.juanbenevento.wms.inventory.application.port.in.command.ReceiveInventoryCommand;
import com.juanbenevento.wms.inventory.application.port.in.dto.InventoryItemResponse;
import com.juanbenevento.wms.inventory.application.port.in.usecases.ReceiveInventoryUseCase;
import com.juanbenevento.wms.inventory.application.port.out.InventoryRepositoryPort;
import com.juanbenevento.wms.warehouse.application.port.out.LocationRepositoryPort;
import com.juanbenevento.wms.catalog.application.port.out.ProductRepositoryPort;
import com.juanbenevento.wms.inventory.domain.event.StockReceivedEvent;
import com.juanbenevento.wms.warehouse.domain.exception.LocationNotFoundException;
import com.juanbenevento.wms.catalog.domain.exception.ProductNotFoundException;
import com.juanbenevento.wms.inventory.domain.model.InventoryItem;
import com.juanbenevento.wms.shared.domain.valueobject.BatchNumber;
import com.juanbenevento.wms.shared.domain.valueobject.Lpn;
import com.juanbenevento.wms.shared.domain.valueobject.WmsConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InboundService implements ReceiveInventoryUseCase {

    private final InventoryRepositoryPort inventoryRepository;
    private final ProductRepositoryPort productRepository;
    private final LocationRepositoryPort locationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final InventoryMapper mapper;

    @Override
    @Transactional
    public InventoryItemResponse receiveInventory(ReceiveInventoryCommand command) {
        var product = productRepository.findBySku(command.productSku())
                .orElseThrow(() -> new ProductNotFoundException(command.productSku()));

        var location = locationRepository.findByCode(command.locationCode())
                .orElseThrow(() -> new LocationNotFoundException(command.locationCode()));

        // Crear Value Objects
        Lpn lpn = Lpn.generate();
        BatchNumber batchNumber = BatchNumber.of(command.batchNumber());

        // Crear InventoryItem usando factory method
        InventoryItem newItem = InventoryItem.createReceived(
                lpn,
                command.productSku(),
                product,
                command.quantity(),
                batchNumber,
                command.expiryDate(),
                command.locationCode()
        );

        location.consolidateLoad(newItem);

        inventoryRepository.save(newItem);
        locationRepository.save(location);

        eventPublisher.publishEvent(new StockReceivedEvent(
                lpn.getValue(),
                newItem.getProductSku(),
                newItem.getQuantity(),
                location.getLocationCode(),
                getCurrentUser(),
                LocalDateTime.now()
        ));

        return mapper.toItemResponse(newItem);
    }

    private String getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : WmsConstants.SYSTEM_USER;
    }
}
