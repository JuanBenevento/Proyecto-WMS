package com.juanbenevento.wms.infrastructure.config;

import com.juanbenevento.wms.domain.model.Role;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;
import com.juanbenevento.wms.infrastructure.adapter.out.persistence.entity.UserEntity;
import com.juanbenevento.wms.infrastructure.config.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final SpringDataUserRepository springDataUserRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initData() {
        log.info("🚀 Inicializando datos maestros del sistema...");

        TenantContext.setTenantId("SYSTEM");

        try {
            if (springDataUserRepository.count() == 0) {
                log.info("Base vacía. Creando Super Admin...");

                UserEntity superAdmin = UserEntity.builder()
                        .username("superadmin")
                        .password(passwordEncoder.encode("root123"))
                        .role(Role.SUPER_ADMIN)
                        .build();

                springDataUserRepository.save(superAdmin);
                log.info("✅ Super Admin creado exitosamente.");
            } else {
                log.info("El sistema ya tiene usuarios.");
            }
        } catch (Exception e) {
            log.error("❌ Error crítico en inicialización: " + e.getMessage());
        } finally {
            TenantContext.clear();
        }
    }
}