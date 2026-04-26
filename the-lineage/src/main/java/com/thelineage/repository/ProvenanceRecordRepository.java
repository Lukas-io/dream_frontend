package com.thelineage.repository;

import com.thelineage.domain.ProvenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProvenanceRecordRepository extends JpaRepository<ProvenanceRecord, UUID> {
    List<ProvenanceRecord> findByShoeIdOrderByOccurredAtAsc(UUID shoeId);
}
