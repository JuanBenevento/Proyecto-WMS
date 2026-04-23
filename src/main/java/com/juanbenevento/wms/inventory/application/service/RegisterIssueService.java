package com.juanbenevento.wms.inventory.application.service;

import com.juanbenevento.wms.inventory.application.RegisterIssueCommand;
import com.juanbenevento.wms.inventory.application.port.out.LotRepository;
import com.juanbenevento.wms.inventory.domain.model.AllocationContext;
import com.juanbenevento.wms.inventory.domain.model.InventoryMovement;
import com.juanbenevento.wms.inventory.domain.model.LotAllocation;
import com.juanbenevento.wms.inventory.domain.model.LotStatus;
import com.juanbenevento.wms.inventory.domain.strategy.FefoAllocationStrategy;
import com.juanbenevento.wms.inventory.domain.strategy.FifoAllocationStrategy;
import com.juanbenevento.wms.inventory.domain.strategy.LotControlStrategy;
import com.juanbenevento.wms.inventory.domain.strategy.WeightCertificationStrategy;
import com.juanbenevento.wms.inventory.application.port.in.AllocationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Application service for registering inventory issues (picking/shipment).
 *
 * @author WMS Architecture Team
 * @since 1.0
 */
@Slf4j
@Service
public class RegisterIssueService {

    private final Map<String, AllocationStrategy> strategies;
    private final LotRepository lotRepository;

    public RegisterIssueService(
            FefoAllocationStrategy fefoStrategy,
            FifoAllocationStrategy fifoStrategy,
            LotControlStrategy lotControlStrategy,
            WeightCertificationStrategy weightCertStrategy,
            LotRepository lotRepository
    ) {
        this.strategies = Map.of(
                "FEFO", fefoStrategy,
                "FIFO", fifoStrategy,
                "LOT_CONTROL", lotControlStrategy,
                "WEIGHT_CERT", weightCertStrategy
        );
        this.lotRepository = lotRepository;
    }

    @Transactional
    public IssueResponse execute(RegisterIssueCommand command) {
        log.info("Registering issue for SKU={}, qty={}, strategy={}",
                command.productSku(), command.quantity(), command.allocationStrategy());

        // Select strategy
        AllocationStrategy strategy = strategies.getOrDefault(
                command.allocationStrategy(), strategies.get("FEFO"));

        // Build context
        AllocationContext context = AllocationContext.of(command.quantity()).builder()
                .preferredLocation(null)
                .includeLots(command.preferredLots() != null ? command.preferredLots() : List.of())
                .build();

        // Allocate lots
        List<LotAllocation> allocations = strategy.selectLots(
                command.productSku(), command.quantity(), context);

        if (allocations.isEmpty()) {
            log.warn("No lots available for SKU={}", command.productSku());
            return new IssueResponse(List.of(), null, "No lots available");
        }

        // Create movement for each allocation
        for (LotAllocation allocation : allocations) {
            InventoryMovement movement = InventoryMovement.issue(
                    allocation.lotNumber(),
                    allocation.quantity(),
                    null, // from location - comes from inventory item
                    command.reason(),
                    command.performedBy()
            );
            // Save movement
            log.debug("Movement created: lot={}, qty={}", allocation.lotNumber(), allocation.quantity());
        }

        return new IssueResponse(
                allocations,
                java.util.UUID.randomUUID(),
                "Issued successfully"
        );
    }

    /**
     * Response record for issue registration.
     */
    public record IssueResponse(
            List<LotAllocation> allocations,
            java.util.UUID movementId,
            String message
    ) {}
}