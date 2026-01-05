package com.juanbenevento.wms.infrastructure.adapter.in.rest.controller;

import com.juanbenevento.wms.application.ports.in.usecases.LoginUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "0. Autenticación", description = "Gestión de acceso y tokens.")
public class AuthController {
    private final LoginUseCase loginUseCase;

    @Operation(summary = "Iniciar Sesión", description = "Devuelve el Token JWT para usar en el Header 'Authorization'.")
    @PostMapping("/login")
    public ResponseEntity<LoginUseCase.AuthResponse> login(@RequestBody LoginRequest request) {
        LoginUseCase.LoginCommand command = new LoginUseCase.LoginCommand(
                request.username(),
                request.password()
        );

        return ResponseEntity.ok(loginUseCase.login(command));
    }

    // DTO Web para Swagger
    public record LoginRequest(
            @Schema(example = "admin") String username,
            @Schema(example = "123") String password
    ) {}
}