package com.juanbenevento.wms.shared.infrastructure.config;

import com.juanbenevento.wms.shared.infrastructure.tenant.TenantAwareDataSource;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/**
 * Registra {@link TenantAwareDataSource} como envoltorio del pool HikariCP
 * configurado por Spring Boot, de modo que TODA conexión entregada a la
 * aplicación lleve el search_path del tenant (S1).
 *
 * <p>El flag {@code wms.tenant.isolation.enabled} (default {@code true}) controla
 * el aislamiento (S3). Solo se envuelve el {@link HikariDataSource} real del pool;
 * mocks u otros DataSource de tests no se tocan.
 */
@Slf4j
@Configuration
public class TenantDataSourceConfiguration {

    @Bean
    static BeanPostProcessor tenantAwareDataSourcePostProcessor(Environment environment) {
        boolean isolationEnabled = environment.getProperty(
                "wms.tenant.isolation.enabled", Boolean.class, true);
        log.info("TenantAwareDataSource registered (wms.tenant.isolation.enabled={})", isolationEnabled);

        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof HikariDataSource hikari && !(bean instanceof TenantAwareDataSource)) {
                    return new TenantAwareDataSource(hikari, isolationEnabled);
                }
                return bean;
            }
        };
    }
}
