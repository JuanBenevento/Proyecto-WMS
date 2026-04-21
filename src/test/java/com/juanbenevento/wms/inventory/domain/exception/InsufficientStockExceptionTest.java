package com.juanbenevento.wms.inventory.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InsufficientStockException.
 */
@DisplayName("InsufficientStockException")
class InsufficientStockExceptionTest {

    @Test
    @DisplayName("should create exception with correct message and store fields")
    void testConstructor_andGetters() {
        String sku = "SKU-123";
        BigDecimal requested = new BigDecimal("100");
        BigDecimal available = new BigDecimal("30");

        InsufficientStockException exception =
                new InsufficientStockException(sku, requested, available);

        // Verify message
        assertTrue(exception.getMessage().contains(sku));
        assertTrue(exception.getMessage().contains("100"));
        assertTrue(exception.getMessage().contains("30"));

        // Verify getters
        assertEquals(sku, exception.getProductSku());
        assertEquals(0, requested.compareTo(exception.getRequestedQuantity()));
        assertEquals(0, available.compareTo(exception.getAvailableQuantity()));
    }

    @Test
    @DisplayName("should handle decimal quantities correctly")
    void testConstructor_decimalQuantities() {
        InsufficientStockException exception = new InsufficientStockException(
                "PRECISION-SKU",
                new BigDecimal("50.75"),
                new BigDecimal("12.50")
        );

        assertEquals("PRECISION-SKU", exception.getProductSku());
        assertEquals(0, new BigDecimal("50.75").compareTo(exception.getRequestedQuantity()));
        assertEquals(0, new BigDecimal("12.50").compareTo(exception.getAvailableQuantity()));
    }

    @Test
    @DisplayName("should extend DomainException")
    void testExtendsDomainException() {
        InsufficientStockException exception =
                new InsufficientStockException("SKU", new BigDecimal("10"), new BigDecimal("5"));

        assertTrue(exception instanceof com.juanbenevento.wms.shared.domain.exception.DomainException);
    }
}