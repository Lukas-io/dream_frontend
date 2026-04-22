package com.thelineage.dto.dispute;

import com.thelineage.domain.DisputeStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResolveDisputeRequest(
        @NotNull DisputeStatus outcome,
        @Size(max = 4000) String note
) {}
