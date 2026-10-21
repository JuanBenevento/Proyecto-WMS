package com.juanbenevento.wms.integration.config;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.DockerClientFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para tests que requieren Docker/Testcontainers.
 * Los tests se SALTAN (no fallan) cuando el daemon de Docker no está disponible.
 * Registra {@link RequiresDockerCondition} automáticamente vía meta-anotación.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(RequiresDockerCondition.class)
public @interface RequiresDocker {
    String value() default "This test requires Docker to be running";
}

/**
 * Condición JUnit 5 que salta tests si Docker no está disponible.
 * Se registra automáticamente mediante la meta-anotación {@link RequiresDocker}.
 */
class RequiresDockerCondition implements ExecutionCondition {

    private static final ConditionEvaluationResult ENABLED =
        ConditionEvaluationResult.enabled("Docker is available");

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        try {
            // Lanza excepción si el daemon de Docker no está accesible
            DockerClientFactory.instance().client();
            return ENABLED;
        } catch (Exception e) {
            return ConditionEvaluationResult.disabled(
                "Docker is not available: " + e.getMessage()
                    + ". Iniciá Docker Desktop para correr este test."
            );
        }
    }
}
