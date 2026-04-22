package com.thelineage.dto.listing;

import com.thelineage.domain.ConditionGrade;

public record ListingFilter(
        String brand,
        String colorway,
        Integer eraFrom,
        Integer eraTo,
        ConditionGrade condition,
        Integer minRarity,
        int page,
        int size
) {}
