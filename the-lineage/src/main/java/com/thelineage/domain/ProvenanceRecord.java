package com.thelineage.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "provenance_records")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProvenanceRecord {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shoe_id", nullable = false, updatable = false)
    private Shoe shoe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_user_id", nullable = false, updatable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, updatable = false)
    private ProvenanceEventType eventType;

    @Column(name = "payload_json", length = 4000, updatable = false)
    private String payloadJson;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @PrePersist
    void onCreate() {
        if (occurredAt == null) occurredAt = Instant.now();
    }
}
