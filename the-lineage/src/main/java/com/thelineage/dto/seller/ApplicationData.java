package com.thelineage.dto.seller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApplicationData(
        @NotBlank @Size(min = 30, max = 4000) String narrative,
        @Size(max = 4000) String referencesJson,
        @Size(max = 2000) String inventorySummary
) {}
