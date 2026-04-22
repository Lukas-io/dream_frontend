package com.thelineage.dto.listing;

import com.thelineage.domain.ProvenanceEventType;

import java.time.Instant;
import java.util.UUID;

public record ProvenanceRecordDto(
        UUID id,
        UUID actorUserId,
        ProvenanceEventType eventType,
        String payloadJson,
        Instant occurredAt
) {}
