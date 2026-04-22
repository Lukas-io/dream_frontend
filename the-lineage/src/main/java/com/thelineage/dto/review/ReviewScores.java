package com.thelineage.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ReviewScores(
        @Min(1) @Max(5) int accuracyScore,
        @Min(1) @Max(5) int conditionScore,
        @Min(1) @Max(5) int shippingScore
) {}
