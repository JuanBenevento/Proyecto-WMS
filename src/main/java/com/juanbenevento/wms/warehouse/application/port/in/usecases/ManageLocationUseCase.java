package com.juanbenevento.wms.warehouse.application.port.in.usecases;

import com.juanbenevento.wms.warehouse.application.port.in.command.CreateLocationCommand;
import com.juanbenevento.wms.warehouse.application.port.in.dto.LocationResponse;

import java.util.List;

public interface ManageLocationUseCase {
    List<LocationResponse> getAllLocations();
    LocationResponse createLocation(CreateLocationCommand command);
    LocationResponse updateLocation(String code, CreateLocationCommand command);
    void deleteLocation(String code);
    LocationResponse getLocationByCode(String code);
}