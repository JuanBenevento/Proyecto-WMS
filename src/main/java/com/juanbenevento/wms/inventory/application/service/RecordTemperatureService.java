package com.juanbenevento.wms.inventory.application.service;

import com.juanbenevento.wms.inventory.application.RecordTemperatureCommand;
import com.juanbenevento.wms.inventory.application.port.out.LotRepository;
import com.juanbenevento.wms.inventory.domain.model.Lot;
import com.juanbenevento.wms.inventory.domain.model.LotStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Application service for recording temperature readings (cold chain monitoring).
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
@Slf4j
@Service
public class RecordTemperatureService {

    private final LotRepository lotRepository;

    public RecordTemperatureService(LotRepository lotRepository) {
        this.lotRepository = lotRepository;
    }

    public TemperatureResponse execute(RecordTemperatureCommand command) {
        log.info("Recording temperature for lot={}: {}°C at {}",
                command.lotNumber(), command.temperature(), command.location());

        Optional<Lot> lotOpt = lotRepository.findByLotNumber(command.lotNumber());
        if (lotOpt.isEmpty()) {
            return new TemperatureResponse(false, "Lot not found", false);
        }

        Lot lot = lotOpt.get();
        boolean withinRange = lot.isTemperatureWithinRange(command.temperature());

        if (!withinRange) {
            log.warn("TEMPERATURE ALERT: Lot {} at {}°C (range: {})",
                    command.lotNumber(),
                    command.temperature(),
                    lot.getTemperatureRange());
            // TODO: Trigger alert via configured channel (email/webhook)
            return new TemperatureResponse(
                    true,
                    "Temperature out of range - ALERT TRIGGERED",
                    false
            );
        }

        return new TemperatureResponse(
                true,
                "Temperature recorded within range",
                true
        );
    }

    public record TemperatureResponse(
            boolean recorded,
            String message,
            boolean withinRange
    ) {}
}