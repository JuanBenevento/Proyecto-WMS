package com.juanbenevento.wms.inventory.domain.model;

import com.juanbenevento.wms.inventory.application.port.in.command.StartPickingCommand.ShortPickDecision;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Sesión de picking que tracking el proceso completo de picking de una orden.
 * 
 * Una sesión de picking:
 * - Se crea cuando un operador inicia el picking
 * - Contiene el estado de cada línea
 * - Permite registro granular de picks por línea
 * - Se completa cuando todas las líneas están procesadas
 */
public class PickingSession {

    private final String sessionId;
    private final String orderId;
    private final String orderNumber;
    private final ShortPickDecision shortPickDecision;
    private final String assignedOperator;
    private final LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private PickingSessionStatus status;
    
    private final List<PickingLine> lines;
    
    public enum PickingSessionStatus {
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }

    private PickingSession(String sessionId, String orderId, String orderNumber,
                         ShortPickDecision shortPickDecision, String assignedOperator,
                         LocalDateTime startedAt, PickingSessionStatus status,
                         List<PickingLine> lines) {
        this.sessionId = sessionId;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.shortPickDecision = shortPickDecision;
        this.assignedOperator = assignedOperator;
        this.startedAt = startedAt;
        this.completedAt = null;
        this.status = status;
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }

    // --- FACTORY METHODS ---

    public static PickingSession create(String orderId, String orderNumber,
                                       ShortPickDecision shortPickDecision,
                                       List<PickingLineInfo> lines,
                                       String assignedOperator) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("El ID de la orden es obligatorio");
        }
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("La orden debe tener líneas para pickear");
        }

        List<PickingLine> pickingLines = lines.stream()
                .map(info -> PickingLine.create(
                        info.lineId(),
                        info.sku(),
                        info.allocatedQuantity(),
                        info.locationCode(),
                        info.inventoryItemId()
                ))
                .toList();

        return new PickingSession(
                UUID.randomUUID().toString(),
                orderId,
                orderNumber,
                shortPickDecision,
                assignedOperator,
                LocalDateTime.now(),
                PickingSessionStatus.IN_PROGRESS,
                pickingLines
        );
    }

    // --- PICK LINE ---

    public void pickLine(String lineId, BigDecimal pickedQuantity, boolean wasShort, String notes) {
        if (status != PickingSessionStatus.IN_PROGRESS) {
            throw new IllegalStateException("La sesión no está en progreso");
        }

        PickingLine line = findLine(lineId);
        line.pick(pickedQuantity, wasShort, notes);
    }

    public void pickLineWithLocation(String lineId, BigDecimal pickedQuantity, 
                                   boolean wasShort, String locationCode, String notes) {
        if (status != PickingSessionStatus.IN_PROGRESS) {
            throw new IllegalStateException("La sesión no está en progreso");
        }

        PickingLine line = findLine(lineId);
        line.pickWithLocation(pickedQuantity, wasShort, locationCode, notes);
    }

    // --- COMPLETE SESSION ---

    public void complete() {
        if (status != PickingSessionStatus.IN_PROGRESS) {
            throw new IllegalStateException("La sesión no está en progreso");
        }

        if (!allLinesPicked()) {
            throw new IllegalStateException("No se pueden completar líneas sin pickear");
        }

        this.status = PickingSessionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = PickingSessionStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
    }

    // --- QUERIES ---

    public boolean allLinesPicked() {
        return lines.stream().allMatch(PickingLine::isPicked);
    }

    public boolean hasShortPicks() {
        return lines.stream().anyMatch(PickingLine::isShort);
    }

    public List<PickingLine> getShortLines() {
        return lines.stream().filter(PickingLine::isShort).toList();
    }

    public BigDecimal getTotalPickedQuantity() {
        return lines.stream()
                .map(PickingLine::getPickedQuantity)
                .filter(q -> q != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalShortQuantity() {
        return lines.stream()
                .filter(PickingLine::isShort)
                .map(line -> line.getAllocatedQuantity().subtract(line.getPickedQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PickingLine findLine(String lineId) {
        return lines.stream()
                .filter(line -> line.getLineId().equals(lineId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Línea no encontrada: " + lineId));
    }

    // --- GETTERS ---

    public String getSessionId() { return sessionId; }
    public String getOrderId() { return orderId; }
    public String getOrderNumber() { return orderNumber; }
    public ShortPickDecision getShortPickDecision() { return shortPickDecision; }
    public String getAssignedOperator() { return assignedOperator; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public PickingSessionStatus getStatus() { return status; }
    public List<PickingLine> getLines() { return List.copyOf(lines); }
    public int getLineCount() { return lines.size(); }
    public int getPickedLineCount() { return (int) lines.stream().filter(PickingLine::isPicked).count(); }

    // --- INFO CLASS ---

    public record PickingLineInfo(
        String lineId,
        String sku,
        BigDecimal allocatedQuantity,
        String locationCode,
        String inventoryItemId
    ) {}

    // --- INNER CLASS: PickingLine ---

    public static class PickingLine {
        private final String lineId;
        private final String sku;
        private final BigDecimal allocatedQuantity;
        private String inventoryItemId;
        private String locationCode;
        private BigDecimal pickedQuantity;
        private boolean picked;
        private boolean wasShort;
        private String notes;
        private LocalDateTime pickedAt;

        private PickingLine(String lineId, String sku, BigDecimal allocatedQuantity,
                           String inventoryItemId, String locationCode,
                           BigDecimal pickedQuantity, boolean picked, boolean wasShort,
                           String notes, LocalDateTime pickedAt) {
            this.lineId = lineId;
            this.sku = sku;
            this.allocatedQuantity = allocatedQuantity;
            this.inventoryItemId = inventoryItemId;
            this.locationCode = locationCode;
            this.pickedQuantity = pickedQuantity;
            this.picked = picked;
            this.wasShort = wasShort;
            this.notes = notes;
            this.pickedAt = pickedAt;
        }

        static PickingLine create(String lineId, String sku, BigDecimal allocatedQuantity,
                                String locationCode, String inventoryItemId) {
            return new PickingLine(lineId, sku, allocatedQuantity, inventoryItemId, 
                                  locationCode, null, false, false, null, null);
        }

        void pick(BigDecimal pickedQuantity, boolean wasShort, String notes) {
            this.pickedQuantity = pickedQuantity;
            this.wasShort = wasShort;
            this.notes = notes;
            this.picked = true;
            this.pickedAt = LocalDateTime.now();
        }

        void pickWithLocation(BigDecimal pickedQuantity, boolean wasShort, 
                            String locationCode, String notes) {
            this.pickedQuantity = pickedQuantity;
            this.wasShort = wasShort;
            this.locationCode = locationCode;
            this.notes = notes;
            this.picked = true;
            this.pickedAt = LocalDateTime.now();
        }

        public boolean isPicked() { return picked; }
        public boolean isShort() { return wasShort; }
        public String getLineId() { return lineId; }
        public String getSku() { return sku; }
        public BigDecimal getAllocatedQuantity() { return allocatedQuantity; }
        public BigDecimal getPickedQuantity() { return pickedQuantity; }
        public String getLocationCode() { return locationCode; }
        public String getInventoryItemId() { return inventoryItemId; }
        public boolean wasShort() { return wasShort; }
        public String getNotes() { return notes; }
        public LocalDateTime getPickedAt() { return pickedAt; }
    }
}
