package com.juanbenevento.wms.shared.infrastructure.config;

import com.juanbenevento.wms.shared.infrastructure.tenant.TenantContext;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class ContextAwareTaskDecorator implements TaskDecorator {

    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        String currentTenant = TenantContext.getTenantId();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        return () -> {
            try {
                TenantContext.setTenantId(currentTenant);
                SecurityContextHolder.setContext(securityContext);
                RequestContextHolder.setRequestAttributes(requestAttributes);

                runnable.run();
            } finally {
                TenantContext.clear();
                SecurityContextHolder.clearContext();
                RequestContextHolder.resetRequestAttributes();
            }
        };
    }
}