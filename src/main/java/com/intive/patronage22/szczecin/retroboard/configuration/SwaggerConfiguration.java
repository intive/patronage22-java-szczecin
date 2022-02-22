package com.intive.patronage22.szczecin.retroboard.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
@SecurityScheme(
        name = "tokenAuth",
        type = SecuritySchemeType.APIKEY,
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER, paramName = HttpHeaders.AUTHORIZATION
)
@OpenAPIDefinition(
        info = @Info(title = "Retro board.", version = "v1")
)
public class SwaggerConfiguration {
}
