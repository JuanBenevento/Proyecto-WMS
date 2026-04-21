package com.juanbenevento.wms.inventory.application.service;

import com.juanbenevento.wms.inventory.application.port.out.LotRepository;
import com.juanbenevento.wms.inventory.domain.model.Lot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service for querying lots expiring within N days.
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
@Slf4j
@Service
public class QueryExpiringLotsService {

    private final LotRepository lotRepository;

    public QueryExpiringLotsService(LotRepository lotRepository) {
        this.lotRepository = lotRepository;
    }

    public ExpiringLotsResponse execute(int daysThreshold, String productSku) {
        log.debug("Querying lots expiring within {} days, SKU={}", daysThreshold, productSku);

        List<Lot> expiringLots;

        if (productSku != null && !productSku.isBlank()) {
            // Filter by product SKU
            expiringLots = lotRepository.findByProductSku(productSku).stream()
                    .filter(lot -> lot.getExpiryDate() != null)
                    .filter(lot -> lot.getDaysUntilExpiry() <= daysThreshold)
                    .filter(lot -> lot.getStatus() == com.juanbenevento.wms.inventory.domain.model.LotStatus.ACTIVE)
                    .sorted((a, b) -> Long.compare(a.getDaysUntilExpiry(), b.getDaysUntilExpiry()))
                    .toList();
        } else {
            expiringLots = lotRepository.findExpiringWithin(daysThreshold);
        }

        return new ExpiringLotsResponse(expiringLots, expiringLots.size());
    }

    public record ExpiringLotsResponse(List<Lot> lots, int totalCount) {}
}