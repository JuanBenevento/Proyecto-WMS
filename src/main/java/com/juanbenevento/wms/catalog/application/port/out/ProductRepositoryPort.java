package com.juanbenevento.wms.catalog.application.port.out;

import com.juanbenevento.wms.catalog.domain.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepositoryPort {
    Optional<Product> findBySku(String sku);
    Product save(Product product);
    List<Product> findAll();
    boolean existsInInventory(String sku);
    void delete(String sku);
}
