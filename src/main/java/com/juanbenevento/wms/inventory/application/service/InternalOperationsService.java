package com.juanbenevento.wms.inventory.application.service;

import com.juanbenevento.wms.inventory.application.port.in.command.InternalMoveCommand;
import com.juanbenevento.wms.inventory.application.port.in.command.InventoryAdjustmentCommand;
import com.juanbenevento.wms.inventory.application.port.in.command.PutAwayInventoryCommand;
import com.juanbenevento.wms.inventory.application.port.in.usecases.ManageInventoryOperationsUseCase;
import com.juanbenevento.wms.inventory.application.port.in.usecases.PutAwayUseCase;
import com.juanbenevento.wms.inventory.application.port.out.InventoryRepositoryPort;
import com.juanbenevento.wms.warehouse.application.port.out.LocationRepositoryPort;
import com.juanbenevento.wms.inventory.domain.event.InventoryAdjustedEvent;
import com.juanbenevento.wms.inventory.domain.event.StockMovedEvent;
import com.juanbenevento.wms.inventory.domain.exception.InventoryItemNotFoundException;
import com.juanbenevento.wms.warehouse.domain.exception.LocationNotFoundException;
import com.juanbenevento.wms.inventory.domain.model.InventoryItem;
import com.juanbenevento.wms.inventory.domain.model.InventoryStatus;
import com.juanbenevento.wms.warehouse.domain.model.Location;
import com.juanbenevento.wms.shared.domain.valueobject.WmsConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InternalOperationsService implements PutAwayUseCase, ManageInventoryOperationsUseCase {

    private final InventoryRepositoryPort inventoryRepository;
    private final LocationRepositoryPort locationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void putAwayInventory(PutAwayInventoryCommand command) {
        InventoryItem item = inventoryRepository.findByLpn(command.lpn())
                .orElseThrow(() -> new InventoryItemNotFoundException(command.lpn()));

        if (item.getLocationCode() != null && item.getLocationCode().equals(command.targetLocationCode())) {
            return;
        }

        Location oldLoc = locationRepository.findByCode(item.getLocationCode())
                .orElseThrow(() -> new LocationNotFoundException(item.getLocationCode(), "Origen"));
        Location newLoc = locationRepository.findByCode(command.targetLocationCode())
                .orElseThrow(() -> new LocationNotFoundException(command.targetLocationCode(), "Destino"));

        oldLoc.releaseLoad(item);
        newLoc.consolidateLoad(item);

        item.moveTo(command.targetLocationCode());
        item.approveQualityCheck();

        locationRepository.save(oldLoc);
        locationRepository.save(newLoc);
        inventoryRepository.save(item);

        eventPublisher.publishEvent(new StockMovedEvent(
                item.getLpn().getValue(),
                item.getProductSku(),
                item.getQuantity(),
                oldLoc.getLocationCode(),
                newLoc.getLocationCode(),
                getCurrentUser(),
                "PUT-AWAY",
                LocalDateTime.now()
        ));
    }

    @Override
    @Transactional
    public void processInternalMove(InternalMoveCommand command) {
        InventoryItem item = inventoryRepository.findByLpn(command.lpn())
                .orElseThrow(() -> new InventoryItemNotFoundException(command.lpn()));

        Location oldLoc = locationRepository.findByCode(item.getLocationCode())
                .orElseThrow(() -> new LocationNotFoundException(item.getLocationCode(), "Origen"));
        Location newLoc = locationRepository.findByCode(command.targetLocationCode())
                .orElseThrow(() -> new LocationNotFoundException(command.targetLocationCode(), "Destino"));

        oldLoc.releaseLoad(item);
        newLoc.consolidateLoad(item);

        item.moveTo(command.targetLocationCode());

        locationRepository.save(oldLoc);
        locationRepository.save(newLoc);
        inventoryRepository.save(item);

        eventPublisher.publishEvent(new StockMovedEvent(
                item.getLpn().getValue(),
                item.getProductSku(),
                item.getQuantity(),
                oldLoc.getLocationCode(),
                newLoc.getLocationCode(),
                getCurrentUser(),
                "MOVIMIENTO",
                LocalDateTime.now()
        ));
    }

    @Override
    @Transactional
    public void processInventoryAdjustment(InventoryAdjustmentCommand command) {
        InventoryItem item = inventoryRepository.findByLpn(command.lpn())
                .orElseThrow(() -> new InventoryItemNotFoundException(command.lpn()));

        Location location = locationRepository.findByCode(item.getLocationCode())
                .orElseThrow(() -> new LocationNotFoundException(item.getLocationCode()));

        BigDecimal oldQty = item.getQuantity();
        BigDecimal newQty = command.newQuantity();

        if (oldQty == newQty) return;

        location.releaseLoad(item);

        if (newQty.compareTo(BigDecimal.ZERO) <= 0) {
            item.setQuantity(BigDecimal.valueOf(0.0));
            item.setStatus(InventoryStatus.SHIPPED);
        } else {
            item.setQuantity(newQty);
            location.consolidateLoad(item);
        }

        locationRepository.save(location);
        inventoryRepository.save(item);

        eventPublisher.publishEvent(new InventoryAdjustedEvent(
                item.getLpn().getValue(), item.getProductSku(), oldQty, newQty,
                command.reason(), location.getLocationCode(), getCurrentUser(), LocalDateTime.now()
        ));
    }

    private String getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : WmsConstants.SYSTEM_USER;
    }
}