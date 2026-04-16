package com.juanbenevento.wms.inventory.application.port.in.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InventoryItemResponse(
        String lpn,
        String sku,
        String productName,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        BigDecimal quantity,
        String status,
        String batchNumber,
        LocalDate expiryDate,
        String locationCode
) {}