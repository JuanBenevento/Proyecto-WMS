package com.juanbenevento.wms.inventory.application.port.in.dto;

import com.juanbenevento.wms.inventory.application.port.in.command.StartPickingCommand.ShortPickDecision;
import com.juanbenevento.wms.inventory.domain.model.PickingSession;
import com.juanbenevento.wms.inventory.domain.model.PickingSession.PickingLine;
import com.juanbenevento.wms.inventory.domain.model.PickingSession.PickingSessionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para una sesión de picking.
 */
public record PickingSessionResponse(
    String sessionId,
    String orderId,
    String orderNumber,
    String assignedOperator,
    ShortPickDecision shortPickDecision,
    PickingSessionStatus status,
    LocalDateTime startedAt,
    LocalDateTime completedAt,
    int totalLines,
    int pickedLines,
    int shortLines,
    BigDecimal totalPickedQuantity,
    BigDecimal totalShortQuantity,
    List<PickingLineResponse> lines
) {
    public static PickingSessionResponse from(PickingSession session) {
        List<PickingLineResponse> lineResponses = session.getLines().stream()
                .map(PickingLineResponse::from)
                .toList();

        return new PickingSessionResponse(
                session.getSessionId(),
                session.getOrderId(),
                session.getOrderNumber(),
                session.getAssignedOperator(),
                session.getShortPickDecision(),
                session.getStatus(),
                session.getStartedAt(),
                session.getCompletedAt(),
                session.getLineCount(),
                session.getPickedLineCount(),
                session.getShortLines().size(),
                session.getTotalPickedQuantity(),
                session.getTotalShortQuantity(),
                lineResponses
        );
    }

    /**
     * DTO de respuesta para una línea de picking.
     */
    public record PickingLineResponse(
        String lineId,
        String sku,
        BigDecimal allocatedQuantity,
        BigDecimal pickedQuantity,
        String locationCode,
        boolean picked,
        boolean wasShort,
        boolean requiresAttention,
        String notes
    ) {
        public static PickingLineResponse from(PickingLine line) {
            boolean requiresAttention = line.isShort() && 
                    line.getPickedQuantity().compareTo(BigDecimal.ZERO) == 0;

            return new PickingLineResponse(
                    line.getLineId(),
                    line.getSku(),
                    line.getAllocatedQuantity(),
                    line.getPickedQuantity(),
                    line.getLocationCode(),
                    line.isPicked(),
                    line.isShort(),
                    requiresAttention,
                    line.getNotes()
            );
        }
    }
}
