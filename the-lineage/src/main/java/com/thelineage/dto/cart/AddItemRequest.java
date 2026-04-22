package com.thelineage.dto.cart;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddItemRequest(@NotNull UUID listingId) {}
