package com.thelineage.service;

import com.thelineage.domain.ConditionGrade;
import com.thelineage.domain.Shoe;
import com.thelineage.dto.shoe.SubmitShoeRequest;

import java.util.UUID;

public interface ShoeService {
    Shoe submit(UUID sellerUserId, SubmitShoeRequest request);
    Shoe authenticate(UUID shoeId, UUID curatorUserId, ConditionGrade grade, int rarityScore);
    Shoe findById(UUID shoeId);
}
