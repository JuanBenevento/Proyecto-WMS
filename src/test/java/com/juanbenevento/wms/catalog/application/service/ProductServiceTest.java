package com.juanbenevento.wms.catalog.application.service;

import com.juanbenevento.wms.catalog.application.mapper.ProductMapper;
import com.juanbenevento.wms.catalog.application.port.in.command.CreateProductCommand;
import com.juanbenevento.wms.catalog.application.port.out.ProductRepositoryPort;
import com.juanbenevento.wms.catalog.domain.exception.ProductInUseException;
import com.juanbenevento.wms.shared.domain.valueobject.Dimensions;
import com.juanbenevento.wms.catalog.domain.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock private ProductRepositoryPort productRepository;
    @Mock private ProductMapper mapper;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldThrowError_WhenUpdatingDimensionsWithExistingStock() {
        // GIVEN
        String sku = "TV-01";
        Dimensions dims = new Dimensions(
                new BigDecimal("10.0"), new BigDecimal("10.0"),
                new BigDecimal("10.0"), new BigDecimal("10.0")
        );
        Product existing = new Product(UUID.randomUUID(), sku, "TV", "Desc", dims, 1L);

        CreateProductCommand updateCmd = new CreateProductCommand(
                sku, "TV", "Desc",
                new BigDecimal("10.0"), new BigDecimal("10.0"),
                new BigDecimal("10.0"), new BigDecimal("20.0")
        );

        when(productRepository.findBySku(sku)).thenReturn(Optional.of(existing));
        when(productRepository.existsInInventory(sku)).thenReturn(true);

        // WHEN & THEN
        assertThrows(ProductInUseException.class, () -> {
            productService.updateProduct(sku, updateCmd);
        });

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldAllowUpdate_WhenNoStockExists() {
        // GIVEN
        String sku = "TV-01";
        Dimensions dims = new Dimensions(
                new BigDecimal("10.0"), new BigDecimal("10.0"),
                new BigDecimal("10.0"), new BigDecimal("10.0")
        );
        Product existing = new Product(UUID.randomUUID(), sku, "TV", "Desc", dims, 1L);

        CreateProductCommand updateCmd = new CreateProductCommand(
                sku, "TV", "Desc",
                new BigDecimal("10.0"), new BigDecimal("10.0"),
                new BigDecimal("10.0"), new BigDecimal("20.0")
        );

        when(productRepository.findBySku(sku)).thenReturn(Optional.of(existing));
        when(productRepository.existsInInventory(sku)).thenReturn(false);

        // WHEN
        productService.updateProduct(sku, updateCmd);

        // THEN
        verify(productRepository).save(any(Product.class));
        verify(mapper).toProductResponse(any());
    }
}