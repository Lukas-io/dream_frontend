package com.thelineage.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Listing {

    @Id
    @GeneratedValue
    private UUID id;

    // EAGER: ListingMapper renders shoe.passportId / brand / model / colorway /
    // eraYear into ListingDto after the service transaction closes (OSIV is off).
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "shoe_id", unique = true, nullable = false)
    private Shoe shoe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_profile_id", nullable = false)
    private SellerProfile seller;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingState state;

    @Column(name = "state_changed_at", nullable = false)
    private Instant stateChangedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (stateChangedAt == null) stateChangedAt = now;
        if (state == null) state = ListingState.AVAILABLE;
    }
}
