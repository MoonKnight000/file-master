package uz.murodjon.filemaster.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.utils.SpringDocUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uz.murodjon.filemaster.auth.security.CurrentUser

@Configuration
class OpenApiConfig {

    init {
        // `@CurrentUser User` is injected by a custom resolver — don't document it as a parameter.
        SpringDocUtils.getConfig().addAnnotationsToIgnore(CurrentUser::class.java)
    }

    @Bean
    fun fileMasterOpenApi(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("File Master API")
                    .version("v1")
                    .description("File conversion & editing backend. Send `Authorization: Bearer <token>` on all endpoints except the public tools catalog."),
            )
            .addSecurityItem(SecurityRequirement().addList(BEARER_SCHEME))
            .components(
                Components().addSecuritySchemes(
                    BEARER_SCHEME,
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("Opaque session token"),
                ),
            )

    private companion object {
        const val BEARER_SCHEME = "bearerAuth"
    }
}
