package com.thelineage.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Buyer's post-transaction review of a completed order.")
public record SubmitReviewRequest(
        @Schema(description = "Multi-axis numeric scores.")
        @Valid @NotNull ReviewScores scores,

        @Schema(description = "Optional free-text review body.",
                example = "Pristine pair, packaged in archival paper. Communication was excellent.")
        @Size(max = 4000) String body
) {}
