package com.thelineage.dto.dispute;

import com.thelineage.domain.DisputeStatus;

import java.time.Instant;
import java.util.UUID;

public record DisputeDto(
        UUID id,
        UUID orderId,
        UUID raisedByUserId,
        UUID resolverUserId,
        DisputeStatus status,
        String reason,
        String resolutionNote,
        Instant openedAt,
        Instant resolvedAt
) {}
