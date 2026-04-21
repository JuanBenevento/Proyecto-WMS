package com.juanbenevento.wms.inventory.application.service;

import com.juanbenevento.wms.inventory.application.TransferInventoryCommand;
import com.juanbenevento.wms.inventory.application.port.out.LotRepository;
import com.juanbenevento.wms.inventory.application.port.out.MovementRepository;
import com.juanbenevento.wms.inventory.domain.model.InventoryMovement;
import com.juanbenevento.wms.inventory.domain.model.Lot;
import com.juanbenevento.wms.inventory.domain.exception.ExpiredLotException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Application service for transferring inventory between locations.
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
@Slf4j
@Service
public class TransferInventoryService {

    private final LotRepository lotRepository;
    private final MovementRepository movementRepository;

    public TransferInventoryService(LotRepository lotRepository, MovementRepository movementRepository) {
        this.lotRepository = lotRepository;
        this.movementRepository = movementRepository;
    }

    @Transactional
    public TransferResponse execute(TransferInventoryCommand command) {
        log.info("Transfer: lot={}, qty={}, from={}, to={}",
                command.lotNumber(), command.quantity(),
                command.fromLocation(), command.toLocation());

        // Find lot
        Optional<Lot> lotOpt = lotRepository.findByLotNumber(command.lotNumber());
        if (lotOpt.isEmpty()) {
            return new TransferResponse(null, "Lot not found");
        }

        Lot lot = lotOpt.get();

        // Validate can be moved
        if (lot.getStatus() == com.juanbenevento.wms.inventory.domain.model.LotStatus.EXPIRED) {
            throw new ExpiredLotException(lot.getLotNumber(), lot.getExpiryDate());
        }

        // Create movement
        InventoryMovement movement = InventoryMovement.transfer(
                lot.getLotNumber(),
                command.quantity(),
                command.fromLocation(),
                command.toLocation(),
                command.performedBy()
        );

        movementRepository.save(movement);

        log.info("Transfer completed: movementId={}", movement.getMovementId());

        return new TransferResponse(movement.getMovementId(), "Transferred successfully");
    }

    public record TransferResponse(java.util.UUID movementId, String message) {}
}