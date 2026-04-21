package com.juanbenevento.wms.inventory.application.port.in;

import com.juanbenevento.wms.inventory.domain.model.AllocationContext;
import com.juanbenevento.wms.inventory.domain.model.LotAllocation;

import java.util.List;

/**
 * Port (interface) for pluggable lot allocation strategies.
 * Each company enables the strategies it needs. Core WMS has NO industry-specific logic.
 *
 * <p>Implementations:</p>
 * <ul>
 *   <li>{@link com.juanbenevento.wms.inventory.domain.strategy.FefoAllocationStrategy} - Food (frutihortícola)</li>
 *   <li>{@link com.juanbenevento.wms.inventory.domain.strategy.FifoAllocationStrategy} - Metalworking</li>
 *   <li>{@link com.juanbenevento.wms.inventory.domain.strategy.LotControlStrategy} - Pharmaceutical</li>
 *   <li>{@link com.juanbenevento.wms.inventory.domain.strategy.WeightCertificationStrategy} - Port/Export</li>
 * </ul>
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
public interface AllocationStrategy {

    /**
     * Selects lots to fulfill a picking request based on the strategy logic.
     *
     * @param productSku the product to allocate
     * @param quantity the quantity needed
     * @param context allocation context with filters and preferences
     * @return list of lot allocations ordered by priority
     */
    List<LotAllocation> selectLots(String productSku, java.math.BigDecimal quantity, AllocationContext context);

    /**
     * Returns the unique name of this strategy.
     * Used for configuration and logging.
     *
     * @return strategy name (e.g., "FEFO", "FIFO", "LOT_CONTROL")
     */
    String getStrategyName();

    /**
     * Checks if this strategy is currently enabled for the tenant.
     *
     * @return true if enabled
     */
    boolean isEnabled();
}