package com.juanbenevento.wms.inventory.domain.model;

import com.juanbenevento.wms.catalog.domain.model.Product;
import com.juanbenevento.wms.shared.domain.valueobject.BatchNumber;
import com.juanbenevento.wms.shared.domain.valueobject.Lpn;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class InventoryItem {
    
    private final Lpn lpn;
    private final String productSku;
    private final Product product;
    private BigDecimal quantity;
    private final BatchNumber batchNumber;
    private final LocalDate expiryDate;
    private InventoryStatus status;
    private String locationCode;
    private Long version;

    public InventoryItem(Lpn lpn, String productSku, Product product, 
                         BigDecimal quantity, BatchNumber batchNumber,
                         LocalDate expiryDate, InventoryStatus status, 
                         String locationCode, Long version) {
        this.lpn = lpn;
        this.productSku = productSku;
        this.product = product;
        this.quantity = quantity;
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
        this.status = status;
        this.locationCode = locationCode;
        this.version = version;
    }

    // --- FACTORY METHODS ---

    /**
     * Crea un nuevo InventoryItem durante la recepción.
     */
    public static InventoryItem createReceived(Lpn lpn, String productSku, Product product,
                                                BigDecimal quantity, BatchNumber batchNumber,
                                                LocalDate expiryDate, String locationCode) {
        return new InventoryItem(
                lpn,
                productSku,
                product,
                quantity,
                batchNumber,
                expiryDate,
                InventoryStatus.IN_QUALITY_CHECK,
                locationCode,
                null
        );
    }

    /**
     * Reconstruye un InventoryItem desde la DB (sin Product).
     */
    public static InventoryItem fromRepository(Lpn lpn, String productSku, BigDecimal quantity,
                                               BatchNumber batchNumber, LocalDate expiryDate,
                                               InventoryStatus status, String locationCode, Long version) {
        return new InventoryItem(lpn, productSku, null, quantity, batchNumber, 
                                 expiryDate, status, locationCode, version);
    }

    // --- LÓGICA DE NEGOCIO ---

    public void addQuantity(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad a agregar debe ser positiva");
        }
        this.quantity = this.quantity.add(amount);
    }

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
