package com.juanbenevento.wms.infrastructure.adapter.in.rest.controller;

import com.juanbenevento.wms.application.ports.in.command.CreateUserCommand;
import com.juanbenevento.wms.application.ports.in.dto.UserResponse;
import com.juanbenevento.wms.application.ports.in.usecases.ManageUserUseCase;
import com.juanbenevento.wms.domain.model.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "5. Gestión de Usuarios (Admin)", description = "Administración de personal y roles dentro de la empresa.")
public class UserController {

    private final ManageUserUseCase manageUserUseCase;

    @Operation(summary = "Listar usuarios", description = "Devuelve todos los usuarios de la empresa actual.")
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(manageUserUseCase.getAllUsers());
    }

    @Operation(summary = "Crear usuario", description = "Registra un nuevo empleado (Admin u Operador).")
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid CreateUserWebRequest request) {

        // Mapeo: Web Request -> Command (Caso de Uso)
        CreateUserCommand command = new CreateUserCommand(
                request.username(),
                request.password(),
                request.role()
        );

        return new ResponseEntity<>(manageUserUseCase.createUser(command), HttpStatus.CREATED);
    }

    @Operation(summary = "Eliminar usuario", description = "Da de baja un usuario por ID.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        manageUserUseCase.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Obtener roles", description = "Lista los roles disponibles para asignar.")
    @GetMapping("/roles")
    public ResponseEntity<Role[]> getRoles() {
        return ResponseEntity.ok(manageUserUseCase.getAvailableRoles());
    }

    // DTO Web Local (Input)
    public record CreateUserWebRequest(
            @Schema(example = "operador1") @NotBlank(message = "El usuario es obligatorio")
            String username,

            @Schema(example = "123456") @NotBlank(message = "La contraseña es obligatoria")
            String password,

            @Schema(example = "OPERATOR") @NotNull(message = "El rol es obligatorio")
            Role role
    ) {}
}