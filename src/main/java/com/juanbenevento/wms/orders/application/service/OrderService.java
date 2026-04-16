package com.juanbenevento.wms.orders.application.service;

import com.juanbenevento.wms.orders.application.mapper.OrderMapper;
import com.juanbenevento.wms.orders.application.port.in.command.*;
import com.juanbenevento.wms.orders.application.port.in.dto.*;
import com.juanbenevento.wms.orders.application.port.out.OrderRepositoryPort;
import com.juanbenevento.wms.orders.domain.event.OrderCreatedEvent;
import com.juanbenevento.wms.orders.domain.event.OrderShippedEvent;
import com.juanbenevento.wms.orders.domain.event.OrderStatusChangedEvent;
import com.juanbenevento.wms.orders.domain.model.Order;
import com.juanbenevento.wms.orders.domain.model.OrderStatus;
import com.juanbenevento.wms.orders.domain.model.OrderStatusInfo;
import com.juanbenevento.wms.orders.domain.model.StatusReason;
import com.juanbenevento.wms.orders.infrastructure.event.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de aplicación para la gestión de pedidos.
 * 
 * Coordina las operaciones entre el dominio de Orders e Inventory
 * mediante el Event Bus para lograr consistencia eventual.
 * 
 * Flujo Inventory Leads:
 * 1. OrderService crea orden → OrderCreatedEvent
 * 2. InventoryService detecta evento → asigna stock
 * 3. InventoryService publica StockAssignedEvent
 * 4. OrderService recibe evento → actualiza estado a ALLOCATED
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepositoryPort orderRepository;
    private final OrderMapper orderMapper;
    private final EventBus eventBus;

    public OrderService(OrderRepositoryPort orderRepository, 
                        OrderMapper orderMapper,
                        EventBus eventBus) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.eventBus = eventBus;
    }

    // ==================== COMANDOS ====================

    /**
     * Crea una nueva orden.
     * 
     * Flujo:
     * 1. Crea el dominio con las líneas
     * 2. Confirma la orden (CREATED → CONFIRMED)
     * 3. Persiste en DB
     * 4. Publica OrderCreatedEvent (Inventory se entera)
     */
    @Transactional
    public OrderResponse createOrder(CreateOrderCommand command) {
        log.info("Creando orden para cliente: {}", command.customerId());

        // 1. Mapear command a dominio
        Order order = orderMapper.toOrderDomain(command);
        
        // 2. Confirmar la orden (CREATED → CONFIRMED)
        order.confirm();
        
        // 3. Persistir
        Order savedOrder = orderRepository.save(order);
        
        // 4. Publicar evento para Inventory
        eventBus.publish(new OrderCreatedEvent(savedOrder, null));
        
        log.info("Orden creada: {} [{}]", savedOrder.getOrderNumber(), savedOrder.getOrderId());
        
        return orderMapper.toOrderResponse(savedOrder);
    }

    /**
     * Agrega una línea a una orden existente.
     * Solo funciona en estado CREATED o PENDING.
     */
    @Transactional
    public OrderResponse addLine(String orderId, AddOrderLineCommand command) {
        log.info("Agregando línea a orden: {}", orderId);

        Order order = findOrderById(orderId);
        OrderStatus previousStatus = order.getStatus();
        
        // Crear y agregar la línea
        var orderLine = orderMapper.toOrderLineDomain(command);
        order.addLine(orderLine);
        
        // Persistir y responder
        Order savedOrder = orderRepository.save(order);
        
        // Publicar evento de cambio de estado
        publishStatusChangeEvent(savedOrder, previousStatus, null, null);
        
        return orderMapper.toOrderResponse(savedOrder);
    }

    /**
     * Confirma una orden (CREATED → CONFIRMED).
     */
    @Transactional
    public OrderResponse confirmOrder(String orderId) {
        log.info("Confirmando orden: {}", orderId);
        
        Order order = findOrderById(orderId);
        OrderStatus previousStatus = order.getStatus();
        
        order.confirm();
        
        Order savedOrder = orderRepository.save(order);
        
        publishStatusChangeEvent(savedOrder, previousStatus, StatusReason.NONE, null);
        publishOrderCreated(savedOrder);
        
        return orderMapper.toOrderResponse(savedOrder);
    }

    /**
     * Cancela una orden.
     */
    @Transactional
    public OrderResponse cancelOrder(CancelOrderCommand command) {
        log.info("Cancelando orden: {} [razón: {}]", command.orderId(), command.cancellationReason());

        Order order = findOrderById(command.orderId());
        OrderStatus previousStatus = order.getStatus();
        
        StatusReason reason = command.cancellationReason() != null 
            ? StatusReason.valueOf(command.cancellationReason())
            : StatusReason.CUSTOMER_CANCELLED;
        
        order.cancel(reason, command.cancelledBy(), command.cancellationReason());
        
        Order savedOrder = orderRepository.save(order);
        
        publishStatusChangeEvent(savedOrder, previousStatus, reason, command.cancelledBy());
        
        log.info("Orden cancelada: {}", command.orderId());
        
        return orderMapper.toOrderResponse(savedOrder);
    }

    /**
     * Poné una orden en espera con una razón específica.
     */
    @Transactional
    public OrderResponse holdOrder(String orderId, String reason) {
        log.info("Poniendo orden en espera: {} [razón: {}]", orderId, reason);

        Order order = findOrderById(orderId);
        OrderStatus previousStatus = order.getStatus();
        
        StatusReason statusReason = reason != null 
            ? StatusReason.valueOf(reason)
            : StatusReason.MANUAL_REVIEW;
        
        order.hold(statusReason);
        
        Order savedOrder = orderRepository.save(order);
        
        publishStatusChangeEvent(savedOrder, previousStatus, statusReason, null);
        
        return orderMapper.toOrderResponse(savedOrder);
    }

    /**
     * Libera una orden de espera.
     */
    @Transactional
    public OrderResponse releaseOrder(String orderId) {
        log.info("Liberando orden de espera: {}", orderId);

        Order order = findOrderById(orderId);
        OrderStatus previousStatus = order.getStatus();
        
        order.releaseFromHold();
        
        Order savedOrder = orderRepository.save(order);
        
        publishStatusChangeEvent(savedOrder, previousStatus, StatusReason.NONE, null);
        
        return orderMapper.toOrderResponse(savedOrder);
    }

    /**
     * Inicia el picking de una orden.
     */
    @Transactional
    public OrderResponse startPicking(String orderId) {
        log.info("Iniciando picking: {}", orderId);

        Order order = findOrderById(orderId);
        OrderStatus previousStatus = order.getStatus();
        
        order.startPicking();
        
        Order savedOrder = orderRepository.save(order);
        
        publishStatusChangeEvent(savedOrder, previousStatus, StatusReason.NONE, null);
        
        return orderMapper.toOrderResponse(savedOrder);
    }

    /**
     * Empaca la orden después del picking.
     */
    @Transactional
    public OrderResponse packOrder(String orderId) {
        log.info("Empacando orden: {}", orderId);

        Order order = findOrderById(orderId);
        OrderStatus previousStatus = order.getStatus();
        
        order.pack();
        
        Order savedOrder = orderRepository.save(order);
        
        publishStatusChangeEvent(savedOrder, previousStatus, StatusReason.NONE, null);
        
        return orderMapper.toOrderResponse(savedOrder);
    }

    /**
     * Envía la orden con información del carrier.
     */
    @Transactional
    public OrderResponse shipOrder(String orderId, String carrierId, String trackingNumber) {
        log.info("Enviando orden: {} [carrier: {}, tracking: {}]", orderId, carrierId, trackingNumber);

        Order order = findOrderById(orderId);
        OrderStatus previousStatus = order.getStatus();
        
        order.ship(carrierId, trackingNumber);
        
        Order savedOrder = orderRepository.save(order);
        
        // Publicar evento de envío (para tracking/notificaciones)
        eventBus.publish(new OrderShippedEvent(
            savedOrder, carrierId, null, trackingNumber, null));
        publishStatusChangeEvent(savedOrder, previousStatus, StatusReason.NONE, null);
        
        return orderMapper.toOrderResponse(savedOrder);
    }

    /**
     * Marca la orden como entregada.
     */
    @Transactional
    public OrderResponse deliverOrder(String orderId) {
        log.info("Marcando como entregada: {}", orderId);

        Order order = findOrderById(orderId);
        OrderStatus previousStatus = order.getStatus();
        
        order.deliver();
        
        Order savedOrder = orderRepository.save(order);
        
        publishStatusChangeEvent(savedOrder, previousStatus, StatusReason.NONE, null);
        
        return orderMapper.toOrderResponse(savedOrder);
    }

    // ==================== QUERIES ====================

    /**
     * Obtiene una orden por ID.
     */
    public Optional<OrderResponse> getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .map(orderMapper::toOrderResponse);
    }

    /**
     * Obtiene una orden por número de orden.
     */
    public Optional<OrderResponse> getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(orderMapper::toOrderResponse);
    }

    /**
     * Lista órdenes con filtros.
     */
    public OrderListResponse listOrders(OrderListQuery query) {
        List<Order> orders;
        
        // Aplicar filtros según parámetros
        if (query.customerId() != null && !query.customerId().isBlank()) {
            orders = orderRepository.findByCustomerId(query.customerId());
        } else if (query.warehouseId() != null && !query.warehouseId().isBlank()) {
            if (query.status() != null) {
                orders = orderRepository.findByWarehouseIdAndStatus(query.warehouseId(), query.status());
            } else {
                orders = orderRepository.findByWarehouseId(query.warehouseId());
            }
        } else if (query.status() != null) {
            orders = orderRepository.findByStatus(query.status());
        } else {
            orders = orderRepository.findAll();
        }
        
        List<OrderResponse> responses = orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
        
        return OrderListResponse.of(responses, query.page(), query.size(), responses.size());
    }

    // ==================== MÉTODOS INTERNOS ====================

    /**
     * Busca una orden o lanza excepción.
     */
    private Order findOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + orderId));
    }

    /**
     * Publica evento de cambio de estado.
     */
    private void publishStatusChangeEvent(Order order, OrderStatus previousStatus,
                                          StatusReason reason, String changedBy) {
        eventBus.publish(new OrderStatusChangedEvent(
                order,
                previousStatus,
                StatusReason.NONE,
                reason != null ? reason : order.getStatusReason(),
                changedBy,
                null
        ));
    }

    /**
     * Publica evento OrderCreated (para re-envío o recovery).
     */
    private void publishOrderCreated(Order order) {
        eventBus.publish(new OrderCreatedEvent(order, null));
    }
}
