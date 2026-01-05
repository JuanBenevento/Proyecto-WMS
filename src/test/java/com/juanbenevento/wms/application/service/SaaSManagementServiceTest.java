package com.juanbenevento.wms.application.service;

import com.juanbenevento.wms.application.ports.in.command.OnboardCompanyCommand;
import com.juanbenevento.wms.application.ports.out.TenantRepositoryPort;
import com.juanbenevento.wms.application.ports.out.UserRepositoryPort;
import com.juanbenevento.wms.domain.exception.TenantAlreadyExistsException;
import com.juanbenevento.wms.domain.model.Tenant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaaSManagementServiceTest {
    @Mock private TenantRepositoryPort tenantRepository;
    @Mock private UserRepositoryPort userRepository;
    @Mock private PasswordEncoder passwordEncoder;

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

        // WHEN
        saasService.onboardNewCustomer(command);

        // THEN
        verify(tenantRepository).save(any(Tenant.class));

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

        assertThrows(TenantAlreadyExistsException.class, () -> {
            saasService.onboardNewCustomer(command);
        });

        verify(tenantRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }
}