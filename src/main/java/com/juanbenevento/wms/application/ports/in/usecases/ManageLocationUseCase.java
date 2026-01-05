package com.juanbenevento.wms.application.ports.in.usecases;

import com.juanbenevento.wms.application.ports.in.command.CreateLocationCommand;
import com.juanbenevento.wms.application.ports.in.dto.LocationResponse; // Usar DTO

import java.util.List;

public interface ManageLocationUseCase {
    List<LocationResponse> getAllLocations();
    LocationResponse createLocation(CreateLocationCommand command);
    LocationResponse updateLocation(String code, CreateLocationCommand command);
    void deleteLocation(String code);
}