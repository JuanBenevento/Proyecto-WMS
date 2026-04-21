package com.juanbenevento.wms.inventory.application;

import com.juanbenevento.wms.inventory.application.port.out.LotRepository;
import com.juanbenevento.wms.inventory.application.port.out.MovementRepository;
import com.juanbenevento.wms.inventory.domain.model.InventoryMovement;
import com.juanbenevento.wms.inventory.domain.model.Lot;
import com.juanbenevento.wms.inventory.domain.model.TemperatureRange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Application service for registering inventory receipts.
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
@Slf4j
@Service
public class RegisterReceiptService {

    private final LotRepository lotRepository;
    private final MovementRepository movementRepository;

    public RegisterReceiptService(LotRepository lotRepository, MovementRepository movementRepository) {
        this.lotRepository = lotRepository;
        this.movementRepository = movementRepository;
    }

    /**
     * Registers a receipt of goods, creating a Lot and initial movement.
     *
     * @param command receipt command
     * @return receipt response
     */
    @Transactional
    public ReceiptResponse execute(RegisterReceiptCommand command) {
        log.info("Registering receipt for SKU={}, qty={}, location={}",
                command.productSku(), command.quantity(), command.locationCode());

        // 1. Create or find Lot
        Lot lot = createLot(command);

        // 2. Save Lot
        Lot savedLot = lotRepository.save(lot);

        // 3. Create movement record
        InventoryMovement movement = InventoryMovement.receive(
                savedLot.getLotNumber(),
                command.quantity(),
                command.locationCode(),
                command.performedBy()
        ).withTemperature(command.temperatureAtReceipt())
         .withCertificate(command.certificateUrl());

        movementRepository.save(movement);

        log.info("Receipt registered: lot={}, movementId={}",
                savedLot.getLotNumber(), movement.getMovementId());

        return new ReceiptResponse(
                savedLot.getLotNumber(),
                command.productSku(),
                command.quantity(),
                command.locationCode(),
                LocalDateTime.now(),
                command.performedBy()
        );
    }

    private Lot createLot(RegisterReceiptCommand command) {
        TemperatureRange temperatureRange = null;
        if (command.minTemperature() != null && command.maxTemperature() != null) {
            temperatureRange = TemperatureRange.of(command.minTemperature(), command.maxTemperature());
        }

        return Lot.create(
                command.lotNumber(),
                command.productSku(),
                command.batchNumber(),
                command.productionDate(),
                command.origin(),
                command.expiryDate(),
                temperatureRange,
                command.netWeight(),
                command.grossWeight(),
                command.metadata() != null ? command.metadata() : java.util.Map.of()
        );
    }
}