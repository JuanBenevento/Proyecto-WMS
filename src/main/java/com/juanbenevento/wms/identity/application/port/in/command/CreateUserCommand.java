package com.juanbenevento.wms.identity.application.port.in.command;

import com.juanbenevento.wms.identity.domain.model.Role;

public record CreateUserCommand(
        String username,
        String password,
        Role role
) {}