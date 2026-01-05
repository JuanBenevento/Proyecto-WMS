package com.juanbenevento.wms.application.service;

import com.juanbenevento.wms.application.mapper.InventoryMapper;
import com.juanbenevento.wms.application.ports.in.command.ReceiveInventoryCommand;
import com.juanbenevento.wms.application.ports.in.dto.InventoryItemResponse;
import com.juanbenevento.wms.application.ports.in.usecases.ReceiveInventoryUseCase;
import com.juanbenevento.wms.application.ports.out.InventoryRepositoryPort;
import com.juanbenevento.wms.application.ports.out.LocationRepositoryPort;
import com.juanbenevento.wms.application.ports.out.ProductRepositoryPort;
import com.juanbenevento.wms.domain.event.StockReceivedEvent;
import com.juanbenevento.wms.domain.exception.LocationNotFoundException;
import com.juanbenevento.wms.domain.exception.ProductNotFoundException;
import com.juanbenevento.wms.domain.model.InventoryItem;
import com.juanbenevento.wms.domain.model.InventoryStatus;
import com.juanbenevento.wms.domain.model.Location;
import com.juanbenevento.wms.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

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
        Product product = productRepository.findBySku(command.productSku())
                .orElseThrow(() -> new ProductNotFoundException(command.productSku()));

        Location location = locationRepository.findByCode(command.locationCode())
                .orElseThrow(() -> new LocationNotFoundException(command.locationCode()));

        InventoryItem newItem = new InventoryItem(
                generateLpn(),
                command.productSku(),
                product,
                command.quantity(),
                command.batchNumber(),
                command.expiryDate(),
                InventoryStatus.IN_QUALITY_CHECK,
                command.locationCode(),
                null // Version
        );

        location.consolidateLoad(newItem);

        inventoryRepository.save(newItem);
        locationRepository.save(location);

        eventPublisher.publishEvent(new StockReceivedEvent(
                newItem.getLpn(), newItem.getProductSku(), newItem.getQuantity(),
                location.getLocationCode(), getCurrentUser(), LocalDateTime.now()
        ));

        return mapper.toItemResponse(newItem);
    }

    private String getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "SYSTEM";
    }

    private String generateLpn() {
        return "LPN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}