package com.thelineage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thelineage.domain.*;
import com.thelineage.dto.auth.LoginRequest;
import com.thelineage.dto.auth.TokenPair;
import com.thelineage.dto.listing.CreateListingRequest;
import com.thelineage.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@DisplayName("ListingIntegrationTest — full stack create-listing flow")
class ListingIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("lineage_test")
            .withUsername("lineage")
            .withPassword("lineage");

    static { POSTGRES.start(); }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry reg) {
        reg.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        reg.add("spring.datasource.username", POSTGRES::getUsername);
        reg.add("spring.datasource.password", POSTGRES::getPassword);
        reg.add("lineage.jwt.secret", () -> "test-secret-test-secret-test-secret-test-secret");
    }

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;
    @Autowired private UserRepository users;
    @Autowired private SellerProfileRepository profiles;
    @Autowired private ShoeRepository shoes;
    @Autowired private ListingRepository listings;
    @Autowired private ProvenanceRecordRepository provenance;
    @Autowired private PasswordEncoder passwordEncoder;

    private User approvedSellerUser;
    private SellerProfile approvedSellerProfile;
    private User pendingSellerUser;
    private SellerProfile pendingSellerProfile;
    private User curator;
    private Shoe authenticatedShoe;

    @BeforeEach
    void seed() {
        provenance.deleteAll();
        listings.deleteAll();
        shoes.deleteAll();
        profiles.deleteAll();
        users.deleteAll();

        approvedSellerUser = users.save(User.builder()
                .email("seller@lineage.test")
                .passwordHash(passwordEncoder.encode("password123"))
                .displayName("Approved Seller")
                .role(UserRole.SELLER)
                .active(true)
                .build());
        approvedSellerProfile = profiles.save(SellerProfile.builder()
                .user(approvedSellerUser)
                .tier(SellerTier.TIER_2)
                .applicationStatus(ApplicationStatus.APPROVED)
                .approvedAt(Instant.now())
                .build());

        pendingSellerUser = users.save(User.builder()
                .email("pending@lineage.test")
                .passwordHash(passwordEncoder.encode("password123"))
                .displayName("Pending Seller")
                .role(UserRole.SELLER)
                .active(true)
                .build());
        pendingSellerProfile = profiles.save(SellerProfile.builder()
                .user(pendingSellerUser)
                .tier(SellerTier.TIER_1)
                .applicationStatus(ApplicationStatus.PENDING)
                .build());

        curator = users.save(User.builder()
                .email("curator@lineage.test")
                .passwordHash(passwordEncoder.encode("password123"))
                .displayName("House Curator")
                .role(UserRole.CURATOR)
                .active(true)
                .build());

        users.save(User.builder()
                .email("admin@lineage.test")
                .passwordHash(passwordEncoder.encode("password123"))
                .displayName("Admin")
                .role(UserRole.ADMIN)
                .active(true)
                .build());

        authenticatedShoe = shoes.save(Shoe.builder()
                .seller(approvedSellerProfile)
                .brand("Nike")
                .model("Air Jordan 1")
                .colorway("Chicago")
                .eraYear(1985)
                .conditionGrade(ConditionGrade.EXCELLENT)
                .rarityScore(92)
                .authenticatedByCurator(curator)
                .authenticatedAt(Instant.now())
                .build());
    }

    private String accessToken(String email) throws Exception {
        String body = json.writeValueAsString(new LoginRequest(email, "password123"));
        String response = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readValue(response, TokenPair.class).accessToken();
    }

    @Test
    @DisplayName("createListing_withValidPayload_returns201AndListingWithPassport")
    void createListing_withValidPayload_returns201AndListingWithPassport() throws Exception {
        String token = accessToken("seller@lineage.test");
        String body = json.writeValueAsString(new CreateListingRequest(
                authenticatedShoe.getId(), new BigDecimal("1200.00"), "USD"));

        String response = mvc.perform(post("/listings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.state").value("AVAILABLE"))
                .andExpect(jsonPath("$.price").value(1200.00))
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> dto = json.readValue(response, Map.class);
        UUID listingId = UUID.fromString((String) dto.get("id"));

        Listing persisted = listings.findById(listingId).orElseThrow();
        assertThat(persisted.getState()).isEqualTo(ListingState.AVAILABLE);
        assertThat(persisted.getSeller().getId()).isEqualTo(approvedSellerProfile.getId());

        List<ProvenanceRecord> chain = provenance.findByShoeIdOrderByOccurredAtAsc(authenticatedShoe.getId());
        assertThat(chain).hasSize(1);
        assertThat(chain.get(0).getEventType()).isEqualTo(ProvenanceEventType.LISTED);
        assertThat(chain.get(0).getActor().getId()).isEqualTo(approvedSellerUser.getId());

        mvc.perform(get("/listings/" + listingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listing.id").value(listingId.toString()))
                .andExpect(jsonPath("$.passport.length()").value(1));
    }

    @Test
    @DisplayName("createListing_withUnapprovedSeller_returns403")
    void createListing_withUnapprovedSeller_returns403() throws Exception {
        Shoe pendingSellerShoe = shoes.save(Shoe.builder()
                .seller(pendingSellerProfile)
                .brand("Adidas")
                .model("Samba")
                .colorway("White/Black")
                .eraYear(2001)
                .conditionGrade(ConditionGrade.GOOD)
                .rarityScore(40)
                .authenticatedByCurator(curator)
                .authenticatedAt(Instant.now())
                .build());
        String token = accessToken("pending@lineage.test");
        String body = json.writeValueAsString(new CreateListingRequest(
                pendingSellerShoe.getId(), new BigDecimal("300.00"), "USD"));

        mvc.perform(post("/listings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("createListing_withMissingRequiredFields_returns400")
    void createListing_withMissingRequiredFields_returns400() throws Exception {
        String token = accessToken("seller@lineage.test");
        String body = "{}";
        mvc.perform(post("/listings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    @DisplayName("createListing_withoutAuth_returns401")
    void createListing_withoutAuth_returns401() throws Exception {
        String body = json.writeValueAsString(new CreateListingRequest(
                authenticatedShoe.getId(), new BigDecimal("1200.00"), "USD"));
        mvc.perform(post("/listings")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("createListing_thenFetchPassport_passportChainIsCorrect")
    void createListing_thenFetchPassport_passportChainIsCorrect() throws Exception {
        String token = accessToken("seller@lineage.test");
        String body = json.writeValueAsString(new CreateListingRequest(
                authenticatedShoe.getId(), new BigDecimal("900.00"), "USD"));
        String response = mvc.perform(post("/listings")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID listingId = UUID.fromString((String) json.readValue(response, Map.class).get("id"));

        mvc.perform(get("/listings/" + listingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passport.length()").value(1))
                .andExpect(jsonPath("$.passport[0].eventType").value("LISTED"))
                .andExpect(jsonPath("$.passport[0].actorUserId").value(approvedSellerUser.getId().toString()));
    }
}
