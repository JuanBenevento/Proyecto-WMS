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
 * First Expired First Out (FEFO) allocation strategy.
 * Prioritizes lots expiring soonest for food/frutihortícola industry.
 *
 * <p>This strategy:
 * <ul>
 *   <li>Orders lots by expiryDate ASC (soonest first)</li>
 *   <li>Excludes EXPIRED, EXHAUSTED, QUARANTINE lots</li>
 *   <li>Allocates from multiple lots if needed</li>
 * </ul>
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
@Slf4j
@Component
public class FefoAllocationStrategy implements AllocationStrategy {

    private final LotRepository lotRepository;

    public FefoAllocationStrategy(LotRepository lotRepository) {
        this.lotRepository = lotRepository;
    }

    @Override
    public List<LotAllocation> selectLots(String productSku, BigDecimal quantity, AllocationContext context) {
        log.debug("FEFO allocation for SKU={}, qty={}", productSku, quantity);

        List<Lot> activeLots = lotRepository.findActiveLotsForAllocation(productSku).stream()
                .filter(lot -> lot.getStatus() == LotStatus.ACTIVE)
                .filter(lot -> !lot.isExpired())
                .filter(lot -> lot.getExpiryDate() != null) // FEFO requires expiry dates
                .sorted(Comparator.comparing(Lot::getExpiryDate))
                .collect(Collectors.toList());

        return allocateFromLots(activeLots, quantity, context);
    }

    @Override
    public String getStrategyName() {
        return "FEFO";
    }

    @Override
    public boolean isEnabled() {
        return true; // Configurable via properties in real implementation
    }

    private List<LotAllocation> allocateFromLots(List<Lot> lots, BigDecimal needed, AllocationContext context) {
        var allocations = new java.util.ArrayList<LotAllocation>();
        BigDecimal remaining = needed;

        for (Lot lot : lots) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            // Check exclusions
            if (context.getExcludeLots() != null && context.getExcludeLots().contains(lot.getLotNumber())) {
                continue;
            }

            // Check inclusions (if specified)
            if (context.getIncludeLots() != null && !context.getIncludeLots().isEmpty()) {
                if (!context.getIncludeLots().contains(lot.getLotNumber())) {
                    continue;
                }
            }

            // Placeholder: actual quantity comes from InventoryItem linked to lot
            // For now, allocate based on available quantity
            BigDecimal availableQty = getAvailableQuantity(lot);
            if (availableQty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal allocateQty = availableQty.min(remaining);

            allocations.add(LotAllocation.of(lot.getLotNumber(), allocateQty, "FEFO allocation"));

            remaining = remaining.subtract(allocateQty);
            log.debug("Allocated {} from lot {}, remaining={}", allocateQty, lot.getLotNumber(), remaining);
        }

        return allocations;
    }

    private BigDecimal getAvailableQuantity(Lot lot) {
        // TODO: Integrate with InventoryItem to get actual quantity
        // For now, return a placeholder
        return new BigDecimal("100");
    }
}