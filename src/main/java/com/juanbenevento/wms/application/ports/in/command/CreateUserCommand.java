package com.juanbenevento.wms.application.ports.in.command;

import com.juanbenevento.wms.domain.model.Role;

public record CreateUserCommand(
        String username,
        String password,
        Role role
) {}