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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Weight Certification allocation strategy.
 * For port/export industries requiring certified weights per lot.
 *
 * <p>This strategy:
 * <ul>
 *   <li>Prioritizes lots with weight certificates</li>
 *   <li>Orders by FEFO (expiry) when weights are equal</li>
 *   <li>Requires weight recording at each movement</li>
 * </ul>
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
@Slf4j
@Component
public class WeightCertificationStrategy implements AllocationStrategy {

    private final LotRepository lotRepository;

    public WeightCertificationStrategy(LotRepository lotRepository) {
        this.lotRepository = lotRepository;
    }

    @Override
    public List<LotAllocation> selectLots(String productSku, BigDecimal quantity, AllocationContext context) {
        log.debug("WeightCert allocation for SKU={}, qty={}", productSku, quantity);

        List<Lot> activeLots = lotRepository.findActiveLotsForAllocation(productSku).stream()
                .filter(lot -> lot.getStatus() == LotStatus.ACTIVE)
                .filter(lot -> !lot.isExpired())
                .filter(lot -> lot.getGrossWeight() != null) // Weight cert required
                .sorted(weightCertificateComparator())
                .collect(Collectors.toList());

        List<LotAllocation> allocations = new ArrayList<>();
        BigDecimal remaining = quantity;

        for (Lot lot : activeLots) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal availableQty = getAvailableQuantity(lot);
            if (availableQty.compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal allocateQty = availableQty.min(remaining);
            allocations.add(LotAllocation.of(
                    lot.getLotNumber(),
                    allocateQty,
                    "WeightCert allocation, gross=" + lot.getGrossWeight()
            ));
            remaining = remaining.subtract(allocateQty);

            log.debug("WeightCert: allocated {} from lot {} (gross={})",
                    allocateQty, lot.getLotNumber(), lot.getGrossWeight());
        }

        return allocations;
    }

    @Override
    public String getStrategyName() {
        return "WEIGHT_CERT";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private Comparator<Lot> weightCertificateComparator() {
        return Comparator
                // First: prefer lots with weight certificates
                .comparing((Lot lot) -> lot.getGrossWeight() != null, Comparator.reverseOrder())
                // Second: FEFO order by expiry
                .thenComparing(lot -> lot.getExpiryDate() != null ? lot.getExpiryDate() :
                        java.time.LocalDate.MAX.plusYears(100));
    }

    private BigDecimal getAvailableQuantity(Lot lot) {
        // TODO: Integrate with InventoryItem
        return new BigDecimal("100");
    }
}