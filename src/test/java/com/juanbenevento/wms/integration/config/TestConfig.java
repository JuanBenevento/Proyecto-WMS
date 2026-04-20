package com.juanbenevento.wms.integration.config;

import com.juanbenevento.wms.inventory.application.port.out.PickingOrderPort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test configuration that provides mock beans for integration tests.
 */
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public PickingOrderPort pickingOrderPort() {
        PickingOrderPort mock = mock(PickingOrderPort.class);
        
        // Mock responses for getOrderLinesForPicking
        when(mock.getOrderLinesForPicking(anyString()))
            .thenReturn(List.of(
                new PickingOrderPort.OrderLineForPicking(
                    "line-001",
                    "SKU-TEST-001",
                    new BigDecimal("10"),
                    "LPN-TEST001",
                    "A-01-01"
                )
            ));
        
        // Mock response for getPickingOrderInfo
        when(mock.getPickingOrderInfo(anyString()))
            .thenReturn(new PickingOrderPort.PickingOrderInfo(
                "order-001",
                "ORD-TEST-001",
                "WH-001",
                "MEDIUM"
            ));
        
        return mock;
    }
}
