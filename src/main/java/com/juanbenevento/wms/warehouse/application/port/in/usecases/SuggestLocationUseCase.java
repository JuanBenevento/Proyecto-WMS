package com.juanbenevento.wms.warehouse.application.port.in.usecases;

import java.math.BigDecimal;

public interface SuggestLocationUseCase {
    String suggestBestLocation(String sku, BigDecimal quantity);
}