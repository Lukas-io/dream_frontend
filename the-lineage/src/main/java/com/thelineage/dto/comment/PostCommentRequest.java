package com.thelineage.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Body of a new comment or reply on a listing.")
public record PostCommentRequest(
        @Schema(description = "Plain-text body. Markdown is not interpreted.",
                example = "Are these the original 1985 release or a retro?")
        @NotBlank @Size(max = 4000) String body
) {}
