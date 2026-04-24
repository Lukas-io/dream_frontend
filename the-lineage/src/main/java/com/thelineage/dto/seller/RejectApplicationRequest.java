package com.thelineage.dto.seller;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Curator rejects a pending seller application.")
public record RejectApplicationRequest(
        @Schema(description = "Reason for rejection. Sent to the applicant.",
                example = "Insufficient verifiable transaction history; please reapply after building a public record.")
        @NotBlank @Size(max = 2000) String note
) {}
