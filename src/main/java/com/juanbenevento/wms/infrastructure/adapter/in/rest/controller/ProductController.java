package com.juanbenevento.wms.infrastructure.adapter.in.rest.controller;

import com.juanbenevento.wms.application.ports.in.command.CreateProductCommand;
import com.juanbenevento.wms.application.ports.in.dto.ProductResponse;
import com.juanbenevento.wms.application.ports.in.usecases.ManageProductUseCase;
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
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "1. Catálogo de Productos", description = "Gestión de maestros de productos (SKUs, Dimensiones).")
public class ProductController {

    private final ManageProductUseCase manageProductUseCase;

    @Operation(summary = "Listar productos")
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(manageProductUseCase.getAllProducts());
    }

    @Operation(summary = "Crear producto")
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid CreateProductRequest request) {
        CreateProductCommand command = new CreateProductCommand(
                request.sku(), request.name(), request.description(),
                request.width(), request.height(), request.depth(), request.weight()
        );
        return new ResponseEntity<>(manageProductUseCase.createProduct(command), HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar producto")
    @PutMapping("/{sku}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable String sku, @RequestBody @Valid CreateProductRequest request) {
        CreateProductCommand command = new CreateProductCommand(
                request.sku(), request.name(), request.description(),
                request.width(), request.height(), request.depth(), request.weight()
        );
        return ResponseEntity.ok(manageProductUseCase.updateProduct(sku, command));
    }

    @Operation(summary = "Eliminar producto")
    @DeleteMapping("/{sku}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable String sku) {
        manageProductUseCase.deleteProduct(sku);
        return ResponseEntity.noContent().build();
    }

    public record CreateProductRequest(
            @Schema(example = "TV-LG-65") @NotBlank String sku,
            @Schema(example = "Smart TV 65") @NotBlank String name,
            @Schema(example = "Televisor 4K") String description,
            @NotNull @Positive Double width,
            @NotNull @Positive Double height,
            @NotNull @Positive Double depth,
            @NotNull @Positive Double weight
    ) {}
}