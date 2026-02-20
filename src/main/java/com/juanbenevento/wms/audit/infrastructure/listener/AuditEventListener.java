package com.juanbenevento.wms.audit.infrastructure.listener;

import com.juanbenevento.wms.inventory.application.port.out.StockMovementLogRepositoryPort;
import com.juanbenevento.wms.audit.domain.AuditLog;
import com.juanbenevento.wms.inventory.domain.model.StockMovementType;
import com.juanbenevento.wms.inventory.domain.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
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
        BigDecimal diff = event.newQuantity().subtract(event.oldQuantity());
        String tipo = diff.compareTo(BigDecimal.ZERO) < 0 ? "PÉRDIDA" : "GANANCIA";

        log.warn("🚨 [AUDITORÍA DE STOCK] Ajuste detectado: {} | LPN: {} | Diferencia: {} | Motivo: {}",
                tipo, event.lpn(), diff, event.reason());

        if (diff.compareTo(new BigDecimal("-10")) <= 0) {
            log.error("🔥 ALERTA DE SEGURIDAD: Se ajustaron muchas unidades negativas. Revisar cámaras.");
        }

        String username = getCurrentUsername();
        AuditLog auditLog = new AuditLog(
                null,
                event.occurredAt() != null ? event.occurredAt() : LocalDateTime.now(),
                StockMovementType.AJUSTE,
                event.productSku(),
                event.lpn(),
                diff.abs(), // Cantidad del movimiento en valor absoluto
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

        String username = getCurrentUsername();
        AuditLog auditLog = new AuditLog(
                null,
                event.occurredAt() != null ? event.occurredAt() : LocalDateTime.now(),
                StockMovementType.RECEPCION,
                event.sku(),
                event.lpn(),
                event.quantity(),
                BigDecimal.ZERO,
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
        AuditLog auditLog = new AuditLog(
                null,
                event.occurredAt(),
                StockMovementType.MOVIMIENTO,
                event.sku(),
                "VARIOUS",
                event.quantity(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                event.username(),
                "Reserva de stock para pedido (Picking)"
        );
        // 2. Ahora sí coincide el nombre
        stockMovementLogRepositoryPort.save(auditLog);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleShipping(StockShippedEvent event) {
        AuditLog auditLog = new AuditLog(
                null,
                event.occurredAt(),
                StockMovementType.SALIDA,
                event.sku(),
                "LPN-UNKNOWN",
                event.quantity(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                event.username(),
                "Despacho confirmado desde " + event.locationCode()
        );
        stockMovementLogRepositoryPort.save(auditLog);
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
                BigDecimal.ZERO,
                BigDecimal.ZERO,
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