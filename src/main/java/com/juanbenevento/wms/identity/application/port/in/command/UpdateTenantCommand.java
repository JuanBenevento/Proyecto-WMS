package com.juanbenevento.wms.identity.application.port.in.command;

public record UpdateTenantCommand(
        String name,
        String contactEmail
) {}