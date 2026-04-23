package com.juanbenevento.wms.inventory.domain.strategy;

import com.juanbenevento.wms.inventory.application.port.out.LotRepository;
import com.juanbenevento.wms.inventory.domain.model.AllocationContext;
import com.juanbenevento.wms.inventory.domain.model.Lot;
import com.juanbenevento.wms.inventory.domain.model.LotAllocation;
import com.juanbenevento.wms.inventory.domain.model.LotStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FefoAllocationStrategy")
class FefoAllocationStrategyTest {

    @Mock
    private LotRepository lotRepository;

    private FefoAllocationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new FefoAllocationStrategy(lotRepository);
    }

    @Nested
    @DisplayName("selectLots()")
    class SelectLots {

        @Test
        @DisplayName("orders by expiry date ascending (soonest first)")
        void testFefoOrder() {
            Lot lotExpiry1 = createLot("LOT-001", "TOMATE-001", LocalDate.now().plusDays(5));
            Lot lotExpiry2 = createLot("LOT-002", "TOMATE-001", LocalDate.now().plusDays(30));

            when(lotRepository.findActiveLotsForAllocation("TOMATE-001"))
                    .thenReturn(List.of(lotExpiry1, lotExpiry2));

            AllocationContext context = AllocationContext.of(new BigDecimal("50"));
            List<LotAllocation> result = strategy.selectLots("TOMATE-001", new BigDecimal("50"), context);

            assertThat(result).isNotEmpty();
            assertThat(result.get(0)).isNotNull();
        }

        @Test
        @DisplayName("excludes expired lots")
        void testExcludesExpired() {
            Lot expiredLot = createLot("LOT-EXPIRED", "TOMATE-001", LocalDate.now().minusDays(1));

            when(lotRepository.findActiveLotsForAllocation("TOMATE-001"))
                    .thenReturn(List.of(expiredLot));

            AllocationContext context = AllocationContext.of(new BigDecimal("50"));
            List<LotAllocation> result = strategy.selectLots("TOMATE-001", new BigDecimal("50"), context);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("excludes lots without expiry date")
        void testExcludesNoExpiryDate() {
            Lot noExpiry = createLotWithoutExpiry("LOT-001", "TOMATE-001");

            when(lotRepository.findActiveLotsForAllocation("TOMATE-001"))
                    .thenReturn(List.of(noExpiry));

            AllocationContext context = AllocationContext.of(new BigDecimal("50"));
            List<LotAllocation> result = strategy.selectLots("TOMATE-001", new BigDecimal("50"), context);

            // FEFO requires expiry date, so lots without it are excluded
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Strategy Info")
    class StrategyInfo {

        @Test
        @DisplayName("getStrategyName() returns FEFO")
        void testStrategyName() {
            assertThat(strategy.getStrategyName()).isEqualTo("FEFO");
        }

        @Test
        @DisplayName("isEnabled() returns true")
        void testIsEnabled() {
            assertThat(strategy.isEnabled()).isTrue();
        }
    }

    // Helper methods
    private Lot createLot(String lotNumber, String productSku, LocalDate expiryDate) {
        Lot lot = Lot.create(lotNumber, productSku, "BATCH", LocalDate.now().minusDays(10), "Origin", expiryDate, null, null, null, null);
        return lot;
    }

    private Lot createLotWithoutExpiry(String lotNumber, String productSku) {
        return Lot.create(lotNumber, productSku, "BATCH", LocalDate.now().minusDays(10), "Origin");
    }
}