package com.thelineage.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    @Bean
    public OpenAPI lineageOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("The Lineage API")
                        .version("v1")
                        .description("""
                                Premium vintage and collector shoe marketplace — backend API.

                                **Getting started.** Call `POST /auth/login` to obtain a JWT, then click the
                                lock icon above and paste the access token into the **bearerAuth** field.
                                Every protected endpoint will then accept the token automatically.

                                **Dev seed users (local profile).** Password for all is `password123`:
                                - `admin@lineage.test` (ADMIN)
                                - `curator@lineage.test` (CURATOR)
                                - `seller@lineage.test` (SELLER, approved, TIER_2)
                                - `buyer@lineage.test` (BUYER)

                                Public endpoints under `/listings`, `/sellers/{id}`, and `GET /v3/api-docs`
                                are open. Everything else needs the bearer token.

                                Enum value semantics (UserRole, ListingState, OrderStatus, etc.) are
                                documented in the README's enum tables and in `docs/design/`."""))
                .addSecurityItem(new SecurityRequirement().addList(BEARER))
                .components(new Components().addSecuritySchemes(BEARER,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT access token from POST /auth/login")));
    }
}
