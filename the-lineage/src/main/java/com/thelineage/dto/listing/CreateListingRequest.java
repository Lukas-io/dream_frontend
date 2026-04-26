package com.thelineage.dto.listing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Payload for a seller creating a listing for a previously authenticated shoe.")
public record CreateListingRequest(
        @Schema(description = "ID of an authenticated Shoe owned by the calling seller.",
                example = "f1a2b3c4-d5e6-7890-abcd-ef1234567890")
        @NotNull UUID shoeId,

        @Schema(description = "Sale price in the specified currency. Must be > 0.", example = "1200.00")
        @NotNull @DecimalMin(value = "0.01") BigDecimal price,

        @Schema(description = "ISO 4217 currency code (3 letters).", example = "USD")
        @NotBlank @Size(min = 3, max = 3) String currency
) {}
