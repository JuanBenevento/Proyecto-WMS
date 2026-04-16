package com.juanbenevento.wms.inventory.application.service;

import com.juanbenevento.wms.inventory.application.port.out.InventoryRepositoryPort;
import com.juanbenevento.wms.orders.application.port.out.OrderQueryPort;
import com.juanbenevento.wms.inventory.domain.event.StockAssignedEvent;
import com.juanbenevento.wms.inventory.domain.event.StockShortageEvent;
import com.juanbenevento.wms.inventory.domain.model.InventoryItem;
import com.juanbenevento.wms.inventory.domain.model.InventoryStatus;
import com.juanbenevento.wms.warehouse.application.port.out.LocationRepositoryPort;
import com.juanbenevento.wms.warehouse.domain.model.Location;
import com.juanbenevento.wms.shared.domain.valueobject.Lpn;
import com.juanbenevento.wms.shared.domain.valueobject.WmsConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Servicio que detecta órdenes pendientes y les asigna stock automáticamente.
 * 
 * Implementa el patrón Inventory Leads:
 * - Detecta órdenes en estado PENDING
 * - Asigna stock de inventario disponible
 * - Publica StockAssignedEvent o StockShortageEvent
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderAssignmentService {

    private final OrderQueryPort orderQueryPort;
    private final InventoryRepositoryPort inventoryRepository;
    private final LocationRepositoryPort locationRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Detecta y procesa órdenes pendientes cada 30 segundos.
     * En producción esto podría ser triggered por eventos en lugar de polling.
     */
    @Scheduled(fixedDelayString = "${wms.order.assignment.interval:30000}")
    public void processPendingOrders() {
        log.debug("Verificando órdenes pendientes para asignación...");
        
        List<String> pendingOrderIds = orderQueryPort.findPendingOrderIds();
        
        if (pendingOrderIds.isEmpty()) {
            log.debug("No hay órdenes pendientes");
            return;
        }
        
        log.info("Encontradas {} órdenes pendientes para asignación", pendingOrderIds.size());
        
        for (String orderId : pendingOrderIds) {
            try {
                processOrder(orderId);
            } catch (Exception e) {
                log.error("Error procesando orden {}: {}", orderId, e.getMessage(), e);
            }
        }
    }

    /**
     * Procesa una orden específica asignándole stock.
     */
    @Transactional
    public void processOrder(String orderId) {
        log.info("Procesando asignación para orden: {}", orderId);
        
        OrderQueryPort.PendingOrderInfo orderInfo = orderQueryPort.getPendingOrderInfo(orderId);
        if (orderInfo == null) {
            log.warn("Orden {} no encontrada o no está pendiente", orderId);
            return;
        }
        
        List<StockAssignedEvent.LineAssignment> successfulAllocations = new ArrayList<>();
        List<StockShortageEvent.LineShortage> shortages = new ArrayList<>();
        String user = getCurrentUser();
        
        for (OrderQueryPort.PendingOrderInfo.OrderLineInfo line : orderInfo.lines()) {
            AllocationResult result = allocateStockForLine(
                orderId, 
                line.lineId(), 
                line.sku(), 
                line.requestedQuantity()
            );
            
            if (result.success()) {
                successfulAllocations.add(new StockAssignedEvent.LineAssignment(
                    line.lineId(),
                    line.sku(),
                    line.requestedQuantity(),
                    result.allocatedQuantity(),
                    result.inventoryItemId(),
                    result.locationCode()
                ));
            } else {
                shortages.add(new StockShortageEvent.LineShortage(
                    line.lineId(),
                    line.sku(),
                    line.requestedQuantity(),
                    result.allocatedQuantity()
                ));
            }
        }
        
        // Publicar evento según resultado
        if (!successfulAllocations.isEmpty()) {
            StockAssignedEvent event = new StockAssignedEvent(
                orderId,
                orderInfo.orderNumber(),
                successfulAllocations,
                user,
                LocalDateTime.now()
            );
            log.info("Publicando StockAssignedEvent para orden {}: {} líneas asignadas", 
                orderId, successfulAllocations.size());
            eventPublisher.publishEvent(event);
        }
        
        if (!shortages.isEmpty()) {
            StockShortageEvent event = new StockShortageEvent(
                orderId,
                orderInfo.orderNumber(),
                shortages,
                "INVENTORY_SHORTAGE",
                user,
                LocalDateTime.now()
            );
            log.warn("Publicando StockShortageEvent para orden {}: {} líneas con faltante", 
                orderId, shortages.size());
            eventPublisher.publishEvent(event);
        }
    }

    private AllocationResult allocateStockForLine(String orderId, String lineId, 
                                                  String sku, BigDecimal quantityNeeded) {
        List<InventoryItem> availableItems = inventoryRepository.findAvailableStockForAllocation(sku);
        
        if (availableItems.isEmpty()) {
            return AllocationResult.shortage(BigDecimal.ZERO);
        }
        
        BigDecimal totalAvailable = availableItems.stream()
            .map(InventoryItem::getQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalAvailable.compareTo(quantityNeeded) < 0) {
            // Hay stock parcial disponible
            return allocatePartialStock(availableItems, quantityNeeded, totalAvailable);
        }
        
        // Hay stock suficiente
        return allocateFullStock(availableItems, quantityNeeded);
    }

    private AllocationResult allocateFullStock(List<InventoryItem> availableItems, 
                                              BigDecimal quantityNeeded) {
        BigDecimal remaining = quantityNeeded;
        String inventoryItemId = null;
        String locationCode = null;
        
        for (InventoryItem item : availableItems) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            
            BigDecimal toTake = item.getQuantity().min(remaining);
            
            Location location = locationRepository.findByCode(item.getLocationCode())
                .orElse(null);
            
            if (location == null) continue;
            
            // Split si es necesario
            if (toTake.compareTo(item.getQuantity()) < 0) {
                // Split: parte se reserva, parte sigue disponible
                item.setQuantity(item.getQuantity().subtract(toTake));
                inventoryRepository.save(item);
                
                InventoryItem reservedPart = InventoryItem.createReceived(
                    generateLpn(),
                    item.getProductSku(),
                    item.getProduct(),
                    toTake,
                    item.getBatchNumber(),
                    item.getExpiryDate(),
                    item.getLocationCode()
                );
                reservedPart.setStatus(InventoryStatus.RESERVED);
                inventoryRepository.save(reservedPart);
                
                inventoryItemId = reservedPart.getLpn().getValue();
                locationCode = item.getLocationCode();
            } else {
                // Tomar todo el item
                item.setStatus(InventoryStatus.RESERVED);
                inventoryRepository.save(item);
                
                inventoryItemId = item.getLpn().getValue();
                locationCode = item.getLocationCode();
            }
            
            remaining = remaining.subtract(toTake);
            
            if (inventoryItemId != null) {
                // Tomamos el primer LPN completo o parcial
                return AllocationResult.success(toTake, inventoryItemId, locationCode);
            }
        }
        
        return AllocationResult.success(quantityNeeded.subtract(remaining), inventoryItemId, locationCode);
    }

    private AllocationResult allocatePartialStock(List<InventoryItem> availableItems,
                                                  BigDecimal requested, BigDecimal totalAvailable) {
        // Asignar todo lo disponible
        BigDecimal remaining = totalAvailable;
        String inventoryItemId = null;
        String locationCode = null;
        
        for (InventoryItem item : availableItems) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            
            Location location = locationRepository.findByCode(item.getLocationCode())
                .orElse(null);
            
            if (location == null) continue;
            
            BigDecimal toTake = item.getQuantity().min(remaining);
            
            item.setStatus(InventoryStatus.RESERVED);
            inventoryRepository.save(item);
            
            inventoryItemId = item.getLpn().getValue();
            locationCode = item.getLocationCode();
            remaining = remaining.subtract(toTake);
            
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
        }
        
        return AllocationResult.success(totalAvailable, inventoryItemId, locationCode);
    }

    private Lpn generateLpn() {
        String uuid = UUID.randomUUID().toString()
            .replace("-", "")
            .substring(0, 8)
            .toUpperCase();
        return Lpn.fromRaw(WmsConstants.PICK_PREFIX + uuid);
    }

    private String getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : WmsConstants.SYSTEM_USER;
    }
    
    private record AllocationResult(
        boolean success,
        BigDecimal allocatedQuantity,
        String inventoryItemId,
        String locationCode
    ) {
        static AllocationResult success(BigDecimal qty, String lpn, String loc) {
            return new AllocationResult(true, qty, lpn, loc);
        }
        static AllocationResult shortage(BigDecimal allocated) {
            return new AllocationResult(false, allocated, null, null);
        }
    }
}
