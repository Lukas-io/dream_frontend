package com.thelineage.dto.seller;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Seller application content reviewed by curators.")
public record ApplicationData(
        @Schema(description = "Free-text narrative describing the applicant's history and credibility.",
                example = "Collector since 2003, 200+ verified transactions on prior platforms. Specialise in pre-2000 Air Jordan retros.")
        @NotBlank @Size(min = 30, max = 4000) String narrative,

        @Schema(description = "Optional JSON blob of references (forum profiles, prior platform ids, etc.).",
                example = "[{\"platform\":\"forum.x\",\"handle\":\"@xyz\"}]")
        @Size(max = 4000) String referencesJson,

        @Schema(description = "Short paragraph summarising current inventory.",
                example = "Approximately 40 pairs in storage; primarily Jordan 1, 3, 4 in DS or near-DS condition.")
        @Size(max = 2000) String inventorySummary
) {}
