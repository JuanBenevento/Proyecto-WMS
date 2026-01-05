package com.juanbenevento.wms.domain.model;

import com.juanbenevento.wms.domain.exception.DomainException;
import lombok.Getter;

import java.util.UUID;

@Getter
public class Product {

    private final UUID id;
    private final String sku;
    private String name;
    private String description;
    private Dimensions dimensions;
    private final Long version;

    // Constructor Principal
    public Product(UUID id, String sku, String name, String description, Dimensions dimensions, Long version) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.dimensions = dimensions;
        this.version = version;
        validateState();
    }

    // Factory Method para creación limpia
    public static Product create(String sku, String name, String description, Dimensions dimensions) {
        return new Product(UUID.randomUUID(), sku, name, description, dimensions, null);
    }


    public void updateDetails(String name, String description) {
        this.name = name;
        this.description = description;
        validateState();
    }

    public void changeDimensions(Dimensions newDimensions) {
        if (newDimensions == null) {
            throw new DomainException("Las nuevas dimensiones no pueden ser nulas");
        }
        this.dimensions = newDimensions;
    }

    public Double getStorageVolume() {
        return this.dimensions.calculateVolume();
    }

    public boolean requiresHeavyMachinery() {
        return this.dimensions.isHeavyLoad();
    }

    private void validateState() {
        if (id == null) throw new DomainException("El ID del producto es obligatorio");
        if (sku == null || sku.isBlank()) throw new DomainException("El SKU es obligatorio");
        if (name == null || name.isBlank()) throw new DomainException("El nombre es obligatorio");
        if (dimensions == null) throw new DomainException("Las dimensiones son obligatorias");
    }
}