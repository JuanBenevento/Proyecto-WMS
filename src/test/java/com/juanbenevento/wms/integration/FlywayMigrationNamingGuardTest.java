package com.juanbenevento.wms.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * S7 — Guard de convención de nombres Flyway.
 *
 * Flyway 10.20.1 IGNORA SILENCIOSAMENTE los archivos `V__x.sql` (versión
 * vacía): no los resuelve, no falla, no los aplica. Un script invisible es
 * peor que uno roto: quien "arregle" el nombre lo activaría de golpe contra
 * una BD que no espera. Este guard garantiza que todo archivo en
 * db/migration tenga versión válida (V&lt;dígitos&gt;__) o sea repeatable (R__).
 */
class FlywayMigrationNamingGuardTest {

    private static final Pattern VALID_NAME = Pattern.compile("^(V\\d+__|R__).+\\.sql$");

    @Test
    @DisplayName("S7: todo archivo de db/migration tiene versión Flyway válida")
    void allMigrationFilesHaveValidFlywayVersions() throws Exception {
        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("classpath:db/migration/*.sql");

        List<String> invalid = Arrays.stream(resources)
                .map(Resource::getFilename)
                .filter(name -> !VALID_NAME.matcher(name).matches())
                .sorted()
                .toList();

        assertTrue(resources.length > 0, "debe existir al menos una migración");
        assertTrue(invalid.isEmpty(),
                "Archivos ignorados silenciosamente por Flyway (versión vacía/inválida): " + invalid);
        assertFalse(invalid.contains(null), "nombre de archivo nulo");
    }
}
