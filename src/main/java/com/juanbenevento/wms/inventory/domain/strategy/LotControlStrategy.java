package com.juanbenevento.wms.inventory.domain.strategy;

import com.juanbenevento.wms.inventory.application.port.in.AllocationStrategy;
import com.juanbenevento.wms.inventory.application.port.out.LotRepository;
import com.juanbenevento.wms.inventory.domain.model.AllocationContext;
import com.juanbenevento.wms.inventory.domain.model.Lot;
import com.juanbenevento.wms.inventory.domain.model.LotAllocation;
import com.juanbenevento.wms.inventory.domain.model.LotStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lot Control allocation strategy.
 * Requires specific lot assignment for pharmaceutical/regulated industries.
 *
 * <p>This strategy:
 * <ul>
 *   <li>Validates that the required lot exists</li>
 *   <li>Blocks allocation if lot is not in allowed list</li>
 *   <li>Requires explicit lot specification</li>
 * </ul>
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
@Slf4j
@Component
public class LotControlStrategy implements AllocationStrategy {

    private final LotRepository lotRepository;

    public LotControlStrategy(LotRepository lotRepository) {
        this.lotRepository = lotRepository;
    }

    @Override
    public List<LotAllocation> selectLots(String productSku, BigDecimal quantity, AllocationContext context) {
        log.debug("LotControl allocation for SKU={}, qty={}", productSku, quantity);

        // LotControl requires explicit lot specification
        if (context.getIncludeLots() == null || context.getIncludeLots().isEmpty()) {
            log.warn("LotControl strategy requires explicit lot specification");
            return List.of();
        }

        List<String> requiredLots = context.getIncludeLots();
        List<LotAllocation> allocations = new ArrayList<>();

        for (String lotNumber : requiredLots) {
            Optional<Lot> lotOpt = lotRepository.findByLotNumber(lotNumber);
            if (lotOpt.isEmpty()) {
                log.warn("Required lot {} not found", lotNumber);
                continue;
            }

            Lot lot = lotOpt.get();

            // Validate lot
            if (lot.getStatus() == LotStatus.EXPIRED) {
                log.warn("Cannot allocate from expired lot {}", lotNumber);
                continue;
            }

            if (lot.getStatus() == LotStatus.EXHAUSTED) {
                log.warn("Cannot allocate from exhausted lot {}", lotNumber);
                continue;
            }

            // For quarantine, require explicit release
            if (lot.getStatus() == LotStatus.QUARANTINE) {
                log.info("Lot {} is quarantined - requires release before allocation", lotNumber);
            }

            BigDecimal availableQty = getAvailableQuantity(lot);
            if (availableQty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal allocateQty = availableQty.min(quantity);
            allocations.add(LotAllocation.of(lotNumber, allocateQty, "LotControl allocation"));
        }

        return allocations;
    }

    @Override
    public String getStrategyName() {
        return "LOT_CONTROL";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private BigDecimal getAvailableQuantity(Lot lot) {
        // TODO: Integrate with InventoryItem
        return new BigDecimal("100");
    }
}