package com.juanbenevento.wms.infrastructure.adapter.out.persistence.adapter;

import com.juanbenevento.wms.application.mapper.ProductMapper;
import com.juanbenevento.wms.application.ports.out.ProductRepositoryPort;
import com.juanbenevento.wms.domain.model.Product;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.entity.ProductEntity;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.repository.SpringDataInventoryRepository;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.repository.SpringDataProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements ProductRepositoryPort {

    private final SpringDataProductRepository springRepository;
    private final SpringDataInventoryRepository inventoryRepository;
    private final ProductMapper mapper;

    @Override
    public List<Product> findAll() {
        return springRepository.findAll().stream()
                .map(mapper::toProductDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return springRepository.findBySku(sku)
                .map(mapper::toProductDomain);
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = mapper.toProductEntity(product);
        ProductEntity saved = springRepository.save(entity);
        return mapper.toProductDomain(saved);
    }

    @Override
    public void delete(String sku) {
        springRepository.findBySku(sku)
                .ifPresent(springRepository::delete);
    }

    @Override
    public boolean existsInInventory(String sku) {
        return !inventoryRepository.findByProductSku(sku).isEmpty();
    }
}