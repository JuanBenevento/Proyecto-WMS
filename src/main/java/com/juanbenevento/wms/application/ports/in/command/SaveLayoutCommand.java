package com.juanbenevento.wms.application.ports.in.command;

public record SaveLayoutCommand(String tenantId, String layoutJson) {
}
