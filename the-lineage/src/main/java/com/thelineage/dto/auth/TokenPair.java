package com.thelineage.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Access + refresh JWTs. Send the access token as `Authorization: Bearer <token>`.")
public record TokenPair(
        @Schema(description = "Short-lived JWT (default 15 minutes). Use on every protected request.",
                example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Longer-lived JWT (default 7 days). Currently used to re-login; refresh endpoint TBD.",
                example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken
) {}
