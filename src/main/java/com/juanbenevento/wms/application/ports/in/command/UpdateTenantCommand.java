package com.juanbenevento.wms.application.ports.in.command;

public record UpdateTenantCommand(
        String name,
        String contactEmail
) {}