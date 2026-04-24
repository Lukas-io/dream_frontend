package com.thelineage.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for creating a new BUYER account.")
public record RegisterRequest(
        @Schema(description = "Email used as login identifier.", example = "ada@lineage.test")
        @Email @NotBlank String email,

        @Schema(description = "Plain-text password. Hashed with bcrypt server-side. Minimum 8 characters.",
                example = "password123")
        @NotBlank @Size(min = 8, max = 100) String password,

        @Schema(description = "Public-facing display name shown on comments, reviews, and seller profiles.",
                example = "Ada Lovelace")
        @NotBlank @Size(min = 2, max = 80) String displayName
) {}
