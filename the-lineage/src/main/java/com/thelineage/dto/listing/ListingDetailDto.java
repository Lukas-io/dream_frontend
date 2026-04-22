package com.thelineage.dto.listing;

import java.util.List;

public record ListingDetailDto(
        ListingDto listing,
        List<ProvenanceRecordDto> passport
) {}
