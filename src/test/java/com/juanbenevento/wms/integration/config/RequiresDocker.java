package com.juanbenevento.wms.integration.config;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark tests that require Docker/Testcontainers.
 * Tests will be skipped if Docker is not available.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresDocker {
    String value() default "This test requires Docker to be running";
}

/**
 * JUnit 5 condition that skips tests if Docker is not available.
 * Applied automatically to any test annotated with @RequiresDocker.
 */
class RequiresDockerCondition implements ExecutionCondition {

    private static final ConditionEvaluationResult ENABLED = 
        ConditionEvaluationResult.enabled("Docker is available");
    
    private static final ConditionEvaluationResult DISABLED = 
        ConditionEvaluationResult.disabled("Docker is not available");

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        // Check if the test has @RequiresDocker annotation
        boolean hasAnnotation = context.getElement()
            .map(el -> el.isAnnotationPresent(RequiresDocker.class))
            .orElse(false);
        
        if (!hasAnnotation) {
            // Not our annotation, don't affect the test
            return ConditionEvaluationResult.enabled("");
        }
        
        try {
            // Try to get Docker client - this will throw if Docker is not available
            DockerClientFactory.instance().client();
            return ENABLED;
        } catch (Exception e) {
            return ConditionEvaluationResult.disabled(
                "Docker is not available: " + e.getMessage() + 
                ". To run these tests, enable Docker Desktop daemon on tcp://localhost:2375"
            );
        }
    }
}