package com.juanbenevento.wms.orders.infrastructure.event.bridge;

import com.juanbenevento.wms.inventory.domain.event.StockAssignedEvent;
import com.juanbenevento.wms.inventory.domain.event.StockPickingCompletedEvent;
import com.juanbenevento.wms.inventory.domain.event.StockPickingStartedEvent;
import com.juanbenevento.wms.inventory.domain.event.StockShortageEvent;
import com.juanbenevento.wms.orders.application.service.InventoryEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Bridge que conecta los eventos de Inventory (Spring ApplicationEventPublisher)
 * con el EventBus de Orders (InMemoryEventBus con retry/DLQ).
 * 
 * Este componente es el pegamento entre los dos módulos.
 * Escucha los eventos publicados por Inventory y los traduce al formato
 * que espera el InventoryEventHandler.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventBridge {

    private final InventoryEventHandler inventoryEventHandler;

    // ==================== STOCK EVENTS ====================

    /**
     * Traduce StockAssignedEvent de Inventory a una llamada directa al handler.
     */
    @EventListener
    @Async
    public void onStockAssigned(StockAssignedEvent event) {
        log.info("Bridge: Recibido StockAssignedEvent para orden {}", event.orderId());
        
        try {
            InventoryEventHandler.StockAssignedEvent translated = 
                new InventoryEventHandler.StockAssignedEvent(
                    event.orderId(),
                    event.orderNumber(),
                    java.time.Instant.now(),
                    event.lines().stream()
                        .map(l -> new InventoryEventHandler.StockAssignedEvent.LineAssignment(
                            l.lineId(),
                            l.sku(),
                            l.allocatedQuantity(),
                            l.inventoryItemId(),
                            l.locationCode()
                        ))
                        .toList()
                );
            
            inventoryEventHandler.onStockAssigned(translated);
            
        } catch (Exception e) {
            log.error("Error procesando StockAssignedEvent en bridge: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Traduce StockShortageEvent de Inventory a una llamada directa al handler.
     */
    @EventListener
    @Async
    public void onStockShortage(StockShortageEvent event) {
        log.info("Bridge: Recibido StockShortageEvent para orden {}", event.orderId());
        
        try {
            InventoryEventHandler.StockShortageEvent translated = 
                new InventoryEventHandler.StockShortageEvent(
                    event.orderId(),
                    event.orderNumber(),
                    java.time.Instant.now(),
                    com.juanbenevento.wms.orders.domain.model.StatusReason.INVENTORY_SHORTAGE,
                    event.shortages().stream()
                        .map(s -> new InventoryEventHandler.StockShortageEvent.LineShortage(
                            s.lineId(),
                            s.sku(),
                            s.requestedQuantity(),
                            s.allocatedQuantity()
                        ))
                        .toList()
                );
            
            inventoryEventHandler.onStockShortage(translated);
            
        } catch (Exception e) {
            log.error("Error procesando StockShortageEvent en bridge: {}", e.getMessage(), e);
            throw e;
        }
    }

    // ==================== PICKING EVENTS ====================

    /**
     * Traduce PickingStartedEvent de Inventory.
     */
    @EventListener
    @Async
    public void onPickingStarted(StockPickingStartedEvent event) {
        log.info("Bridge: Recibido PickingStartedEvent para orden {}", event.orderId());
        
        try {
            InventoryEventHandler.PickingStartedEvent translated = 
                new InventoryEventHandler.PickingStartedEvent(
                    event.orderId(),
                    event.orderNumber(),
                    java.time.Instant.now(),
                    event.startedBy(),
                    event.assignedLocations()
                );
            
            inventoryEventHandler.onPickingStarted(translated);
            
        } catch (Exception e) {
            log.error("Error procesando PickingStartedEvent en bridge: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Traduce PickingCompletedEvent de Inventory.
     */
    @EventListener
    @Async
    public void onPickingCompleted(StockPickingCompletedEvent event) {
        log.info("Bridge: Recibido PickingCompletedEvent para orden {}", event.orderId());
        
        try {
            InventoryEventHandler.PickingCompletedEvent translated = 
                new InventoryEventHandler.PickingCompletedEvent(
                    event.orderId(),
                    event.orderNumber(),
                    java.time.Instant.now(),
                    event.completedBy(),
                    event.pickedLines().stream()
                        .map(p -> new InventoryEventHandler.PickingCompletedEvent.PickedLine(
                            p.lineId(),
                            p.pickedQuantity(),
                            p.wasShort()
                        ))
                        .toList()
                );
            
            inventoryEventHandler.onPickingCompleted(translated);
            
        } catch (Exception e) {
            log.error("Error procesando PickingCompletedEvent en bridge: {}", e.getMessage(), e);
            throw e;
        }
    }
}
