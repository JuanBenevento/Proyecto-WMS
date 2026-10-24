package com.juanbenevento.wms.integration.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test del guard de Docker (S8). La anotación {@link RequiresDocker} DEBE
 * registrar {@link RequiresDockerCondition} vía meta-anotación {@link ExtendWith};
 * sin eso, la anotación es decorativa y los tests fallan en vez de saltarse
 * cuando el daemon no está disponible.
 */
class RequiresDockerGuardTest {

    @Test
    @DisplayName("@RequiresDocker debe registrar RequiresDockerCondition via @ExtendWith")
    void requiresDockerAnnotationRegistersCondition() {
        ExtendWith extendWith = RequiresDocker.class.getAnnotation(ExtendWith.class);

        assertNotNull(extendWith,
                "@RequiresDocker debe llevar @ExtendWith meta-anotado; sin él la condición nunca se ejecuta");

        assertArrayEquals(
                new Class[]{RequiresDockerCondition.class},
                extendWith.value(),
                "@ExtendWith debe registrar exactamente RequiresDockerCondition");
    }

    @Test
    @DisplayName("RequiresDockerCondition debe evaluar sin excepción cuando Docker está disponible")
    void conditionEvaluatesWhenDockerAvailable() {
        RequiresDockerCondition condition = new RequiresDockerCondition();
        var result = condition.evaluateExecutionCondition(null);

        assertFalse(result.isDisabled(),
                "Con Docker arriba la condición debe habilitar el test");
    }
}
