package com.juanbenevento.wms.application.ports.in.usecases;

import com.juanbenevento.wms.application.ports.in.command.SaveLayoutCommand;
import com.juanbenevento.wms.application.ports.in.dto.RackSummaryDto;
import com.juanbenevento.wms.application.ports.in.dto.WarehouseLayoutResponse;

import java.util.List;

public interface ManageLayoutUseCase {
    WarehouseLayoutResponse getLayout(String tenantId);
    WarehouseLayoutResponse saveLayout(SaveLayoutCommand command);
    List<String> searchLocations(String query);
    RackSummaryDto getRackSummary(String rackCode);
}
