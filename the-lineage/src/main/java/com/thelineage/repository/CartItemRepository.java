package com.thelineage.repository;

import com.thelineage.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    List<CartItem> findAllByExpiresAtBefore(Instant now);
}
