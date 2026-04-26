package com.thelineage.mapper;

import com.thelineage.domain.Listing;
import com.thelineage.domain.ProvenanceRecord;
import com.thelineage.dto.listing.ListingDto;
import com.thelineage.dto.listing.ProvenanceRecordDto;
import org.springframework.stereotype.Component;

@Component
public class ListingMapper {

    public ListingDto toDto(Listing l) {
        return new ListingDto(
                l.getId(),
                l.getShoe().getId(),
                l.getShoe().getPassportId(),
                l.getShoe().getBrand(),
                l.getShoe().getModel(),
                l.getShoe().getColorway(),
                l.getShoe().getEraYear(),
                l.getPrice(),
                l.getCurrency(),
                l.getState(),
                l.getCreatedAt()
        );
    }

    public ProvenanceRecordDto toDto(ProvenanceRecord r) {
        return new ProvenanceRecordDto(
                r.getId(),
                r.getActor().getId(),
                r.getEventType(),
                r.getPayloadJson(),
                r.getOccurredAt()
        );
    }
}
