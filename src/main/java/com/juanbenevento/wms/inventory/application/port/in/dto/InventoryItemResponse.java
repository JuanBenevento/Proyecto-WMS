package com.juanbenevento.wms.inventory.application.port.in.dto;

import java.time.LocalDate;

public record InventoryItemResponse(
        String lpn,
        String sku,
        String productName,
        Double quantity,
        String status,
        String batchNumber,
        LocalDate expiryDate,
        String locationCode
) {}