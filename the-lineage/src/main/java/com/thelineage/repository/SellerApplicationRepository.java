package com.thelineage.repository;

import com.thelineage.domain.ApplicationStatus;
import com.thelineage.domain.SellerApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SellerApplicationRepository extends JpaRepository<SellerApplication, UUID> {
    Optional<SellerApplication> findFirstByApplicantIdAndStatus(UUID applicantId, ApplicationStatus status);
    List<SellerApplication> findAllByStatus(ApplicationStatus status);
}
