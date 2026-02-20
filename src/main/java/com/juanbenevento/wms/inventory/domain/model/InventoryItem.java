package com.juanbenevento.wms.inventory.domain.model;

import com.juanbenevento.wms.catalog.domain.model.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class InventoryItem {
    private final String lpn;
    private final String productSku;
    private final Product product;
    private BigDecimal quantity;
    private final String batchNumber;
    private final LocalDate expiryDate;
    private InventoryStatus status;
    private String locationCode;
    private Long version;

    // --- LÓGICA DE NEGOCIO ---

    public void addQuantity(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("La cantidad a agregar debe ser positiva");
        }
        this.quantity = this.quantity.add(amount);
    }

    // Necesario para InventoryAdjustmentCommand
    public void setQuantity(BigDecimal newQuantity) {
        if (newQuantity == null || newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
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

    public BigDecimal calculateTotalWeight() {
        if (product == null || product.getDimensions() == null) {
            return BigDecimal.ZERO;
        }
        return product.getDimensions().weight().multiply(this.quantity);
    }

    public BigDecimal calculateTotalVolume() {
        if (product == null || product.getDimensions() == null) {
            return BigDecimal.ZERO;
        }
        return product.getDimensions().calculateVolume().multiply(this.quantity);
    }
}