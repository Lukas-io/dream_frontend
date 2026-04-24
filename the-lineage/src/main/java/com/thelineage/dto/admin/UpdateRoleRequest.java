package com.thelineage.dto.admin;

import com.thelineage.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Admin override of a user's role. Use to mint the first CURATOR or another ADMIN.")
public record UpdateRoleRequest(
        @Schema(description = "New role to assign.", example = "CURATOR")
        @NotNull UserRole role
) {}
