package com.juanbenevento.wms.orders.application.service;

import com.juanbenevento.wms.orders.application.port.out.OrderRepositoryPort;
import com.juanbenevento.wms.orders.domain.event.OrderStatusChangedEvent;
import com.juanbenevento.wms.orders.domain.model.Order;
import com.juanbenevento.wms.orders.domain.model.OrderStatus;
import com.juanbenevento.wms.orders.domain.model.StatusReason;
import com.juanbenevento.wms.orders.infrastructure.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Handler que procesa eventos ENTRANTES desde Inventory.
 * 
 * Este componente se suscribe a los eventos que Inventory publica
 * y actualiza el estado de las órdenes correspondientemente.
 * 
 * Eventos que maneja:
 * - StockAssignedEvent → Order pasa a ALLOCATED
 * - StockShortageEvent → Order pasa a HOLD con razón INVENTORY_SHORTAGE
 * - PickingStartedEvent → Order pasa a PICKING
 * - PickingCompletedEvent → Order pasa a PACKED
 */
@Component
public class InventoryEventHandler {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventHandler.class);

    private final OrderRepositoryPort orderRepository;
    private final EventBus eventBus;

    public InventoryEventHandler(OrderRepositoryPort orderRepository, EventBus eventBus) {
        this.orderRepository = orderRepository;
        this.eventBus = eventBus;
    }

    /**
     * Suscribe los handlers a los eventos de Inventory al iniciar la aplicación.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void subscribeToEvents() {
        log.info("Suscribiendo InventoryEventHandler a eventos...");

        // NOTA: Cuando se implemente Kafka/RabbitMQ, estos eventos vendrán
        // del broker en lugar del EventBus local.
        // Por ahora, otros servicios pueden publicar directamente a este handler.

        log.info("InventoryEventHandler listo para recibir eventos");
    }

    /**
     * Procesa evento de stock asignado.
     * Called cuando Inventory asigna stock a las líneas de la orden.
     */
    public void onStockAssigned(StockAssignedEvent event) {
        log.info("Procesando StockAssigned para orden: {} [lineas: {}]", 
                event.orderId(), event.lines().size());

        try {
            Order order = orderRepository.findById(event.orderId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Orden no encontrada: " + event.orderId()));

            // Actualizar cada línea con la información de asignación
            for (StockAssignedEvent.LineAssignment line : event.lines()) {
                order.assignStockToLine(
                        line.lineId(),
                        line.allocatedQuantity(),
                        line.inventoryItemId(),
                        line.locationCode()
                );
            }

            // Persistir
            orderRepository.save(order);

            log.info("Stock asignado exitosamente a orden: {}", event.orderId());

        } catch (Exception e) {
            log.error("Error asignando stock a orden {}: {}", event.orderId(), e.getMessage(), e);
            throw e; // El EventBus hará retry/DLQ
        }
    }

    /**
     * Procesa evento de faltante de stock.
     * Called cuando Inventory detecta que no hay suficiente stock.
     */
    public void onStockShortage(StockShortageEvent event) {
        log.info("Procesando StockShortage para orden: {} [razón: {}]", 
                event.orderId(), event.reason());

        try {
            Order order = orderRepository.findById(event.orderId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Orden no encontrada: " + event.orderId()));

            // Actualizar líneas con stock parcial si hay
            for (StockShortageEvent.LineShortage line : event.shortages()) {
                order.reportShortageForLine(line.lineId(), line.allocatedQuantity());
            }

            // Poner en espera solo si no está ya en HOLD
            if (order.getStatus() != OrderStatus.HOLD) {
                order.hold(StatusReason.INVENTORY_SHORTAGE);
            }

            // Persistir
            orderRepository.save(order);

            log.info("Orden {} puesta en HOLD por faltante de stock", event.orderId());

        } catch (Exception e) {
            log.error("Error procesando faltante para orden {}: {}", event.orderId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Procesa evento de inicio de picking.
     */
    public void onPickingStarted(PickingStartedEvent event) {
        log.info("Picking iniciado para orden: {}", event.orderId());

        try {
            Order order = orderRepository.findById(event.orderId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Orden no encontrada: " + event.orderId()));

            order.startPicking();
            orderRepository.save(order);

            log.info("Orden {} transicionada a PICKING", event.orderId());

        } catch (Exception e) {
            log.error("Error iniciando picking para orden {}: {}", event.orderId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Procesa evento de completación de picking.
     */
    public void onPickingCompleted(PickingCompletedEvent event) {
        log.info("Picking completado para orden: {}", event.orderId());

        try {
            Order order = orderRepository.findById(event.orderId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Orden no encontrada: " + event.orderId()));

            order.pack();
            orderRepository.save(order);

            log.info("Orden {} transicionada a PACKED", event.orderId());

        } catch (Exception e) {
            log.error("Error completando picking para orden {}: {}", event.orderId(), e.getMessage(), e);
            throw e;
        }
    }

    // ==================== EVENT CLASSES ====================
    // Estas clases representan los eventos que vienen de Inventory
    // En un sistema real vendrían de Kafka/RabbitMQ

    /**
     * Evento recibido cuando Inventory asigna stock a líneas de la orden.
     */
    public record StockAssignedEvent(
            String orderId,
            String orderNumber,
            java.time.Instant occurredAt,
            java.util.List<LineAssignment> lines
    ) {
        public record LineAssignment(
                String lineId,
                String productSku,
                java.math.BigDecimal allocatedQuantity,
                String inventoryItemId,  // LPN
                String locationCode
        ) {}
    }

    /**
     * Evento recibido cuando hay faltante de stock.
     */
    public record StockShortageEvent(
            String orderId,
            String orderNumber,
            java.time.Instant occurredAt,
            StatusReason reason,
            java.util.List<LineShortage> shortages
    ) {
        public record LineShortage(
                String lineId,
                String productSku,
                java.math.BigDecimal requestedQuantity,
                java.math.BigDecimal allocatedQuantity  // Puede ser 0 o parcial
        ) {}
    }

    /**
     * Evento recibido cuando PickingService inicia el picking.
     */
    public record PickingStartedEvent(
            String orderId,
            String orderNumber,
            java.time.Instant occurredAt,
            String startedBy,
            java.util.List<String> assignedLocations
    ) {}

    /**
     * Evento recibido cuando PickingService completa el picking.
     */
    public record PickingCompletedEvent(
            String orderId,
            String orderNumber,
            java.time.Instant occurredAt,
            String completedBy,
            java.util.List<PickedLine> pickedLines
    ) {
        public record PickedLine(
                String lineId,
                java.math.BigDecimal pickedQuantity,
                boolean wasShort
        ) {}
    }
}
