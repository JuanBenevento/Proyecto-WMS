package com.juanbenevento.wms.infrastructure.adapter.in.rest.controller;

import com.juanbenevento.wms.application.ports.in.command.CreateLocationCommand;
import com.juanbenevento.wms.application.ports.in.dto.LocationResponse;
import com.juanbenevento.wms.application.ports.in.usecases.ManageLocationUseCase;
import com.juanbenevento.wms.domain.model.ZoneType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Tag(name = "2. Topología y Ubicaciones", description = "Gestión del mapa físico (Racks, Pasillos, Zonas).")
public class LocationController {

    private final ManageLocationUseCase manageLocationUseCase;

    @Operation(summary = "Ver mapa del depósito", description = "Lista todas las ubicaciones y su estado de ocupación.")
    @GetMapping
    public ResponseEntity<List<LocationResponse>> getAllLocations() {
        return ResponseEntity.ok(manageLocationUseCase.getAllLocations());
    }

    @Operation(summary = "Crear ubicación")
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<LocationResponse> createLocation(@RequestBody @Valid CreateLocationRequest request) {
        CreateLocationCommand command = new CreateLocationCommand(
                request.locationCode(), request.zoneType(), request.maxWeight(), request.maxVolume()
        );
        return new ResponseEntity<>(manageLocationUseCase.createLocation(command), HttpStatus.CREATED);
    }

    @Operation(summary = "Modificar ubicación")
    @PutMapping("/{code}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<LocationResponse> updateLocation(@PathVariable String code, @RequestBody @Valid CreateLocationRequest request) {
        CreateLocationCommand command = new CreateLocationCommand(
                code, request.zoneType(), request.maxWeight(), request.maxVolume()
        );
        return ResponseEntity.ok(manageLocationUseCase.updateLocation(code, command));
    }

    @Operation(summary = "Eliminar ubicación", description = "Solo permitido si la ubicación está vacía.")
    @DeleteMapping("/{code}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteLocation(@PathVariable String code) {
        manageLocationUseCase.deleteLocation(code);
        return ResponseEntity.noContent().build();
    }

    // DTO Local
    public record CreateLocationRequest(
            @Schema(example = "A-01-01-1") @NotBlank String locationCode,
            @Schema(example = "DRY_STORAGE") @NotNull ZoneType zoneType,
            @Schema(example = "1000.0") @NotNull @Positive Double maxWeight,
            @Schema(example = "2000000.0") @NotNull @Positive Double maxVolume
    ) {}
}