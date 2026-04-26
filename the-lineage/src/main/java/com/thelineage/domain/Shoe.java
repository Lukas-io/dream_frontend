package com.thelineage.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shoe {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_profile_id", nullable = false)
    private SellerProfile seller;

    @Column(name = "passport_id", nullable = false, unique = true)
    private String passportId;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String colorway;

    @Column(name = "era_year", nullable = false)
    private int eraYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_grade", nullable = false)
    private ConditionGrade conditionGrade;

    @Column(name = "rarity_score", nullable = false)
    private int rarityScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authenticated_by_curator_id")
    private User authenticatedByCurator;

    @Column(name = "authenticated_at")
    private Instant authenticatedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (passportId == null) passportId = "LNG-" + UUID.randomUUID();
    }

    public boolean isAuthenticated() {
        return authenticatedAt != null && authenticatedByCurator != null;
    }
}
