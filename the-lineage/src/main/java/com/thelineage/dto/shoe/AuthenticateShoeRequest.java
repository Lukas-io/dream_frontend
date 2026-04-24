package com.thelineage.dto.shoe;

import com.thelineage.domain.ConditionGrade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Curator's authentication verdict for a submitted shoe. Appends an AUTHENTICATED provenance record.")
public record AuthenticateShoeRequest(
        @Schema(description = "Curator-determined condition grade.", example = "EXCELLENT")
        @NotNull ConditionGrade conditionGrade,

        @Schema(description = "Rarity score on a 0-100 scale (higher = rarer).", example = "92",
                minimum = "0", maximum = "100")
        @Min(0) @Max(100) int rarityScore
) {}
