package com.thelineage.dto.shoe;

import com.thelineage.domain.ConditionGrade;
import jakarta.validation.constraints.*;

public record SubmitShoeRequest(
        @NotBlank String brand,
        @NotBlank String model,
        @NotBlank String colorway,
        @Min(1900) @Max(2100) int eraYear,
        @NotNull ConditionGrade proposedConditionGrade
) {}
