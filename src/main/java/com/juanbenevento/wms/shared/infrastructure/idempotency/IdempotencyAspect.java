package com.juanbenevento.wms.shared.infrastructure.idempotency;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class IdempotencyAspect {

    private final SpringDataIdempotencyRepository repository;

    @Around("@annotation(com.juanbenevento.wms.shared.infrastructure.idempotency.Idempotent)")
    public Object enforceIdempotency(ProceedingJoinPoint joinPoint) throws Throwable {

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("El aspecto de idempotencia debe ejecutarse dentro de un request HTTP");
        }

        HttpServletRequest request = attributes.getRequest();
        String idempotencyKey = request.getHeader("Idempotency-Key");

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            log.warn("Intento de operación crítica sin Idempotency-Key en endpoint: {}", request.getRequestURI());
            throw new IllegalArgumentException("El header 'Idempotency-Key' es obligatorio para esta operación.");
        }

        log.debug("Evaluando llave de idempotencia: {}", idempotencyKey);

        try {
            repository.saveAndFlush(new IdempotencyRecordEntity(idempotencyKey));

        } catch (DataIntegrityViolationException e) {
            log.warn("Bloqueo concurrente: La llave de idempotencia {} ya fue procesada.", idempotencyKey);
            throw e;
        }

        return joinPoint.proceed();
    }
}
