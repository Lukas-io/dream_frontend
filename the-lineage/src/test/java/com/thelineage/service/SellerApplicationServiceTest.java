package com.thelineage.service;

import com.thelineage.domain.*;
import com.thelineage.dto.seller.ApplicationData;
import com.thelineage.exception.ConflictException;
import com.thelineage.exception.ForbiddenException;
import com.thelineage.repository.SellerApplicationRepository;
import com.thelineage.repository.SellerProfileRepository;
import com.thelineage.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerApplicationServiceTest {

    @Mock private SellerApplicationRepository applications;
    @Mock private SellerProfileRepository profiles;
    @Mock private UserRepository users;
    @Mock private NotificationService notifications;
    @InjectMocks private SellerApplicationServiceImpl service;

    @Test
    void submit_whenNoPending_createsPendingApplication() {
        UUID uid = UUID.randomUUID();
        User u = User.builder().id(uid).role(UserRole.BUYER).build();
        when(users.findById(uid)).thenReturn(Optional.of(u));
        when(applications.findFirstByApplicantIdAndStatus(uid, ApplicationStatus.PENDING)).thenReturn(Optional.empty());
        when(applications.save(any())).thenAnswer(inv -> {
            SellerApplication a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });
        when(users.findAllByRole(UserRole.CURATOR)).thenReturn(List.of());
        SellerApplication saved = service.submit(uid,
                new ApplicationData("x".repeat(40), "{}", "summary"));
        assertThat(saved.getStatus()).isEqualTo(ApplicationStatus.PENDING);
    }

    @Test
    void submit_whenPendingExists_throwsConflict() {
        UUID uid = UUID.randomUUID();
        when(users.findById(uid)).thenReturn(Optional.of(User.builder().id(uid).build()));
        when(applications.findFirstByApplicantIdAndStatus(uid, ApplicationStatus.PENDING))
                .thenReturn(Optional.of(new SellerApplication()));
        assertThatThrownBy(() -> service.submit(uid, new ApplicationData("x".repeat(40), null, null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void approve_byNonCurator_throwsForbidden() {
        UUID appId = UUID.randomUUID();
        UUID curatorId = UUID.randomUUID();
        when(applications.findById(appId)).thenReturn(Optional.of(
                SellerApplication.builder().status(ApplicationStatus.PENDING)
                        .applicant(User.builder().id(UUID.randomUUID()).build()).build()));
        when(users.findById(curatorId)).thenReturn(Optional.of(User.builder().id(curatorId).role(UserRole.BUYER).build()));
        assertThatThrownBy(() -> service.approve(appId, curatorId, SellerTier.TIER_1, "ok"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void approve_byCurator_promotesApplicantAndCreatesProfile() {
        UUID appId = UUID.randomUUID();
        UUID curatorId = UUID.randomUUID();
        UUID applicantId = UUID.randomUUID();
        User applicant = User.builder().id(applicantId).role(UserRole.BUYER).build();
        User curator = User.builder().id(curatorId).role(UserRole.CURATOR).build();
        SellerApplication app = SellerApplication.builder()
                .status(ApplicationStatus.PENDING).applicant(applicant).build();
        when(applications.findById(appId)).thenReturn(Optional.of(app));
        when(users.findById(curatorId)).thenReturn(Optional.of(curator));
        when(profiles.findByUserId(applicantId)).thenReturn(Optional.empty());
        when(profiles.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(applications.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(users.save(any())).thenAnswer(inv -> inv.getArgument(0));
        SellerApplication result = service.approve(appId, curatorId, SellerTier.TIER_2, "looks good");
        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(applicant.getRole()).isEqualTo(UserRole.SELLER);
        verify(profiles).save(any(SellerProfile.class));
    }

    @Test
    void reject_byCurator_marksRejected() {
        UUID appId = UUID.randomUUID();
        UUID curatorId = UUID.randomUUID();
        User curator = User.builder().id(curatorId).role(UserRole.CURATOR).build();
        SellerApplication app = SellerApplication.builder()
                .status(ApplicationStatus.PENDING)
                .applicant(User.builder().id(UUID.randomUUID()).build()).build();
        when(applications.findById(appId)).thenReturn(Optional.of(app));
        when(users.findById(curatorId)).thenReturn(Optional.of(curator));
        when(applications.save(any())).thenAnswer(inv -> inv.getArgument(0));
        SellerApplication result = service.reject(appId, curatorId, "no");
        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
    }

    @Test
    void listPending_delegatesToRepo() {
        when(applications.findAllByStatus(ApplicationStatus.PENDING)).thenReturn(List.of(new SellerApplication()));
        assertThat(service.listPending()).hasSize(1);
    }
}
