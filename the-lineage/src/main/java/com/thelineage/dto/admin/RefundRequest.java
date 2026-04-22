package com.thelineage.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record RefundRequest(@NotBlank String reason) {}
