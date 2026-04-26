package com.thelineage.dto.comment;

import java.time.Instant;
import java.util.UUID;

public record CommentDto(
        UUID id,
        UUID listingId,
        UUID parentId,
        UUID authorUserId,
        String body,
        boolean flagged,
        Instant createdAt
) {}
