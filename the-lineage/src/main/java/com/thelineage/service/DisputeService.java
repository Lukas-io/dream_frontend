package com.thelineage.service;

import com.thelineage.domain.Dispute;
import com.thelineage.domain.DisputeStatus;

import java.util.List;
import java.util.UUID;

public interface DisputeService {
    Dispute open(UUID orderId, UUID raisedByUserId, String reason);
    Dispute resolve(UUID disputeId, UUID adminUserId, DisputeStatus outcome, String note);
    List<Dispute> findByOrder(UUID orderId);
}
