package com.thelineage.dto.shoe;

import com.thelineage.domain.ConditionGrade;

import java.time.Instant;
import java.util.UUID;

public record ShoeDto(
        UUID id,
        String passportId,
        String brand,
        String model,
        String colorway,
        int eraYear,
        ConditionGrade conditionGrade,
        int rarityScore,
        boolean authenticated,
        Instant authenticatedAt
) {}
