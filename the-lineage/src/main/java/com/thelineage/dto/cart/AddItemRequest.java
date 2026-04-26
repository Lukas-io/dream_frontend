package com.thelineage.dto.cart;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Add a listing to the buyer's cart by id.")
public record AddItemRequest(
        @Schema(description = "ID of an AVAILABLE listing.",
                example = "f1a2b3c4-d5e6-7890-abcd-ef1234567890")
        @NotNull UUID listingId
) {}
