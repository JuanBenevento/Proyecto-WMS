package com.juanbenevento.wms.identity.application.service;

import com.juanbenevento.wms.identity.application.mapper.TenantMapper;
import com.juanbenevento.wms.identity.application.port.in.command.OnboardCompanyCommand;
import com.juanbenevento.wms.identity.application.port.out.TenantRepositoryPort;
import com.juanbenevento.wms.identity.application.port.out.UserRepositoryPort;
import com.juanbenevento.wms.shared.domain.exception.DomainException;
import com.juanbenevento.wms.identity.domain.exception.TenantAlreadyExistsException;
import com.juanbenevento.wms.identity.domain.model.Tenant;
import com.juanbenevento.wms.identity.domain.model.TenantStatus;
import com.juanbenevento.wms.shared.infrastructure.tenant.TenantSchemaManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaaSManagementServiceTest {

    @Mock private TenantRepositoryPort tenantRepository;
    @Mock private UserRepositoryPort userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TenantMapper mapper;
    @Mock private TenantSchemaManager schemaManager;

    @InjectMocks
    private SaaSManagementService saasService;

    @Test
    void shouldOnboardNewCompanySuccessfully() {
        // GIVEN
        OnboardCompanyCommand command = new OnboardCompanyCommand(
                "Coca Cola", "COCA", "admin@coca.com", "admin_coca", "secret123"
        );

        when(tenantRepository.existsById("COCA")).thenReturn(false);
        when(userRepository.existsByUsername("admin_coca")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("HASH_SECRETO");
        when(schemaManager.createSchema("COCA")).thenReturn("tenant_coca");

        // WHEN
        saasService.onboardNewCustomer(command);

        // THEN
        // Verificamos que se guarde el Tenant
        verify(tenantRepository).save(argThat(tenant ->
                tenant.getId().equals("COCA") &&
                        tenant.getStatus() == TenantStatus.ACTIVE
        ));

        // Verificamos que se guarde el Usuario
        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("admin_coca") &&
                        user.getPassword().equals("HASH_SECRETO") &&
                        user.getTenantId().equals("COCA")
        ));
    }

    @Test
    void shouldFailIfCompanyAlreadyExists() {
        // GIVEN
        OnboardCompanyCommand command = new OnboardCompanyCommand(
                "Coca Cola", "COCA", "mail", "user", "pass"
        );

        when(tenantRepository.existsById("COCA")).thenReturn(true);

        // WHEN / THEN
        assertThrows(TenantAlreadyExistsException.class, () -> {
            saasService.onboardNewCustomer(command);
        });

        verify(tenantRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    // --- NUEVOS TESTS DE SUSPENSIÓN / ACTIVACIÓN ---

    @Test
    void shouldSuspendActiveTenant() {
        // GIVEN
        String tenantId = "TENANT-1";
        // Simulamos un tenant existente y ACTIVO en la DB
        Tenant existingTenant = new Tenant(tenantId, "Test Corp", TenantStatus.ACTIVE, "mail@test.com", LocalDateTime.now());

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(existingTenant));

        // WHEN: Llamamos a suspender (active = false)
        saasService.toggleTenantStatus(tenantId, false);

        // THEN
        // Verificamos que se guarde con estado SUSPENDED
        verify(tenantRepository).save(argThat(tenant ->
                tenant.getId().equals(tenantId) &&
                        tenant.getStatus() == TenantStatus.SUSPENDED
        ));
    }

    @Test
    void shouldReactivateSuspendedTenant() {
        // GIVEN
        String tenantId = "TENANT-1";
        // Simulamos un tenant existente y SUSPENDIDO
        Tenant existingTenant = new Tenant(tenantId, "Test Corp", TenantStatus.SUSPENDED, "mail@test.com", LocalDateTime.now());

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(existingTenant));

        // WHEN: Llamamos a activar (active = true)
        saasService.toggleTenantStatus(tenantId, true);

        // THEN
        verify(tenantRepository).save(argThat(tenant ->
                tenant.getStatus() == TenantStatus.ACTIVE
        ));
    }

    @Test
    void shouldThrowExceptionWhenTogglingNonExistentTenant() {
        // GIVEN
        String tenantId = "GHOST-TENANT";
        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThrows(DomainException.class, () -> {
            saasService.toggleTenantStatus(tenantId, true);
        });

        verify(tenantRepository, never()).save(any());
    }
}