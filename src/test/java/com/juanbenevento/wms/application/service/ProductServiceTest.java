package com.juanbenevento.wms.application.service;

import com.juanbenevento.wms.application.mapper.ProductMapper;
import com.juanbenevento.wms.application.ports.in.command.CreateProductCommand;
import com.juanbenevento.wms.application.ports.out.ProductRepositoryPort;
import com.juanbenevento.wms.domain.exception.ProductInUseException;
import com.juanbenevento.wms.domain.model.Dimensions;
import com.juanbenevento.wms.domain.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        String sku = "TV-01";
        Product existing = new Product(UUID.randomUUID(), sku, "TV", "Desc", new Dimensions(10.0, 10.0, 10.0, 10.0), 1L);
        CreateProductCommand updateCmd = new CreateProductCommand(sku, "TV", "Desc", 10.0, 10.0, 10.0, 20.0);

        when(productRepository.findBySku(sku)).thenReturn(Optional.of(existing));
        when(productRepository.existsInInventory(sku)).thenReturn(true);

        assertThrows(ProductInUseException.class, () -> {
            productService.updateProduct(sku, updateCmd);
        });

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldAllowUpdate_WhenNoStockExists() {
        String sku = "TV-01";
        Product existing = new Product(UUID.randomUUID(), sku, "TV", "Desc", new Dimensions(10.0, 10.0, 10.0, 10.0), 1L);
        CreateProductCommand updateCmd = new CreateProductCommand(sku, "TV", "Desc", 10.0, 10.0, 10.0, 20.0); // Cambio de peso

        when(productRepository.findBySku(sku)).thenReturn(Optional.of(existing));
        when(productRepository.existsInInventory(sku)).thenReturn(false);

        productService.updateProduct(sku, updateCmd);

        verify(productRepository).save(any(Product.class));
        // Verify mapper call if needed
        verify(mapper).toProductResponse(any());
    }
}