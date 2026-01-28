package com.juanbenevento.wms.application.service;

import com.juanbenevento.wms.application.mapper.LayoutDtoMapper;
import com.juanbenevento.wms.application.ports.in.command.SaveLayoutCommand;
import com.juanbenevento.wms.application.ports.in.dto.RackSummaryDto;
import com.juanbenevento.wms.application.ports.in.dto.WarehouseLayoutResponse;
import com.juanbenevento.wms.application.ports.in.usecases.ManageLayoutUseCase;
import com.juanbenevento.wms.application.ports.out.LocationRepositoryPort;
import com.juanbenevento.wms.application.ports.out.WarehouseLayoutRepositoryPort;
import com.juanbenevento.wms.domain.model.LayoutContent;
import com.juanbenevento.wms.domain.model.Location;
import com.juanbenevento.wms.domain.model.WarehouseLayout;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            return new RackSummaryDto(rackCode, 0, 0.0, 0.0, 0.0, "UNBOUND");
        }

        double totalMaxWeight = children.stream().mapToDouble(Location::getMaxWeight).sum();
        double currentWeight = children.stream().mapToDouble(Location::getCurrentWeight).sum();
        double occupancy = (totalMaxWeight > 0) ? (currentWeight / totalMaxWeight) : 0.0;

        String status = "EMPTY";
        if (occupancy >= 1.0) status = "OVERLOADED";
        else if (occupancy > 0.9) status = "FULL";
        else if (occupancy > 0.0) status = "PARTIAL";

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