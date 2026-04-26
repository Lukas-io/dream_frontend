package com.thelineage.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Exchange a refresh token for a new access + refresh token pair.")
public record RefreshRequest(
        @Schema(description = "Refresh token previously issued by POST /auth/login.",
                example = "eyJhbGciOiJIUzI1NiJ9...")
        @NotBlank String refreshToken
) {}
