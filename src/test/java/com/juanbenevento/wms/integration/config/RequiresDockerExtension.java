package com.juanbenevento.wms.integration.config;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

/**
 * JUnit 5 extension that skips tests if Docker is not available.
 * Use @ExtendWith(RequiresDockerExtension.class) on integration tests.
 */
public class RequiresDockerExtension implements ExecutionCondition {

    private static final ConditionEvaluationResult ENABLED = 
        ConditionEvaluationResult.enabled("Docker is available");
    
    private static final ConditionEvaluationResult DISABLED = 
        ConditionEvaluationResult.disabled("Docker is not available");

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
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