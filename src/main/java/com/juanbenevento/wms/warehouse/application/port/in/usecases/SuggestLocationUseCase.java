package com.juanbenevento.wms.warehouse.application.port.in.usecases;

import com.juanbenevento.wms.inventory.application.port.in.dto.LocationSuggestionResponse;

import java.math.BigDecimal;

public interface SuggestLocationUseCase {
    LocationSuggestionResponse suggestBestLocation(String sku, BigDecimal quantity);
}