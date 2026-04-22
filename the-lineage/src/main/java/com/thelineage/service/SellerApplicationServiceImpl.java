package com.thelineage.service;

import com.thelineage.domain.*;
import com.thelineage.dto.seller.ApplicationData;
import com.thelineage.exception.ConflictException;
import com.thelineage.exception.ForbiddenException;
import com.thelineage.exception.NotFoundException;
import com.thelineage.repository.SellerApplicationRepository;
import com.thelineage.repository.SellerProfileRepository;
import com.thelineage.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SellerApplicationServiceImpl implements SellerApplicationService {

    private final SellerApplicationRepository applications;
    private final SellerProfileRepository profiles;
    private final UserRepository users;
    private final NotificationService notifications;

    public SellerApplicationServiceImpl(SellerApplicationRepository applications,
                                        SellerProfileRepository profiles,
                                        UserRepository users,
                                        NotificationService notifications) {
        this.applications = applications;
        this.profiles = profiles;
        this.users = users;
        this.notifications = notifications;
    }

    @Override
    @Transactional
    public SellerApplication submit(UUID applicantUserId, ApplicationData data) {
        User applicant = users.findById(applicantUserId)
                .orElseThrow(() -> new NotFoundException("User not found: " + applicantUserId));
        applications.findFirstByApplicantIdAndStatus(applicantUserId, ApplicationStatus.PENDING)
                .ifPresent(existing -> { throw new ConflictException("Application already pending"); });
        SellerApplication application = SellerApplication.builder()
                .applicant(applicant)
                .status(ApplicationStatus.PENDING)
                .narrative(data.narrative())
                .referencesJson(data.referencesJson())
                .inventorySummary(data.inventorySummary())
                .build();
        SellerApplication saved = applications.save(application);
        List<User> curators = users.findAllByRole(UserRole.CURATOR);
        notifications.notifyAll(curators, NotificationType.APPLICATION_SUBMITTED,
                "{\"applicationId\":\"" + saved.getId() + "\"}");
        return saved;
    }

    @Override
    @Transactional
    public SellerApplication approve(UUID applicationId, UUID curatorUserId, SellerTier tier, String reviewerNote) {
        SellerApplication application = applications.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found: " + applicationId));
        User curator = users.findById(curatorUserId)
                .orElseThrow(() -> new NotFoundException("Curator not found: " + curatorUserId));
        if (curator.getRole() != UserRole.CURATOR && curator.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Only curators or admins can approve applications");
        }
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new ConflictException("Application is not pending");
        }
        application.setStatus(ApplicationStatus.APPROVED);
        application.setReviewer(curator);
        application.setReviewerNote(reviewerNote);
        application.setReviewedAt(Instant.now());

        User applicant = application.getApplicant();
        applicant.setRole(UserRole.SELLER);
        users.save(applicant);

        SellerProfile profile = profiles.findByUserId(applicant.getId()).orElseGet(() ->
                SellerProfile.builder()
                        .user(applicant)
                        .tier(tier)
                        .applicationStatus(ApplicationStatus.APPROVED)
                        .approvedAt(Instant.now())
                        .build()
        );
        profile.setTier(tier);
        profile.setApplicationStatus(ApplicationStatus.APPROVED);
        if (profile.getApprovedAt() == null) profile.setApprovedAt(Instant.now());
        profiles.save(profile);

        notifications.notify(applicant, NotificationType.APPLICATION_UPDATE,
                "{\"applicationId\":\"" + applicationId + "\",\"status\":\"APPROVED\"}");
        return applications.save(application);
    }

    @Override
    @Transactional
    public SellerApplication reject(UUID applicationId, UUID curatorUserId, String reviewerNote) {
        SellerApplication application = applications.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found: " + applicationId));
        User curator = users.findById(curatorUserId)
                .orElseThrow(() -> new NotFoundException("Curator not found: " + curatorUserId));
        if (curator.getRole() != UserRole.CURATOR && curator.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Only curators or admins can reject applications");
        }
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new ConflictException("Application is not pending");
        }
        application.setStatus(ApplicationStatus.REJECTED);
        application.setReviewer(curator);
        application.setReviewerNote(reviewerNote);
        application.setReviewedAt(Instant.now());
        notifications.notify(application.getApplicant(), NotificationType.APPLICATION_UPDATE,
                "{\"applicationId\":\"" + applicationId + "\",\"status\":\"REJECTED\"}");
        return applications.save(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerApplication> listPending() {
        return applications.findAllByStatus(ApplicationStatus.PENDING);
    }
}
