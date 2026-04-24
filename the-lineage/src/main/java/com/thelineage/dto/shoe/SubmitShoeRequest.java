package com.thelineage.dto.shoe;

import com.thelineage.domain.ConditionGrade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Seller submits a shoe for curator authentication. Creates a SUBMITTED provenance record.")
public record SubmitShoeRequest(
        @Schema(description = "Brand name (free text).", example = "Nike")
        @NotBlank String brand,

        @Schema(description = "Model name.", example = "Air Jordan 1")
        @NotBlank String model,

        @Schema(description = "Colorway nickname or descriptor.", example = "Chicago")
        @NotBlank String colorway,

        @Schema(description = "Year the shoe was originally released.", example = "1985", minimum = "1900", maximum = "2100")
        @Min(1900) @Max(2100) int eraYear,

        @Schema(description = "Seller's proposed condition grade. May be revised by the curator on authentication.",
                example = "EXCELLENT")
        @NotNull ConditionGrade proposedConditionGrade
) {}
