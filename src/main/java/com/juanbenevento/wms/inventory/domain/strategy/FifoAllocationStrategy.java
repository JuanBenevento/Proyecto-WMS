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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * First In First Out (FIFO) allocation strategy.
 * Prioritizes oldest production date for metalworking/manufacturing.
 *
 * <p>This strategy:
 * <ul>
 *   <li>Orders lots by productionDate ASC (oldest first)</li>
 *   <li>Excludes EXPIRED, EXHAUSTED lots (no quarantine for non-food)</li>
 *   <li>No expiry date requirement</li>
 * </ul>
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
@Slf4j
@Component
public class FifoAllocationStrategy implements AllocationStrategy {

    private final LotRepository lotRepository;

    public FifoAllocationStrategy(LotRepository lotRepository) {
        this.lotRepository = lotRepository;
    }

    @Override
    public List<LotAllocation> selectLots(String productSku, BigDecimal quantity, AllocationContext context) {
        log.debug("FIFO allocation for SKU={}, qty={}", productSku, quantity);

        List<Lot> activeLots = lotRepository.findActiveLotsForAllocation(productSku).stream()
                .filter(lot -> lot.getStatus() == LotStatus.ACTIVE || lot.getStatus() == LotStatus.QUARANTINE) // Allow quarantine for metalworking
                .filter(lot -> !lot.isExpired())
                .sorted(Comparator.comparing(Lot::getProductionDate))
                .collect(Collectors.toList());

        var allocations = new java.util.ArrayList<LotAllocation>();
        BigDecimal remaining = quantity;

        for (Lot lot : activeLots) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal availableQty = getAvailableQuantity(lot);
            if (availableQty.compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal allocateQty = availableQty.min(remaining);
            allocations.add(LotAllocation.of(lot.getLotNumber(), allocateQty, "FIFO allocation"));
            remaining = remaining.subtract(allocateQty);
        }

        return allocations;
    }

    @Override
    public String getStrategyName() {
        return "FIFO";
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