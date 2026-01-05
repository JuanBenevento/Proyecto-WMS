package com.juanbenevento.wms.application.ports.in.usecases;

import com.juanbenevento.wms.application.ports.in.command.CreateUserCommand;
import com.juanbenevento.wms.application.ports.in.dto.UserResponse;
import com.juanbenevento.wms.domain.model.Role;

import java.util.List;

public interface ManageUserUseCase {
    List<UserResponse> getAllUsers();
    UserResponse createUser(CreateUserCommand command);
    void deleteUser(Long id);
    Role[] getAvailableRoles();
}