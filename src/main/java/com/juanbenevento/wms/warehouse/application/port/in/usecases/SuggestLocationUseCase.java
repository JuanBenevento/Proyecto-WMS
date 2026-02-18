package com.juanbenevento.wms.warehouse.application.port.in.usecases;

public interface SuggestLocationUseCase {
    String suggestBestLocation(String sku, Double quantity);
}