package com.thelineage.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Schema(description = "Multi-dimensional review scoring. Each axis is 1 (worst) to 5 (best).")
public record ReviewScores(
        @Schema(description = "Did the listing description match what arrived?", example = "5",
                minimum = "1", maximum = "5")
        @Min(1) @Max(5) int accuracyScore,

        @Schema(description = "Did the actual condition match the curator-graded condition?", example = "5",
                minimum = "1", maximum = "5")
        @Min(1) @Max(5) int conditionScore,

        @Schema(description = "Was the shipment timely and well-packaged?", example = "4",
                minimum = "1", maximum = "5")
        @Min(1) @Max(5) int shippingScore
) {}
