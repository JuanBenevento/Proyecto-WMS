package com.juanbenevento.wms.inventory.application.service;

import com.juanbenevento.wms.inventory.application.port.out.LotRepository;
import com.juanbenevento.wms.inventory.application.port.out.MovementRepository;
import com.juanbenevento.wms.inventory.domain.model.InventoryMovement;
import com.juanbenevento.wms.inventory.domain.model.Lot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Application service for querying lot history (traceability).
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
@Slf4j
@Service
public class QueryLotHistoryService {

    private final LotRepository lotRepository;
    private final MovementRepository movementRepository;

    public QueryLotHistoryService(LotRepository lotRepository, MovementRepository movementRepository) {
        this.lotRepository = lotRepository;
        this.movementRepository = movementRepository;
    }

    public LotHistoryResponse execute(String lotNumber) {
        log.debug("Querying lot history for lotNumber={}", lotNumber);

        Optional<Lot> lotOpt = lotRepository.findByLotNumber(lotNumber);
        if (lotOpt.isEmpty()) {
            return new LotHistoryResponse(null, null, List.of());
        }

        Lot lot = lotOpt.get();
        List<InventoryMovement> movements = movementRepository.findByLotNumber(lotNumber);

        return new LotHistoryResponse(
                lot.getLotNumber(),
                lot,
                movements
        );
    }

    public record LotHistoryResponse(
            String lotNumber,
            Lot lot,
            List<InventoryMovement> movements
    ) {}
}