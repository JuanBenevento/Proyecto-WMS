package com.juanbenevento.wms.inventory.application.service;

import com.juanbenevento.wms.inventory.application.port.in.command.AllocateStockCommand;
import com.juanbenevento.wms.inventory.application.port.in.command.CompletePickingCommand;
import com.juanbenevento.wms.inventory.application.port.in.command.PickLineCommand;
import com.juanbenevento.wms.inventory.application.port.in.command.StartPickingCommand;
import com.juanbenevento.wms.inventory.application.port.in.command.StartPickingCommand.ShortPickDecision;
import com.juanbenevento.wms.inventory.application.port.in.usecases.AllocateStockUseCase;
import com.juanbenevento.wms.inventory.application.port.out.InventoryRepositoryPort;
import com.juanbenevento.wms.inventory.application.port.out.PickingOrderPort;
import com.juanbenevento.wms.inventory.application.port.out.PickingOrderPort.OrderLineForPicking;
import com.juanbenevento.wms.inventory.domain.event.PickingCompletedEvent;
import com.juanbenevento.wms.inventory.domain.event.PickingStartedEvent;
import com.juanbenevento.wms.inventory.domain.model.InventoryItem;
import com.juanbenevento.wms.inventory.domain.model.InventoryStatus;
import com.juanbenevento.wms.inventory.domain.model.PickingSession;
import com.juanbenevento.wms.inventory.domain.model.PickingSession.PickingLine;
import com.juanbenevento.wms.warehouse.application.port.out.LocationRepositoryPort;
import com.juanbenevento.wms.inventory.domain.event.StockReservedEvent;
import com.juanbenevento.wms.shared.domain.exception.DomainException;
import com.juanbenevento.wms.warehouse.domain.model.Location;
import com.juanbenevento.wms.shared.domain.valueobject.Lpn;
import com.juanbenevento.wms.shared.domain.valueobject.WmsConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de picking que maneja el flujo completo de picking de órdenes.
 * 
 * Flujo:
 * 1. startPicking() - Inicia sesión de picking, publica PickingStartedEvent
 * 2. pickLine() - Registra el pick de cada línea (puede ser short)
 * 3. completePicking() - Finaliza la sesión, publica PickingCompletedEvent
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PickingService implements AllocateStockUseCase {

    private final InventoryRepositoryPort inventoryRepository;
    private final LocationRepositoryPort locationRepository;
    private final PickingOrderPort pickingOrderPort;
    private final ApplicationEventPublisher eventPublisher;

    // Sesiones de picking activas (en producción usar Redis o similar)
    private final Map<String, PickingSession> activeSessions = new ConcurrentHashMap<>();

    // ==================== PICKING OPERATIONS ====================

    /**
     * Inicia el proceso de picking para una orden.
     * 
     * Valida que la orden existe y tiene líneas para pickear.
     * Crea una sesión de picking y publica PickingStartedEvent.
     */
    @Transactional
    public PickingSession startPicking(StartPickingCommand command) {
        log.info("Iniciando picking para orden: {} con decisión: {}", 
                command.orderId(), command.shortPickDecision());

        // Obtener información de la orden
        PickingOrderPort.PickingOrderInfo orderInfo = pickingOrderPort.getPickingOrderInfo(command.orderId());
        if (orderInfo == null) {
            throw new DomainException("Orden no encontrada: " + command.orderId());
        }

        // Obtener líneas de la orden
        List<OrderLineForPicking> orderLines = pickingOrderPort.getOrderLinesForPicking(command.orderId());
        if (orderLines.isEmpty()) {
            throw new DomainException("La orden no tiene líneas para pickear");
        }

        // Crear sesión de picking
        List<PickingSession.PickingLineInfo> pickingLines = orderLines.stream()
                .map(line -> new PickingSession.PickingLineInfo(
                        line.lineId(),
                        line.sku(),
                        line.allocatedQuantity(),
                        line.locationCode(),
                        line.inventoryItemId()
                ))
                .toList();

        PickingSession session = PickingSession.create(
                command.orderId(),
                orderInfo.orderNumber(),
                command.shortPickDecision(),
                pickingLines,
                command.assignedOperator() != null ? command.assignedOperator() : getCurrentUser()
        );

        // Guardar sesión activa
        activeSessions.put(session.getSessionId(), session);

        // Actualizar estado de inventory items a PICKING
        for (OrderLineForPicking line : orderLines) {
            updateInventoryItemStatus(line.inventoryItemId(), InventoryStatus.PICKING);
        }

        // Publicar evento
        eventPublisher.publishEvent(new PickingStartedEvent(
                command.orderId(),
                orderInfo.orderNumber(),
                orderLines.stream().map(OrderLineForPicking::lineId).toList(),
                orderLines.stream().map(OrderLineForPicking::locationCode).toList(),
                getCurrentUser(),
                LocalDateTime.now()
        ));

        log.info("Sesión de picking {} iniciada para orden {}", session.getSessionId(), command.orderId());
        return session;
    }

    /**
     * Registra el pick de una línea individual.
     * 
     * Puede manejar short picks según la configuración de la sesión.
     */
    @Transactional
    public PickingSession pickLine(PickLineCommand command) {
        log.info("Registrando pick para línea {} de orden {}: {} unidades", 
                command.lineId(), command.orderId(), command.pickedQuantity());

        // Buscar sesión activa por orderId
        PickingSession session = findSessionByOrderId(command.orderId());
        if (session == null) {
            throw new DomainException("No hay sesión de picking activa para la orden: " + command.orderId());
        }

        // Verificar si es un short pick
        PickingSession.PickingLine sessionLine = session.getLines().stream()
                .filter(l -> l.getLineId().equals(command.lineId()))
                .findFirst()
                .orElseThrow(() -> new DomainException("Línea no encontrada en sesión: " + command.lineId()));

        boolean wasShort = command.pickedQuantity()
                .compareTo(sessionLine.getAllocatedQuantity()) < 0;

        // Manejar short pick según decisión configurada
        if (wasShort) {
            handleShortPick(session, sessionLine, command.pickedQuantity(), 
                    command.pickedBy(), session.getShortPickDecision());
        }

        // Registrar pick
        session.pickLine(command.lineId(), command.pickedQuantity(), wasShort, command.notes());

        // Actualizar inventory item si es necesario
        if (sessionLine.getInventoryItemId() != null) {
            InventoryItem item = inventoryRepository.findByInventoryItemId(sessionLine.getInventoryItemId());
            if (item != null) {
                updateInventoryForPick(item, command.pickedQuantity());
            }
        }

        log.info("Pick registrado para línea {}. Short: {}", command.lineId(), wasShort);
        return session;
    }

    /**
     * Completa el proceso de picking para una orden.
     * 
     * Publica PickingCompletedEvent con información de short picks.
     */
    @Transactional
    public PickingSession completePicking(CompletePickingCommand command) {
        log.info("Completando picking para orden: {}", command.orderId());

        PickingSession session = findSessionByOrderId(command.orderId());
        if (session == null) {
            throw new DomainException("No hay sesión de picking activa para la orden: " + command.orderId());
        }

        // Procesar resultados de líneas
        for (CompletePickingCommand.PickResult result : command.lines()) {
            PickingSession.PickingLine line = session.getLines().stream()
                    .filter(l -> l.getLineId().equals(result.lineId()))
                    .findFirst()
                    .orElse(null);

            if (line != null && !line.isPicked()) {
                session.pickLine(result.lineId(), result.pickedQuantity(), 
                        result.wasShort(), result.notes());
            }
        }

        // Verificar short picks según decisión
        if (session.hasShortPicks()) {
            handleSessionShortPicks(session, command.completedBy());
        }

        // Completar sesión
        session.complete();
        activeSessions.remove(session.getSessionId());

        // Actualizar inventory items a PACKED
        for (PickingSession.PickingLine line : session.getLines()) {
            if (line.getInventoryItemId() != null) {
                updateInventoryItemStatus(line.getInventoryItemId(), InventoryStatus.PACKED);
            }
        }

        // Publicar evento
        List<com.juanbenevento.wms.inventory.domain.event.PickingCompletedEvent.PickedLine> pickedLines = session.getLines().stream()
                .map(line -> new com.juanbenevento.wms.inventory.domain.event.PickingCompletedEvent.PickedLine(
                        line.getLineId(),
                        line.getPickedQuantity(),
                        line.wasShort()
                ))
                .toList();

        eventPublisher.publishEvent(new PickingCompletedEvent(
                command.orderId(),
                session.getOrderNumber(),
                pickedLines,
                command.completedBy() != null ? command.completedBy() : getCurrentUser(),
                LocalDateTime.now()
        ));

        log.info("Picking completado para orden {}. Short picks: {}", 
                command.orderId(), session.getShortLines().size());

        return session;
    }

    /**
     * Obtiene una sesión de picking activa por ID de orden.
     */
    public PickingSession getActiveSession(String orderId) {
        return findSessionByOrderId(orderId);
    }

    /**
     * Cancela una sesión de picking.
     */
    @Transactional
    public void cancelPicking(String orderId) {
        PickingSession session = findSessionByOrderId(orderId);
        if (session == null) {
            throw new DomainException("No hay sesión de picking activa para la orden: " + orderId);
        }

        session.cancel();
        activeSessions.remove(session.getSessionId());

        // Restaurar estado de inventory items
        for (PickingSession.PickingLine line : session.getLines()) {
            if (line.getInventoryItemId() != null) {
                updateInventoryItemStatus(line.getInventoryItemId(), InventoryStatus.RESERVED);
            }
        }

        log.info("Sesión de picking cancelada para orden: {}", orderId);
    }

    // ==================== STOCK ALLOCATION (existing) ====================

    @Override
    @Transactional
    public void allocateStock(AllocateStockCommand command) {
        log.info("Iniciando asignación segura para SKU: {} Cantidad: {}", command.sku(), command.quantity());

        List<InventoryItem> availableItems = inventoryRepository.findAvailableStockForAllocation(command.sku());
        BigDecimal quantityNeeded = command.quantity();
        BigDecimal totalAvailable = availableItems.stream()
                .map(InventoryItem::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalAvailable.compareTo(quantityNeeded) < 0) {
            throw new DomainException("Stock insuficiente. Disponible: " + totalAvailable + ", Solicitado: " + quantityNeeded);
        }

        for (InventoryItem item : availableItems) {
            if (quantityNeeded.compareTo(BigDecimal.ZERO) <= 0) break;

            Location location = locationRepository.findByCode(item.getLocationCode())
                    .orElseThrow(() -> new DomainException("Ubicación no encontrada: " + item.getLocationCode()));

            BigDecimal currentQty = item.getQuantity();
            BigDecimal quantityToTake = currentQty.min(quantityNeeded);

            if (currentQty.compareTo(quantityToTake) == 0) {
                item.setStatus(InventoryStatus.RESERVED);
                inventoryRepository.save(item);
            } else {
                location.releaseLoad(item);
                item.setQuantity(currentQty.subtract(quantityToTake));
                location.consolidateLoad(item);
                inventoryRepository.save(item);

                InventoryItem reservedPart = InventoryItem.createReceived(
                        generatePickingLpn(),
                        item.getProductSku(),
                        item.getProduct(),
                        quantityToTake,
                        item.getBatchNumber(),
                        item.getExpiryDate(),
                        item.getLocationCode()
                );
                reservedPart.setStatus(InventoryStatus.RESERVED);
                location.consolidateLoad(reservedPart);
                inventoryRepository.save(reservedPart);
                locationRepository.save(location);
            }

            quantityNeeded = quantityNeeded.subtract(quantityToTake);
        }

        eventPublisher.publishEvent(new StockReservedEvent(
                command.sku(), command.quantity(), getCurrentUser(), LocalDateTime.now()
        ));
    }

    // ==================== PRIVATE HELPERS ====================

    private PickingSession findSessionByOrderId(String orderId) {
        return activeSessions.values().stream()
                .filter(s -> s.getOrderId().equals(orderId) && 
                        s.getStatus() == PickingSession.PickingSessionStatus.IN_PROGRESS)
                .findFirst()
                .orElse(null);
    }

    private void handleShortPick(PickingSession session, PickingSession.PickingLine line,
                               BigDecimal pickedQuantity, String pickedBy, ShortPickDecision decision) {
        log.warn("Short pick detectado para línea {}. Solicitado: {}, Pickeado: {}. Decisión: {}",
                line.getLineId(), line.getAllocatedQuantity(), pickedQuantity, decision);

        switch (decision) {
            case BLOCK_UNTIL_COMPLETE:
                // La orden se pondrá en HOLD cuando se complete el picking
                log.info("Decisión BLOCK_UNTIL_COMPLETE: La orden {} quedará en HOLD", session.getOrderId());
                break;
            case AUTO_REPLENISH:
                // Intentar buscar stock adicional en otra ubicación
                attemptReplenishment(line, session.getOrderId());
                break;
            case ALLOW_PARTIAL_SHIPMENT:
            case MANUAL_DECISION:
            default:
                // Se permite el short, se maneja al completar
                log.info("Short permitido. Se procesará al completar el picking.");
                break;
        }
    }

    private void handleSessionShortPicks(PickingSession session, String completedBy) {
        ShortPickDecision decision = session.getShortPickDecision();
        
        switch (decision) {
            case BLOCK_UNTIL_COMPLETE:
                // El InventoryEventHandler pondrá la orden en HOLD
                log.warn("Sesión {} tiene short picks con BLOCK_UNTIL_COMPLETE", session.getSessionId());
                break;
            case AUTO_REPLENISH:
                // Intentar replenishment para líneas cortas
                for (PickingSession.PickingLine shortLine : session.getShortLines()) {
                    attemptReplenishment(shortLine, session.getOrderId());
                }
                break;
            case ALLOW_PARTIAL_SHIPMENT:
                // Se permite envío parcial, se creará backorder
                log.info("Sesión {} permite envío parcial. Backorder será creado.", session.getSessionId());
                break;
            case MANUAL_DECISION:
                // Requiere intervención manual
                log.warn("Sesión {} requiere decisión manual para {} líneas cortas", 
                        session.getSessionId(), session.getShortLines().size());
                break;
        }
    }

    private void attemptReplenishment(PickingSession.PickingLine line, String orderId) {
        log.info("Intentando replenishment para línea {} (SKU: {})", line.getLineId(), line.getSku());

        List<InventoryItem> alternativeItems = inventoryRepository
                .findAvailableStockForAllocation(line.getSku());

        // Filtrar ubicaciones diferentes a la original
        alternativeItems = alternativeItems.stream()
                .filter(item -> !item.getLocationCode().equals(line.getLocationCode()))
                .toList();

        if (alternativeItems.isEmpty()) {
            log.warn("No se encontró stock alternativo para SKU: {}", line.getSku());
            return;
        }

        log.info("Encontradas {} ubicaciones alternativas para SKU: {}", 
                alternativeItems.size(), line.getSku());
        // El replenishment real requeriría mover stock o crear nueva asignación
        // Por ahora solo logueamos
    }

    private void updateInventoryForPick(InventoryItem item, BigDecimal pickedQuantity) {
        BigDecimal remaining = item.getQuantity().subtract(pickedQuantity);
        
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            // Should not happen but guard against it
            log.warn("Negative remaining quantity for item {}, setting to zero", item.getLpn());
            remaining = BigDecimal.ZERO;
        }
        
        if (remaining.compareTo(BigDecimal.ZERO) == 0) {
            // Todo el stock fue pickeado
            item.setQuantity(BigDecimal.ZERO);
            item.setStatus(InventoryStatus.SHIPPED);
        } else {
            item.setQuantity(remaining);
            // Mantener el estado para seguimiento
        }
        
        inventoryRepository.save(item);
    }

    private void updateInventoryItemStatus(String inventoryItemId, InventoryStatus status) {
        InventoryItem item = inventoryRepository.findByInventoryItemId(inventoryItemId);
        if (item != null) {
            item.setStatus(status);
            inventoryRepository.save(item);
        }
    }

    private Lpn generatePickingLpn() {
        String uuid = java.util.UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
        return Lpn.fromRaw(WmsConstants.PICK_PREFIX + uuid);
    }

    private String getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : WmsConstants.SYSTEM_USER;
    }
}
