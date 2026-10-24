package com.juanbenevento.wms.orders.infrastructure.in.rest;

import com.juanbenevento.wms.orders.application.port.in.command.*;
import com.juanbenevento.wms.orders.application.port.in.dto.*;
import com.juanbenevento.wms.orders.application.service.OrderService;
import com.juanbenevento.wms.orders.domain.model.OrderStatus;
import com.juanbenevento.wms.shared.infrastructure.rest.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller para gestión de pedidos.
 * 
 * Endpoints siguiendo el patrón de acciones explícitas para mayor claridad
 * en entornos industriales donde la trazabilidad es crítica.
 * 
 * Todas las respuestas usan ApiResponse wrapper estándar para consistencia.
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ==================== CRUD ====================

    /**
     * Crea una nueva orden.
     * POST /api/v1/orders
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderCommand command) {
        OrderResponse response = orderService.createOrder(command);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.created(response));
    }

    /**
     * Obtiene una orden por ID.
     * GET /api/v1/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable String orderId) {
        return orderService.getOrder(orderId)
            .map(response -> ResponseEntity.ok(ApiResponse.success(response)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lista órdenes con filtros opcionales.
     * GET /api/v1/orders
     */
    @GetMapping
    public ResponseEntity<ApiResponse<OrderListResponse>> listOrders(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String warehouseId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String priority,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        OrderListQuery query = OrderListQuery.of(customerId, warehouseId, status, priority, page, size);
        OrderListResponse response = orderService.listOrders(query);
        
        // Add pagination metadata
        var pagination = ApiResponse.PaginationMeta.of(page, size, response.totalElements());
        return ResponseEntity.ok(ApiResponse.successPaginated(response, pagination));
    }

    // ==================== ORDER LINES ====================

    /**
     * Agrega una línea a una orden.
     * POST /api/v1/orders/{orderId}/lines
     */
    @PostMapping("/{orderId}/lines")
    public ResponseEntity<ApiResponse<OrderResponse>> addLine(
            @PathVariable String orderId,
            @Valid @RequestBody AddOrderLineCommand command) {
        OrderResponse response = orderService.addLine(orderId, command);
        return ResponseEntity.ok(ApiResponse.success(response, "Line added to order"));
    }

    // ==================== STATUS TRANSITIONS ====================

    /**
     * Confirma una orden.
     * POST /api/v1/orders/{orderId}/confirm
     */
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<ApiResponse<OrderResponse>> confirmOrder(@PathVariable String orderId) {
        OrderResponse response = orderService.confirmOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(response, "Order confirmed"));
    }

    /**
     * Cancela una orden.
     * POST /api/v1/orders/{orderId}/cancel
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @Valid @RequestBody CancelOrderCommand command) {
        OrderResponse response = orderService.cancelOrder(command);
        return ResponseEntity.ok(ApiResponse.success(response, "Order cancelled"));
    }

    /**
     * Poné una orden en espera.
     * POST /api/v1/orders/{orderId}/hold
     */
    @PostMapping("/{orderId}/hold")
    public ResponseEntity<ApiResponse<OrderResponse>> holdOrder(
            @PathVariable String orderId,
            @RequestParam(required = false, defaultValue = "MANUAL_REVIEW") String reason) {
        OrderResponse response = orderService.holdOrder(orderId, reason);
        return ResponseEntity.ok(ApiResponse.success(response, "Order placed on hold"));
    }

    /**
     * Libera una orden de espera.
     * POST /api/v1/orders/{orderId}/release
     */
    @PostMapping("/{orderId}/release")
    public ResponseEntity<ApiResponse<OrderResponse>> releaseOrder(@PathVariable String orderId) {
        OrderResponse response = orderService.releaseOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(response, "Order released from hold"));
    }

    /**
     * Inicia el picking de una orden.
     * POST /api/v1/orders/{orderId}/pick
     */
    @PostMapping("/{orderId}/pick")
    public ResponseEntity<ApiResponse<OrderResponse>> startPicking(@PathVariable String orderId) {
        OrderResponse response = orderService.startPicking(orderId);
        return ResponseEntity.ok(ApiResponse.success(response, "Picking started"));
    }

    /**
     * Empaca una orden.
     * POST /api/v1/orders/{orderId}/pack
     */
    @PostMapping("/{orderId}/pack")
    public ResponseEntity<ApiResponse<OrderResponse>> packOrder(@PathVariable String orderId) {
        OrderResponse response = orderService.packOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(response, "Order packed"));
    }

    /**
     * Envía una orden.
     * POST /api/v1/orders/{orderId}/ship
     */
    @PostMapping("/{orderId}/ship")
    public ResponseEntity<ApiResponse<OrderResponse>> shipOrder(
            @PathVariable String orderId,
            @RequestParam String carrierId,
            @RequestParam(required = false) String trackingNumber) {
        OrderResponse response = orderService.shipOrder(orderId, carrierId, trackingNumber);
        return ResponseEntity.ok(ApiResponse.success(response, "Order shipped"));
    }

    /**
     * Marca una orden como entregada.
     * POST /api/v1/orders/{orderId}/deliver
     */
    @PostMapping("/{orderId}/deliver")
    public ResponseEntity<ApiResponse<OrderResponse>> deliverOrder(@PathVariable String orderId) {
        OrderResponse response = orderService.deliverOrder(orderId);
        return ResponseEntity.ok(ApiResponse.success(response, "Order delivered"));
    }
    
    // NOTE: Error handling is now centralized in GlobalExceptionHandler
    // All exceptions are automatically converted to ApiResponse format
}