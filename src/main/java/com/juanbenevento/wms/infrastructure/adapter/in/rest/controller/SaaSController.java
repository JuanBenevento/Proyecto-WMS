package com.juanbenevento.wms.infrastructure.adapter.in.rest.controller;

import com.juanbenevento.wms.application.ports.in.command.OnboardCompanyCommand;
import com.juanbenevento.wms.application.ports.in.dto.TenantResponse;
import com.juanbenevento.wms.application.ports.in.usecases.ManageSaaSUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/saas")
@RequiredArgsConstructor
@Tag(name = "0. Panel SaaS (Super Admin)", description = "Gestión de clientes y suscripciones")
public class SaaSController {

    private final ManageSaaSUseCase saasUseCase;

    @GetMapping("/tenants")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @Operation(summary = "Listar Empresas", description = "Solo para el dueño del software.")
    public ResponseEntity<List<TenantResponse>> getAllTenants() {
        return ResponseEntity.ok(saasUseCase.getAllTenants());
    }

    @PostMapping("/onboarding")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @Operation(summary = "Alta de Cliente", description = "Crea la empresa y su usuario Admin inicial.")
    public ResponseEntity<String> onboardCompany(@RequestBody @Valid OnboardRequest request) {

        OnboardCompanyCommand command = new OnboardCompanyCommand(
                request.companyName(), request.companyId(), request.adminEmail(),
                request.adminUsername(), request.adminPassword()
        );

        saasUseCase.onboardNewCustomer(command);
        return ResponseEntity.ok("Empresa " + request.companyName() + " registrada exitosamente.");
    }

    // DTO Web Local (Input) con Validaciones
    public record OnboardRequest(
            @Schema(example = "Logística Global S.A.") @NotBlank(message = "El nombre es obligatorio")
            String companyName,

            @Schema(example = "LOG-GLOBAL") @NotBlank(message = "El ID es obligatorio")
            String companyId,

            @Schema(example = "admin@global.com") @NotBlank @Email(message = "Email inválido")
            String adminEmail,

            @Schema(example = "admin_global") @NotBlank
            String adminUsername,

            @Schema(example = "Secret.123") @NotBlank
            String adminPassword
    ) {}
}