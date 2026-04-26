package com.thelineage.service;

import com.thelineage.domain.ProvenanceEventType;
import com.thelineage.domain.ProvenanceRecord;

import java.util.List;
import java.util.UUID;

public interface ProvenanceService {

    ProvenanceRecord append(UUID shoeId, UUID actorUserId, ProvenanceEventType eventType, String payloadJson);

    List<ProvenanceRecord> chainFor(UUID shoeId);
}
