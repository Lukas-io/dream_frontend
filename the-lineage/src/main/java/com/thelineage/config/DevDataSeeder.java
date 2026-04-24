package com.thelineage.config;

import com.thelineage.domain.*;
import com.thelineage.repository.SellerProfileRepository;
import com.thelineage.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Seeds a known set of users when running with the 'local' profile so a new
 * developer can exercise every role (BUYER, SELLER, CURATOR, ADMIN) immediately
 * after boot without needing to open a SQL console.
 *
 * Idempotent: skips any user whose email already exists.
 */
@Component
@Profile("local")
class DevDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);
    private static final String DEV_PASSWORD = "password123";

    private final UserRepository users;
    private final SellerProfileRepository profiles;
    private final PasswordEncoder passwordEncoder;

    DevDataSeeder(UserRepository users, SellerProfileRepository profiles, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.profiles = profiles;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUser("admin@lineage.test", "House Admin", UserRole.ADMIN);
        seedUser("curator@lineage.test", "House Curator", UserRole.CURATOR);
        seedUser("buyer@lineage.test", "Buyer Example", UserRole.BUYER);
        User seller = seedUser("seller@lineage.test", "Approved Seller", UserRole.SELLER);
        if (seller != null) {
            profiles.findByUserId(seller.getId()).orElseGet(() ->
                    profiles.save(SellerProfile.builder()
                            .user(seller)
                            .tier(SellerTier.TIER_2)
                            .applicationStatus(ApplicationStatus.APPROVED)
                            .approvedAt(Instant.now())
                            .bio("Seed seller for local dev.")
                            .build())
            );
        }
        log.info("""
                Dev seed complete. Login credentials (password is '{}'):
                  admin@lineage.test    (ADMIN)
                  curator@lineage.test  (CURATOR)
                  seller@lineage.test   (SELLER, TIER_2, approved)
                  buyer@lineage.test    (BUYER)""",
                DEV_PASSWORD);
    }

    private User seedUser(String email, String displayName, UserRole role) {
        return users.findByEmail(email).orElseGet(() ->
                users.save(User.builder()
                        .email(email)
                        .passwordHash(passwordEncoder.encode(DEV_PASSWORD))
                        .displayName(displayName)
                        .role(role)
                        .active(true)
                        .build()));
    }
}
