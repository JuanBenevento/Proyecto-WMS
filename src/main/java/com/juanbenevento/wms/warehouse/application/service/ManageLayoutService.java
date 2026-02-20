package com.juanbenevento.wms.warehouse.application.service;

import com.juanbenevento.wms.warehouse.application.mapper.LayoutDtoMapper;
import com.juanbenevento.wms.warehouse.application.port.in.command.SaveLayoutCommand;
import com.juanbenevento.wms.warehouse.application.port.in.dto.RackSummaryDto;
import com.juanbenevento.wms.warehouse.application.port.in.dto.WarehouseLayoutResponse;
import com.juanbenevento.wms.warehouse.application.port.in.usecases.ManageLayoutUseCase;
import com.juanbenevento.wms.warehouse.application.port.out.LocationRepositoryPort;
import com.juanbenevento.wms.warehouse.application.port.out.WarehouseLayoutRepositoryPort;
import com.juanbenevento.wms.warehouse.domain.model.LayoutContent;
import com.juanbenevento.wms.warehouse.domain.model.Location;
import com.juanbenevento.wms.warehouse.domain.model.WarehouseLayout;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManageLayoutService implements ManageLayoutUseCase {

    private final WarehouseLayoutRepositoryPort layoutRepository;
    private final LocationRepositoryPort locationRepository;
    private final LayoutDtoMapper dtoMapper;

    @Override
    @Transactional(readOnly = true)
    public WarehouseLayoutResponse getLayout(String tenantId) {
        return layoutRepository.findByTenantId(tenantId)
                .map(dtoMapper::toResponse)
                .orElseGet(() -> new WarehouseLayoutResponse(
                        "new",
                        tenantId,
                        "{\"objects\": []}",
                        0,
                        LocalDateTime.now()
                ));
    }

    @Override
    @Transactional
    public WarehouseLayoutResponse saveLayout(SaveLayoutCommand command) {
        WarehouseLayout layout = layoutRepository.findByTenantId(command.tenantId())
                .orElseGet(() -> WarehouseLayout.createEmpty(command.tenantId()));

        LayoutContent newContent = new LayoutContent(command.layoutJson());
        layout.updateDesign(newContent);

        WarehouseLayout saved = layoutRepository.save(layout);

        return dtoMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> searchLocations(String query) {
        return locationRepository.findByCodeStartingWith(query.toUpperCase())
                .stream()
                .map(Location::getLocationCode)
                .limit(10)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RackSummaryDto getRackSummary(String rackCode) {
        List<Location> children = locationRepository.findChildrenOfRack(rackCode);

        if (children.isEmpty()) {
            return new RackSummaryDto(rackCode, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "UNBOUND");
        }

        BigDecimal totalMaxWeight = children.stream()
                .map(Location::getMaxWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentWeight = children.stream()
                .map(Location::getCurrentWeight)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal occupancy = BigDecimal.ZERO;
        if (totalMaxWeight.compareTo(BigDecimal.ZERO) > 0) {
            occupancy = currentWeight.divide(totalMaxWeight, 4, RoundingMode.HALF_UP);
        }

        String status = "EMPTY";
        if (occupancy.compareTo(BigDecimal.ONE) >= 0) status = "OVERLOADED";
        else if (occupancy.compareTo(new BigDecimal("0.9")) > 0) status = "FULL";
        else if (occupancy.compareTo(BigDecimal.ZERO) > 0) status = "PARTIAL";

        return new RackSummaryDto(
                rackCode,
                children.size(),
                occupancy,
                currentWeight,
                totalMaxWeight,
                status
        );
    }
}