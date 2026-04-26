package com.thelineage.service;

import com.thelineage.domain.SellerProfile;

import java.util.UUID;

public interface SellerProfileService {
    SellerProfile findById(UUID sellerProfileId);
}
