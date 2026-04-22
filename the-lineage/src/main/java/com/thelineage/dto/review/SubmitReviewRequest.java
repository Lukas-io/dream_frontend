package com.thelineage.dto.review;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmitReviewRequest(
        @Valid @NotNull ReviewScores scores,
        @Size(max = 4000) String body
) {}
