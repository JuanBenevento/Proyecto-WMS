package com.juanbenevento.wms.application.service;

import com.juanbenevento.wms.application.mapper.TenantMapper;
import com.juanbenevento.wms.application.ports.in.command.OnboardCompanyCommand;
import com.juanbenevento.wms.application.ports.in.command.UpdateTenantCommand;
import com.juanbenevento.wms.application.ports.in.dto.TenantResponse;
import com.juanbenevento.wms.application.ports.in.usecases.ManageSaaSUseCase;
import com.juanbenevento.wms.application.ports.out.TenantRepositoryPort;
import com.juanbenevento.wms.application.ports.out.UserRepositoryPort;
import com.juanbenevento.wms.domain.exception.DomainException;
import com.juanbenevento.wms.domain.exception.TenantAlreadyExistsException;
import com.juanbenevento.wms.domain.exception.UserAlreadyExistsException;
import com.juanbenevento.wms.domain.model.Role;
import com.juanbenevento.wms.domain.model.Tenant;
import com.juanbenevento.wms.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SaaSManagementService implements ManageSaaSUseCase {

    private final TenantRepositoryPort tenantRepository;
    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<TenantResponse> getAllTenants() {
        return tenantRepository.findAll().stream()
                .map(mapper::toTenantResponse)
                .toList();
    }

    @Override
    @Transactional
    public void onboardNewCustomer(OnboardCompanyCommand command) {
        if (tenantRepository.existsById(command.companyId())) {
            throw new TenantAlreadyExistsException(command.companyId());
        }
        if (userRepository.existsByUsername(command.adminUsername())) {
            throw new UserAlreadyExistsException(command.adminUsername());
        }

        Tenant tenant = Tenant.create(
                command.companyId(),
                command.companyName(),
                command.adminEmail()
        );
        tenantRepository.save(tenant);

        User adminUser = User.create(
                command.adminUsername(),
                passwordEncoder.encode(command.adminPassword()),
                Role.ADMIN,
                tenant.getId()
        );
        userRepository.save(adminUser);
    }

    @Override
    @Transactional
    public void toggleTenantStatus(String tenantId, boolean isActive) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new DomainException("Tenant no encontrado con ID: " + tenantId));

        if (isActive) {
            tenant.activate();
        } else {
            tenant.suspend();
        }

        tenantRepository.save(tenant);
    }

    @Override
    @Transactional
    public void updateTenant(String tenantId, UpdateTenantCommand command) {
        // 1. Buscar
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new DomainException("No se encontró el tenant con ID: " + tenantId));

        // 2. Modificar (El dominio se encarga de validar)
        tenant.updateInfo(command.name(), command.contactEmail());

        // 3. Guardar
        tenantRepository.save(tenant);
    }
}