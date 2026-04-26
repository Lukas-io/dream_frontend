package com.thelineage.service;

import com.thelineage.domain.SellerProfile;
import com.thelineage.exception.NotFoundException;
import com.thelineage.repository.SellerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SellerProfileServiceImpl implements SellerProfileService {

    private final SellerProfileRepository profiles;

    public SellerProfileServiceImpl(SellerProfileRepository profiles) {
        this.profiles = profiles;
    }

    @Override
    @Transactional(readOnly = true)
    public SellerProfile findById(UUID sellerProfileId) {
        return profiles.findById(sellerProfileId)
                .orElseThrow(() -> new NotFoundException("Seller profile not found: " + sellerProfileId));
    }
}
