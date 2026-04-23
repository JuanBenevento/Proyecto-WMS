package com.juanbenevento.wms.shared.api;

import com.juanbenevento.wms.orders.application.port.out.OrderRepositoryPort;
import com.juanbenevento.wms.orders.domain.model.Order;
import com.juanbenevento.wms.orders.domain.model.OrderStatus;
import com.juanbenevento.wms.shared.infrastructure.rest.ApiResponse;
import com.juanbenevento.wms.warehouse.application.port.out.LocationRepositoryPort;
import com.juanbenevento.wms.warehouse.domain.model.Location;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dashboard REST API for KPIs and metrics.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard KPIs and metrics")
public class DashboardController {

    private final OrderRepositoryPort orderRepository;
    private final LocationRepositoryPort locationRepository;

    @Operation(summary = "Get all KPIs", description = "Returns all dashboard KPIs")
    @GetMapping("/kpis")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllKpis() {
        Map<String, Object> kpis = new HashMap<>();

        // Order KPIs
        List<Order> allOrders = orderRepository.findAll();
        long totalOrders = allOrders.size();
        long pendingOrders = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.PENDING)
            .count();
        long inProgressOrders = allOrders.stream()
            .filter(o -> {
                OrderStatus status = o.getStatus();
                return status == OrderStatus.PICKING || status == OrderStatus.PACKED || status == OrderStatus.SHIPPED;
            })
            .count();
        long completedOrders = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
            .count();

        Map<String, Object> orders = new HashMap<>();
        orders.put("total", totalOrders);
        orders.put("pending", pendingOrders);
        orders.put("inProgress", inProgressOrders);
        orders.put("completed", completedOrders);

        // Warehouse KPIs - use items list to determine usage
        List<Location> allLocations = locationRepository.findAll();
        long totalLocations = allLocations.size();
        long usedLocations = allLocations.stream()
            .filter(l -> l.getItems() != null && !l.getItems().isEmpty())
            .count();

        Map<String, Object> warehouse = new HashMap<>();
        warehouse.put("total", totalLocations);
        warehouse.put("used", usedLocations);
        warehouse.put("available", totalLocations - usedLocations);

        kpis.put("orders", orders);
        kpis.put("warehouse", warehouse);
        kpis.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(ApiResponse.success(kpis));
    }

    @Operation(summary = "Get order metrics", description = "Order statistics and trends")
    @GetMapping("/metrics/orders")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOrderMetrics() {
        List<Order> allOrders = orderRepository.findAll();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("total", allOrders.size());
        metrics.put("created", allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CREATED).count());
        metrics.put("confirmed", allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CONFIRMED).count());
        metrics.put("pending", allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count());
        metrics.put("picking", allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PICKING).count());
        metrics.put("packed", allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PACKED).count());
        metrics.put("shipped", allOrders.stream().filter(o -> o.getStatus() == OrderStatus.SHIPPED).count());
        metrics.put("delivered", allOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count());
        metrics.put("cancelled", allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count());

        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    @Operation(summary = "Get warehouse metrics", description = "Warehouse utilization")
    @GetMapping("/metrics/warehouse")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWarehouseMetrics() {
        List<Location> allLocations = locationRepository.findAll();
        long total = allLocations.size();
        long used = allLocations.stream()
            .filter(l -> l.getItems() != null && !l.getItems().isEmpty())
            .count();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalLocations", total);
        metrics.put("usedLocations", used);
        metrics.put("availableLocations", total - used);

        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    @Operation(summary = "Get recent activity", description = "Today's activity feed")
    @GetMapping("/activity")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getActivity() {
        List<Order> recentOrders = orderRepository.findAll();

        Map<String, Object> activity = new HashMap<>();
        activity.put("totalEvents", recentOrders.size());
        activity.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(ApiResponse.success(activity));
    }
}