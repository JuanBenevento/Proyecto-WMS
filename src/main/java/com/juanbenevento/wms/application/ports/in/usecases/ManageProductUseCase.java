package com.juanbenevento.wms.application.ports.in.usecases;

import com.juanbenevento.wms.application.ports.in.command.CreateProductCommand;
import com.juanbenevento.wms.application.ports.in.dto.ProductResponse; // DTO

import java.util.List;

public interface ManageProductUseCase {
    ProductResponse createProduct(CreateProductCommand command);
    List<ProductResponse> getAllProducts();
    ProductResponse updateProduct(String sku, CreateProductCommand command);
    void deleteProduct(String sku);
}