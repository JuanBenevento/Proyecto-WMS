package com.juanbenevento.wms.warehouse.application.service;

import com.juanbenevento.wms.warehouse.application.port.in.usecases.SuggestLocationUseCase;
import com.juanbenevento.wms.warehouse.application.port.out.LocationRepositoryPort;
import com.juanbenevento.wms.catalog.application.port.out.ProductRepositoryPort;
import com.juanbenevento.wms.shared.domain.exception.DomainException;
import com.juanbenevento.wms.catalog.domain.exception.ProductNotFoundException;
import com.juanbenevento.wms.warehouse.domain.model.Location;
import com.juanbenevento.wms.catalog.domain.model.Product;
import com.juanbenevento.wms.warehouse.domain.model.ZoneType;
import com.juanbenevento.wms.inventory.domain.strategy.PutAwayStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationSuggestionService implements SuggestLocationUseCase {

    private final ProductRepositoryPort productRepository;
    private final LocationRepositoryPort locationRepository;
    private final PutAwayStrategy strategy;

    @Override
    @Transactional(readOnly = true)
    public String suggestBestLocation(String sku, BigDecimal quantity) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException(sku));

        BigDecimal requiredWeight = product.getDimensions().weight().multiply(quantity);
        BigDecimal requiredVolume = product.getStorageVolume().multiply(quantity);
        ZoneType targetZone = strategy.determineZone(product);

        List<Location> candidates = locationRepository.findAvailableLocations(targetZone, requiredWeight, requiredVolume);

        if (candidates.isEmpty()) {
            throw new DomainException(String.format("No hay espacio disponible en zona %s para %.2f kg / %.2f m³", targetZone, requiredWeight, requiredVolume));
        }
        return candidates.get(0).getLocationCode();
    }
}