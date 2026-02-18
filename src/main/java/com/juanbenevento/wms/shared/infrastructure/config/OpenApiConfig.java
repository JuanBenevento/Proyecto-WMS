package com.juanbenevento.wms.shared.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "WMS Enterprise API",
                version = "1.0.0",
                description = "Documentación oficial del Sistema de Gestión de Almacenes. " +
                        "Gestiona inventarios, ubicaciones y movimientos logísticos.",
                contact = @Contact(
                        name = "Juan Manuel Benevento",
                        url = "https://www.linkedin.com/in/juan-manuel-benevento-1870b5216/"
                ),
                license = @License(name = "MIT License", url = "https://opensource.org/licenses/MIT")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Servidor Local")
        },
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Autenticación JWT. Ingrese el token sin la palabra 'Bearer '",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}