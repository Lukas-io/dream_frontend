package com.thelineage.dto.seller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectApplicationRequest(@NotBlank @Size(max = 2000) String note) {}
