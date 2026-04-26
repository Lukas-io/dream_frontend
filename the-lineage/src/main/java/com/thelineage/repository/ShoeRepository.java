package com.thelineage.repository;

import com.thelineage.domain.Shoe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShoeRepository extends JpaRepository<Shoe, UUID> {
}
