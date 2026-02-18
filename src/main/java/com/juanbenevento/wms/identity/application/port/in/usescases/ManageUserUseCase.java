package com.juanbenevento.wms.identity.application.port.in.usescases;

import com.juanbenevento.wms.identity.application.port.in.command.CreateUserCommand;
import com.juanbenevento.wms.identity.application.port.in.dto.UserResponse;
import com.juanbenevento.wms.identity.domain.model.Role;

import java.util.List;

public interface ManageUserUseCase {
    List<UserResponse> getAllUsers();
    UserResponse createUser(CreateUserCommand command);
    void deleteUser(Long id);
    Role[] getAvailableRoles();
}