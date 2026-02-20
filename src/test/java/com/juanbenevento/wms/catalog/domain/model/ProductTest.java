package com.juanbenevento.wms.catalog.domain.model;

import com.juanbenevento.wms.shared.domain.exception.DomainException;
import com.juanbenevento.wms.shared.domain.valueobject.Dimensions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

class ProductTest {

    @Test
    void shouldCreateProductSuccessfully() {
        Dimensions dims = new Dimensions(
                new BigDecimal("10.0"),
                new BigDecimal("10.0"),
                new BigDecimal("10.0"),
                new BigDecimal("5.0")
        );

        Product product = new Product(UUID.randomUUID(), "SKU-123", "Laptop", "Gaming Laptop", dims, null);

        Assertions.assertNotNull(product);
        Assertions.assertEquals("SKU-123", product.getSku());
    }

    @Test
    void shouldIdentifyHeavyLoad() {
        Dimensions heavyDims = new Dimensions(
                new BigDecimal("100.0"),
                new BigDecimal("100.0"),
                new BigDecimal("100.0"),
                new BigDecimal("50.0")
        );

        Product heavyProduct = new Product(UUID.randomUUID(), "SKU-HEAVY", "Motor", "V8", heavyDims, 1L);

        Assertions.assertTrue(heavyProduct.getDimensions().isHeavyLoad());
    }

    @Test
    void shouldThrowErrorForInvalidWeight() {
        Assertions.assertThrows(DomainException.class, () -> {
            new Dimensions(
                    new BigDecimal("10.0"),
                    new BigDecimal("10.0"),
                    new BigDecimal("10.0"),
                    new BigDecimal("-1.0")
            );
        });
    }
}