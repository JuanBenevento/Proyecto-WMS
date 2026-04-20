package com.juanbenevento.wms.inventory.application.service;

import com.juanbenevento.wms.inventory.application.port.in.command.AllocateStockCommand;
import com.juanbenevento.wms.inventory.application.port.in.command.CompletePickingCommand;
import com.juanbenevento.wms.inventory.application.port.in.command.PickLineCommand;
import com.juanbenevento.wms.inventory.application.port.in.command.StartPickingCommand;
import com.juanbenevento.wms.inventory.application.port.in.command.StartPickingCommand.ShortPickDecision;
import com.juanbenevento.wms.inventory.application.port.out.InventoryRepositoryPort;
import com.juanbenevento.wms.inventory.application.port.out.PickingOrderPort;
import com.juanbenevento.wms.inventory.application.port.out.PickingOrderPort.OrderLineForPicking;
import com.juanbenevento.wms.inventory.application.port.out.PickingOrderPort.PickingOrderInfo;
import com.juanbenevento.wms.inventory.domain.model.InventoryItem;
import com.juanbenevento.wms.inventory.domain.model.InventoryStatus;
import com.juanbenevento.wms.inventory.domain.model.PickingSession;
import com.juanbenevento.wms.catalog.domain.model.Product;
import com.juanbenevento.wms.shared.domain.exception.DomainException;
import com.juanbenevento.wms.shared.domain.valueobject.BatchNumber;
import com.juanbenevento.wms.shared.domain.valueobject.Dimensions;
import com.juanbenevento.wms.shared.domain.valueobject.Lpn;
import com.juanbenevento.wms.warehouse.application.port.out.LocationRepositoryPort;
import com.juanbenevento.wms.warehouse.domain.model.Location;
import com.juanbenevento.wms.warehouse.domain.model.ZoneType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PickingService.
 */
@ExtendWith(MockitoExtension.class)
class PickingServiceTest {

    @Mock
    InventoryRepositoryPort inventoryRepository;

    @Mock
    LocationRepositoryPort locationRepository;

    @Mock
    PickingOrderPort pickingOrderPort;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    PickingService pickingService;

    // ==================== HELPER METHODS ====================

    private void startPickingSession(String orderId, ShortPickDecision decision) {
        when(pickingOrderPort.getPickingOrderInfo(orderId))
            .thenReturn(new PickingOrderInfo(orderId, "ORD-2024-" + orderId, "WH-001", "HIGH"));
        when(pickingOrderPort.getOrderLinesForPicking(orderId))
            .thenReturn(List.of(
                new OrderLineForPicking("LINE-1", "SKU-001", new BigDecimal("10"), "LPN-001", "A-01-01")
            ));
        when(inventoryRepository.findByInventoryItemId("LPN-001"))
            .thenReturn(createInventoryItem("LPN-001", "SKU-001", new BigDecimal("10")));

        StartPickingCommand command = new StartPickingCommand(
            orderId, decision, null, "SYSTEM"
        );
        pickingService.startPicking(command);
    }

    private InventoryItem createInventoryItem(String lpn, String sku, BigDecimal quantity) {
        return InventoryItem.fromRepository(
            Lpn.fromRaw(lpn),
            sku,
            quantity,
            BatchNumber.of("BATCH-1"),
            LocalDate.now().plusMonths(6),
            InventoryStatus.RESERVED,
            "A-01-01",
            1L
        );
    }

    // ==================== EXISTING TEST ====================

    @Test
    @DisplayName("Debe dividir inventario cuando asignación es parcial")
    void shouldSplitInventoryWhenAllocationIsPartial() {
        String sku = "SKU-A";
        String locCode = "A-01-01";

        Product product = new Product(UUID.randomUUID(), sku, "P", "D",
                new Dimensions(new BigDecimal("1.0"), new BigDecimal("1.0"), new BigDecimal("1.0"), new BigDecimal("1.0")), 1L);

        InventoryItem originalItem = InventoryItem.createReceived(
                Lpn.fromRaw("LPN-ORIGINAL"), sku, product, new BigDecimal("10.0"),
                BatchNumber.of("B1"), LocalDate.now(), locCode
        );

        Location location = Location.createRackPosition(
                locCode, "A", "01", "01",
                ZoneType.DRY_STORAGE, new BigDecimal("100.0"), new BigDecimal("100.0")
        );

        location.consolidateLoad(originalItem);

        when(inventoryRepository.findAvailableStockForAllocation(sku)).thenAnswer(inv -> {
            List<InventoryItem> list = new java.util.ArrayList<>();
            list.add(originalItem);
            return list;
        });

        when(locationRepository.findByCode(locCode)).thenReturn(Optional.of(location));

        doAnswer(invocation -> {
            InventoryItem itemGuardado = invocation.getArgument(0);
            if (itemGuardado.getLpn().getValue().equals("LPN-ORIGINAL")) {
                if (itemGuardado.getQuantity().compareTo(new BigDecimal("6.0")) != 0) {
                    throw new RuntimeException("Error: Cantidad esperada 6.0 pero fue " + itemGuardado.getQuantity());
                }
            }
            return itemGuardado;
        }).when(inventoryRepository).save(any(InventoryItem.class));

        pickingService.allocateStock(new AllocateStockCommand(sku, new BigDecimal("4.0")));

        verify(inventoryRepository, atLeast(2)).save(any());
    }

    // ==================== NEW PICKING TESTS ====================

    @Nested
    @DisplayName("startPicking")
    class StartPickingTests {

        @Test
        @DisplayName("Debe iniciar sesión de picking exitosamente")
        void shouldStartPickingSuccessfully() {
            // GIVEN
            String orderId = "ORD-001";
            StartPickingCommand command = new StartPickingCommand(
                orderId, 
                ShortPickDecision.ALLOW_PARTIAL_SHIPMENT,
                null,
                "operator-1"
            );

            when(pickingOrderPort.getPickingOrderInfo(orderId))
                .thenReturn(new PickingOrderInfo(orderId, "ORD-2024-001", "WH-001", "HIGH"));
            when(pickingOrderPort.getOrderLinesForPicking(orderId))
                .thenReturn(List.of(
                    new OrderLineForPicking("LINE-1", "SKU-001", new BigDecimal("10"), "LPN-001", "A-01-01")
                ));
            when(inventoryRepository.findByInventoryItemId("LPN-001"))
                .thenReturn(createInventoryItem("LPN-001", "SKU-001", new BigDecimal("10")));

            // WHEN
            PickingSession session = pickingService.startPicking(command);

            // THEN
            assertNotNull(session);
            assertEquals(orderId, session.getOrderId());
            assertEquals(1, session.getLineCount());
            assertEquals(PickingSession.PickingSessionStatus.IN_PROGRESS, session.getStatus());
            // Verify event was published (the mock was called)
            verify(eventPublisher, atLeastOnce()).publishEvent(any(Object.class));
        }

        @Test
        @DisplayName("Debe rechazar orden inexistente")
        void shouldRejectNonExistentOrder() {
            // GIVEN
            when(pickingOrderPort.getPickingOrderInfo("INVALID")).thenReturn(null);

            StartPickingCommand command = new StartPickingCommand(
                "INVALID", ShortPickDecision.ALLOW_PARTIAL_SHIPMENT, null, null
            );

            // WHEN/THEN
            assertThrows(DomainException.class, () -> pickingService.startPicking(command));
        }

        @Test
        @DisplayName("Debe rechazar orden sin líneas")
        void shouldRejectOrderWithNoLines() {
            // GIVEN
            when(pickingOrderPort.getPickingOrderInfo("ORD-001"))
                .thenReturn(new PickingOrderInfo("ORD-001", "ORD-2024-001", "WH-001", "HIGH"));
            when(pickingOrderPort.getOrderLinesForPicking("ORD-001")).thenReturn(List.of());

            StartPickingCommand command = new StartPickingCommand(
                "ORD-001", ShortPickDecision.ALLOW_PARTIAL_SHIPMENT, null, null
            );

            // WHEN/THEN
            assertThrows(DomainException.class, () -> pickingService.startPicking(command));
        }
    }

    @Nested
    @DisplayName("pickLine")
    class PickLineTests {

        @BeforeEach
        void startSession() {
            String orderId = "ORD-PICK";
            when(pickingOrderPort.getPickingOrderInfo(orderId))
                .thenReturn(new PickingOrderInfo(orderId, "ORD-2024-PICK", "WH-001", "HIGH"));
            when(pickingOrderPort.getOrderLinesForPicking(orderId))
                .thenReturn(List.of(
                    new OrderLineForPicking("LINE-1", "SKU-001", new BigDecimal("10"), "LPN-001", "A-01-01")
                ));
            when(inventoryRepository.findByInventoryItemId("LPN-001"))
                .thenReturn(createInventoryItem("LPN-001", "SKU-001", new BigDecimal("10")));

            StartPickingCommand command = new StartPickingCommand(
                orderId, ShortPickDecision.ALLOW_PARTIAL_SHIPMENT, null, "operator-1"
            );
            pickingService.startPicking(command);
        }

        @Test
        @DisplayName("Debe registrar pick completo exitosamente")
        void shouldRegisterFullPickSuccessfully() {
            // GIVEN
            PickLineCommand command = new PickLineCommand(
                "ORD-PICK", "LINE-1", new BigDecimal("10"), null, "operator-1"
            );

            // WHEN
            PickingSession session = pickingService.pickLine(command);

            // THEN
            assertNotNull(session);
            assertTrue(session.getLines().get(0).isPicked());
            assertFalse(session.getLines().get(0).isShort());
        }

        @Test
        @DisplayName("Debe registrar short pick")
        void shouldRegisterShortPick() {
            // GIVEN
            PickLineCommand command = new PickLineCommand(
                "ORD-PICK", "LINE-1", new BigDecimal("5"), "Solo había 5", "operator-1"
            );

            // WHEN
            PickingSession session = pickingService.pickLine(command);

            // THEN
            assertNotNull(session);
            assertTrue(session.getLines().get(0).isPicked());
            assertTrue(session.getLines().get(0).isShort());
        }

        @Test
        @DisplayName("Debe rechazar pick sin sesión activa")
        void shouldRejectPickWithoutActiveSession() {
            // GIVEN
            PickLineCommand command = new PickLineCommand(
                "NON-EXISTENT", "LINE-1", new BigDecimal("10"), null, null
            );

            // WHEN/THEN
            assertThrows(DomainException.class, () -> pickingService.pickLine(command));
        }
    }

    @Nested
    @DisplayName("completePicking")
    class CompletePickingTests {

        @Test
        @DisplayName("Debe completar picking exitosamente")
        void shouldCompletePickingSuccessfully() {
            // GIVEN
            String orderId = "ORD-COMPLETE";
            startPickingSession(orderId, ShortPickDecision.ALLOW_PARTIAL_SHIPMENT);
            pickingService.pickLine(new PickLineCommand(orderId, "LINE-1", new BigDecimal("10"), null, null));

            CompletePickingCommand command = new CompletePickingCommand(
                orderId,
                List.of(new CompletePickingCommand.PickResult("LINE-1", new BigDecimal("10"), false, null)),
                "operator-1",
                "Completado sin problemas"
            );

            // WHEN
            PickingSession session = pickingService.completePicking(command);

            // THEN
            assertEquals(PickingSession.PickingSessionStatus.COMPLETED, session.getStatus());
            assertFalse(session.hasShortPicks());
            // Verify event was published (the mock was called)
            verify(eventPublisher, atLeastOnce()).publishEvent(any(Object.class));
        }

        @Test
        @DisplayName("Debe rechazar completar sin sesión activa")
        void shouldRejectCompleteWithoutActiveSession() {
            // GIVEN
            CompletePickingCommand command = new CompletePickingCommand(
                "NON-EXISTENT", List.of(), "operator-1", null
            );

            // WHEN/THEN
            assertThrows(DomainException.class, () -> pickingService.completePicking(command));
        }
    }

    @Nested
    @DisplayName("getActiveSession")
    class GetActiveSessionTests {

        @Test
        @DisplayName("Debe retornar sesión activa")
        void shouldReturnActiveSession() {
            // GIVEN
            String orderId = "ORD-ACTIVE";
            startPickingSession(orderId, ShortPickDecision.ALLOW_PARTIAL_SHIPMENT);

            // WHEN
            PickingSession session = pickingService.getActiveSession(orderId);

            // THEN
            assertNotNull(session);
            assertEquals(orderId, session.getOrderId());
        }

        @Test
        @DisplayName("Debe retornar null para orden sin sesión")
        void shouldReturnNullForOrderWithoutSession() {
            // WHEN
            PickingSession session = pickingService.getActiveSession("NO-SESSION");

            // THEN
            assertNull(session);
        }
    }

    @Nested
    @DisplayName("cancelPicking")
    class CancelPickingTests {

        @Test
        @DisplayName("Debe cancelar picking exitosamente")
        void shouldCancelPickingSuccessfully() {
            // GIVEN
            String orderId = "ORD-CANCEL";
            startPickingSession(orderId, ShortPickDecision.ALLOW_PARTIAL_SHIPMENT);

            // WHEN
            pickingService.cancelPicking(orderId);

            // THEN
            assertNull(pickingService.getActiveSession(orderId));
        }

        @Test
        @DisplayName("Debe rechazar cancelar sin sesión activa")
        void shouldRejectCancelWithoutActiveSession() {
            // WHEN/THEN
            assertThrows(DomainException.class, () -> pickingService.cancelPicking("NON-EXISTENT"));
        }
    }
}
