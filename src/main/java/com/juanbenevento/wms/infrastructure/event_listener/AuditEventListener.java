package com.juanbenevento.wms.infrastructure.event_listener;

import com.juanbenevento.wms.application.ports.out.StockMovementLogRepositoryPort;
import com.juanbenevento.wms.domain.event.*;
import com.juanbenevento.wms.domain.model.AuditLog;
import com.juanbenevento.wms.domain.model.StockMovementType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuditEventListener {

    private final StockMovementLogRepositoryPort stockMovementLogRepositoryPort;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleInventoryAdjustment(InventoryAdjustedEvent event) {
        double diff = event.newQuantity() - event.oldQuantity();
        String tipo = diff < 0 ? "PÉRDIDA" : "GANANCIA";

        log.warn("🚨 [AUDITORÍA DE STOCK] Ajuste detectado: {} | LPN: {} | Diferencia: {} | Motivo: {}",
                tipo, event.lpn(), diff, event.reason());

        if (diff < -10) {
            log.error("🔥 ALERTA DE SEGURIDAD: Se ajustaron muchas unidades negativas. Revisar cámaras.");
        }

        // Guardar registro usando el port (desacoplado de la infraestructura directa)
        String username = getCurrentUsername();
        AuditLog auditLog = new AuditLog(
                null, // ID será generado por la base de datos
                event.occurredAt() != null ? event.occurredAt() : LocalDateTime.now(),
                StockMovementType.AJUSTE,
                event.productSku(),
                event.lpn(),
                Math.abs(diff),
                event.oldQuantity(),
                event.newQuantity(),
                username,
                event.reason()
        );

        stockMovementLogRepositoryPort.save(auditLog);
        log.debug("✅ Registro de auditoría guardado: AJUSTE para LPN {}", event.lpn());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleStockReceived(StockReceivedEvent event) {
        log.info("📦 [AUDITORÍA DE STOCK] Recepción detectada: LPN: {} | SKU: {} | Cantidad: {}",
                event.lpn(), event.sku(), event.quantity());

        // Guardar registro usando el port (desacoplado de la infraestructura directa)
        String username = getCurrentUsername();
        AuditLog auditLog = new AuditLog(
                null, // ID será generado por la base de datos
                event.occurredAt() != null ? event.occurredAt() : LocalDateTime.now(),
                StockMovementType.RECEPCION,
                event.sku(),
                event.lpn(),
                event.quantity(),
                null,
                event.quantity(),
                username,
                "Recepción de mercadería"
        );

        stockMovementLogRepositoryPort.save(auditLog);
        log.debug("✅ Registro de auditoría guardado: RECEPCION para LPN {}", event.lpn());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReservation(StockReservedEvent event) {
        AuditLog log = new AuditLog(
                null,
                event.occurredAt(),
                StockMovementType.MOVIMIENTO,
                event.sku(),
                "VARIOUS", // O "RESERVED-BATCH"
                event.quantity(),
                0.0, 0.0,
                event.username(),
                "Reserva de stock para pedido (Picking)"
        );
        // 2. Ahora sí coincide el nombre
        stockMovementLogRepositoryPort.save(log);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleShipping(StockShippedEvent event) {
        AuditLog log = new AuditLog(
                null,
                event.occurredAt(),
                StockMovementType.SALIDA,
                event.sku(),
                "LPN-UNKNOWN", // Idealmente el evento debería traer el LPN
                event.quantity(),
                0.0, 0.0,
                event.username(),
                "Despacho confirmado desde " + event.locationCode()
        );
        stockMovementLogRepositoryPort.save(log);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleStockMove(StockMovedEvent event) {
        log.info("🚚 [AUDITORÍA] Movimiento: {} | LPN: {} | De {} a {}",
                event.type(), event.lpn(), event.oldLocation(), event.newLocation());

        AuditLog auditLog = new AuditLog(
                null,
                event.occurredAt(),
                StockMovementType.MOVIMIENTO,
                event.sku(),
                event.lpn(),
                event.quantity(),
                0.0, 0.0, // No hubo cambio de cantidad, solo de lugar
                event.username(),
                event.type() + ": De " + event.oldLocation() + " a " + event.newLocation()
        );

        stockMovementLogRepositoryPort.save(auditLog);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "SYSTEM";
    }
}