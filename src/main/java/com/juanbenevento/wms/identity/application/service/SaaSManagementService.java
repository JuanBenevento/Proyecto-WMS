package com.juanbenevento.wms.identity.application.service;

import com.juanbenevento.wms.identity.application.mapper.TenantMapper;
import com.juanbenevento.wms.identity.application.port.in.command.OnboardCompanyCommand;
import com.juanbenevento.wms.identity.application.port.in.command.UpdateTenantCommand;
import com.juanbenevento.wms.identity.application.port.in.dto.TenantResponse;
import com.juanbenevento.wms.identity.application.port.in.usecases.ManageSaaSUseCase;
import com.juanbenevento.wms.identity.application.port.out.TenantRepositoryPort;
import com.juanbenevento.wms.identity.application.port.out.UserRepositoryPort;
import com.juanbenevento.wms.shared.domain.exception.DomainException;
import com.juanbenevento.wms.identity.domain.exception.TenantAlreadyExistsException;
import com.juanbenevento.wms.identity.domain.exception.UserAlreadyExistsException;
import com.juanbenevento.wms.identity.domain.model.Role;
import com.juanbenevento.wms.identity.domain.model.Tenant;
import com.juanbenevento.wms.identity.domain.model.User;
import com.juanbenevento.wms.shared.domain.valueobject.WmsConstants;
import com.juanbenevento.wms.shared.infrastructure.tenant.TenantContext;
import com.juanbenevento.wms.shared.infrastructure.tenant.TenantSchemaManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaaSManagementService implements ManageSaaSUseCase {

    private final TenantRepositoryPort tenantRepository;
    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantMapper mapper;
    private final TenantSchemaManager schemaManager;

    @Override
    @Transactional(readOnly = true)
    public List<TenantResponse> getAllTenants() {
        return tenantRepository.findAll().stream()
                .map(tenant -> {
                    String schemaName = TenantContext.getSchemaName(tenant.getId());
                    String schemaStatus = getSchemaStatus(tenant.getId(), schemaName);
                    return mapper.toTenantResponse(tenant, schemaName, schemaStatus);
                })
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

        // 1. Create tenant schema BEFORE saving tenant
        String schemaName = createTenantSchema(command.companyId());
        log.info("✅ Schema '{}' created for new tenant '{}'", schemaName, command.companyId());

        // 2. Create tenant entity
        Tenant tenant = Tenant.create(
                command.companyId(),
                command.companyName(),
                command.adminEmail()
        );
        tenantRepository.save(tenant);

        // 3. Create admin user
        User adminUser = User.create(
                command.adminUsername(),
                passwordEncoder.encode(command.adminPassword()),
                Role.ADMIN,
                tenant.getId()
        );
        userRepository.save(adminUser);

        log.info("✅ Tenant '{}' onboarded successfully with schema '{}'", tenant.getId(), schemaName);
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

    /**
     * Creates a PostgreSQL schema for the specified tenant.
     *
     * @param tenantId the tenant identifier
     * @return the name of the created schema
     */
    public String createTenantSchema(String tenantId) {
        String schemaName = schemaManager.createSchema(tenantId);
        return schemaName;
    }

    /**
     * Checks if a schema exists for the specified tenant.
     *
     * @param tenantId the tenant identifier
     * @return true if the schema exists, false otherwise
     */
    public boolean schemaExists(String tenantId) {
        return schemaManager.schemaExistsForTenant(tenantId);
    }

    /**
     * Drops the schema for the specified tenant.
     * WARNING: This is a destructive operation.
     *
     * @param tenantId the tenant identifier
     */
    public void dropTenantSchema(String tenantId) {
        schemaManager.dropSchema(tenantId);
        log.warn("Schema dropped for tenant '{}'", tenantId);
    }

    /**
     * Gets the schema name for a specific tenant.
     *
     * @param tenantId the tenant identifier
     * @return the schema name in format tenant_{id}
     */
    public String getSchemaName(String tenantId) {
        String normalized = WmsConstants.normalizeTenantId(tenantId);
        return WmsConstants.TENANT_SCHEMA_PREFIX + normalized;
    }

    /**
     * Gets the status of a tenant's schema.
     *
     * @param tenantId the tenant identifier
     * @param schemaName the schema name to check
     * @return schema status: "CREATED" if exists, "PENDING" if not, "ERROR" on exception
     */
    private String getSchemaStatus(String tenantId, String schemaName) {
        try {
            if (schemaManager.schemaExists(schemaName)) {
                return "CREATED";
            }
            return "PENDING";
        } catch (Exception e) {
            log.warn("Could not check schema status for tenant '{}': {}", tenantId, e.getMessage());
            return "ERROR";
        }
    }
}