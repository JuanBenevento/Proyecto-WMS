package com.juanbenevento.wms.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class InventoryItem {

    private final String lpn;
    private final String productSku;
    private final Product product; // Referencia al producto (para peso/volumen)
    private Double quantity;

    private final String batchNumber;
    private final LocalDate expiryDate;
    private InventoryStatus status;

    // --- CAMPO FALTANTE AGREGADO ---
    private String locationCode;

    private Long version;

    // --- LÓGICA DE NEGOCIO ---

    public void addQuantity(Double amount) {
        if (amount <= 0) throw new IllegalArgumentException("La cantidad a agregar debe ser positiva");
        this.quantity += amount;
    }

    // Necesario para InventoryAdjustmentCommand
    public void setQuantity(Double newQuantity) {
        if (newQuantity < 0) throw new IllegalArgumentException("La cantidad no puede ser negativa");
        this.quantity = newQuantity;
    }

    public void setStatus(InventoryStatus status) {
        this.status = status;
    }

    public void moveTo(String newLocationCode) {
        if (newLocationCode == null || newLocationCode.isBlank()) {
            throw new IllegalArgumentException("El código de ubicación destino es obligatorio");
        }
        this.locationCode = newLocationCode;
    }

    public void approveQualityCheck() {
        if (this.status == InventoryStatus.IN_QUALITY_CHECK) {
            this.status = InventoryStatus.AVAILABLE;
        }
    }

    public Double calculateTotalWeight() {
        if (product == null) return 0.0;
        return product.getDimensions().weight() * quantity;
    }

    public Double calculateTotalVolume() {
        if (product == null) return 0.0;
        return product.getDimensions().calculateVolume() * quantity;
    }
}