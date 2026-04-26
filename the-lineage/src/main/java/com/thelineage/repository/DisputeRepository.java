package com.thelineage.repository;

import com.thelineage.domain.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DisputeRepository extends JpaRepository<Dispute, UUID> {
    List<Dispute> findAllByOrderId(UUID orderId);
}
