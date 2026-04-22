package com.thelineage.service;

import com.thelineage.domain.SellerApplication;
import com.thelineage.domain.SellerTier;
import com.thelineage.dto.seller.ApplicationData;

import java.util.List;
import java.util.UUID;

public interface SellerApplicationService {
    SellerApplication submit(UUID applicantUserId, ApplicationData data);
    SellerApplication approve(UUID applicationId, UUID curatorUserId, SellerTier tier, String reviewerNote);
    SellerApplication reject(UUID applicationId, UUID curatorUserId, String reviewerNote);
    List<SellerApplication> listPending();
}
