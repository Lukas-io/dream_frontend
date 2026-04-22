package com.thelineage.dto.listing;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateListingRequest(
        @NotNull UUID shoeId,
        @NotNull @DecimalMin(value = "0.01") BigDecimal price,
        @NotBlank @Size(min = 3, max = 3) String currency
) {}
