package com.thelineage.dto.dispute;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record OpenDisputeRequest(
        @NotNull UUID orderId,
        @NotBlank @Size(max = 4000) String reason
) {}
