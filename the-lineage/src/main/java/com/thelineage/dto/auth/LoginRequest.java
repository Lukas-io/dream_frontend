package com.thelineage.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login credentials. Returns a TokenPair on success.")
public record LoginRequest(
        @Schema(description = "Account email.", example = "seller@lineage.test")
        @Email @NotBlank String email,

        @Schema(description = "Account password.", example = "password123")
        @NotBlank String password
) {}
