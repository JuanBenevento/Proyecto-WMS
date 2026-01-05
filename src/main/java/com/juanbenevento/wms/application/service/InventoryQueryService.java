package com.juanbenevento.wms.application.service;

import com.juanbenevento.wms.application.mapper.InventoryMapper;
import com.juanbenevento.wms.application.ports.in.dto.InventoryItemResponse;
import com.juanbenevento.wms.application.ports.in.usecases.RetrieveInventoryUseCase;
import com.juanbenevento.wms.application.ports.out.InventoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryQueryService implements RetrieveInventoryUseCase {

    private final InventoryRepositoryPort inventoryRepository;
    private final InventoryMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(mapper::toItemResponse)
                .toList();
    }
}