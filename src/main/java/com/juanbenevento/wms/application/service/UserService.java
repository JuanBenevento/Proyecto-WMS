package com.juanbenevento.wms.application.service;

import com.juanbenevento.wms.application.mapper.UserMapper;
import com.juanbenevento.wms.application.ports.in.command.CreateUserCommand; // <--- Nuevo
import com.juanbenevento.wms.application.ports.in.dto.UserResponse;
import com.juanbenevento.wms.application.ports.in.usecases.ManageUserUseCase;
import com.juanbenevento.wms.application.ports.out.UserRepositoryPort;
import com.juanbenevento.wms.domain.exception.DomainException;
import com.juanbenevento.wms.domain.exception.UserAlreadyExistsException;
import com.juanbenevento.wms.domain.model.Role;
import com.juanbenevento.wms.domain.model.User;
import com.juanbenevento.wms.infrastructure.config.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements ManageUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(mapper::toUserResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserCommand command) {
        if (userRepository.findByUsername(command.username()).isPresent()) {
            throw new UserAlreadyExistsException(command.username());
        }

        String currentTenant = TenantContext.getTenantId();
        if (currentTenant == null || currentTenant.isBlank()) {
            throw new DomainException("Error de seguridad: Falta contexto de Tenant.");
        }

        User user = User.create(
                command.username(),
                passwordEncoder.encode(command.password()),
                command.role(),
                currentTenant
        );

        User savedUser = userRepository.save(user);
        return mapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new DomainException("Usuario no encontrado: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public Role[] getAvailableRoles() {
        return Role.values();
    }
}