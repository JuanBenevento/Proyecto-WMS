package com.juanbenevento.wms.infrastructure.adapter.out.persistence.adapter;

import com.juanbenevento.wms.application.mapper.InventoryMapper;
import com.juanbenevento.wms.application.ports.out.InventoryRepositoryPort;
import com.juanbenevento.wms.domain.model.InventoryItem;
import com.juanbenevento.wms.domain.model.InventoryStatus;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.entity.InventoryItemEntity;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.repository.SpringDataInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InventoryPersistenceAdapter implements InventoryRepositoryPort {

    private final SpringDataInventoryRepository jpaRepository;
    private final InventoryMapper mapper;

    @Override
    public InventoryItem save(InventoryItem item) {
        InventoryItemEntity entity = mapper.toItemEntity(item);
        InventoryItemEntity saved = jpaRepository.save(entity);
        return mapper.toItemDomain(saved);
    }

    @Override
    public Optional<InventoryItem> findByLpn(String lpn) {
        return jpaRepository.findById(lpn)
                .map(mapper::toItemDomain);
    }

    @Override
    public List<InventoryItem> findByProduct(String sku) {
        return jpaRepository.findByProductSku(sku)
                .stream()
                .map(mapper::toItemDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItem> findAvailableStock(String sku) {
        return jpaRepository.findByProductSkuAndStatusOrderByExpiryDateAsc(sku, InventoryStatus.AVAILABLE)
                .stream()
                .map(mapper::toItemDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItem> findReservedStock(String sku) {
        return jpaRepository.findByProductSkuAndStatusOrderByExpiryDateAsc(sku, InventoryStatus.RESERVED)
                .stream()
                .map(mapper::toItemDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItem> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::toItemDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItem> findAvailableStockForAllocation(String sku) {
        return jpaRepository.findAndLockByProductSku(sku, InventoryStatus.AVAILABLE)
                .stream()
                .map(mapper::toItemDomain)
                .collect(Collectors.toList());
    }
}