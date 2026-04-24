package com.thelineage.repository;

import com.thelineage.domain.ShippingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShippingRecordRepository extends JpaRepository<ShippingRecord, UUID> {
    Optional<ShippingRecord> findByOrderId(UUID orderId);
}
