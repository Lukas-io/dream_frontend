package com.thelineage.dto.admin;

import com.thelineage.domain.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(@NotNull UserRole role) {}
