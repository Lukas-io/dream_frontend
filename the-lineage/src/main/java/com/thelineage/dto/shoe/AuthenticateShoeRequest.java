package com.thelineage.dto.shoe;

import com.thelineage.domain.ConditionGrade;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AuthenticateShoeRequest(
        @NotNull ConditionGrade conditionGrade,
        @Min(0) @Max(100) int rarityScore
) {}
