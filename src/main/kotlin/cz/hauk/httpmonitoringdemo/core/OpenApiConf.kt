package cz.hauk.httpmonitoringdemo.core

import io.swagger.v3.oas.models.Components

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConf {

    @Bean
    fun customOpenAPI(): OpenAPI = OpenAPI()
        .components(
            Components()
                .addSecuritySchemes(
                    "api-key",
                    SecurityScheme().type(SecurityScheme.Type.APIKEY).scheme("ApiKey")
                )
        )

}