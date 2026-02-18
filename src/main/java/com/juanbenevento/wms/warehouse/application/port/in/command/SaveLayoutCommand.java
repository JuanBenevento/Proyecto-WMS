package com.juanbenevento.wms.warehouse.application.port.in.command;

public record SaveLayoutCommand(String tenantId, String layoutJson) {
}
