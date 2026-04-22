package com.thelineage.dto.cart;

import java.util.List;
import java.util.UUID;

public record CartDto(UUID id, UUID ownerId, List<CartItemDto> items) {}
