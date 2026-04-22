package com.thelineage.service;

import com.thelineage.domain.*;
import com.thelineage.dto.shoe.SubmitShoeRequest;
import com.thelineage.exception.ConflictException;
import com.thelineage.exception.ForbiddenException;
import com.thelineage.exception.NotFoundException;
import com.thelineage.repository.SellerProfileRepository;
import com.thelineage.repository.ShoeRepository;
import com.thelineage.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ShoeServiceImpl implements ShoeService {

    private final ShoeRepository shoes;
    private final SellerProfileRepository profiles;
    private final UserRepository users;
    private final ProvenanceService provenance;

    public ShoeServiceImpl(ShoeRepository shoes,
                           SellerProfileRepository profiles,
                           UserRepository users,
                           ProvenanceService provenance) {
        this.shoes = shoes;
        this.profiles = profiles;
        this.users = users;
        this.provenance = provenance;
    }

    @Override
    @Transactional
    public Shoe submit(UUID sellerUserId, SubmitShoeRequest request) {
        SellerProfile profile = profiles.findByUserId(sellerUserId)
                .orElseThrow(() -> new ForbiddenException("Seller profile not found"));
        if (profile.getApplicationStatus() != ApplicationStatus.APPROVED) {
            throw new ForbiddenException("Seller application not approved");
        }
        Shoe shoe = shoes.save(Shoe.builder()
                .seller(profile)
                .brand(request.brand())
                .model(request.model())
                .colorway(request.colorway())
                .eraYear(request.eraYear())
                .conditionGrade(request.proposedConditionGrade())
                .rarityScore(0)
                .build());
        provenance.append(shoe.getId(), sellerUserId, ProvenanceEventType.SUBMITTED,
                "{\"brand\":\"" + request.brand() + "\"}");
        return shoe;
    }

    @Override
    @Transactional
    public Shoe authenticate(UUID shoeId, UUID curatorUserId, ConditionGrade grade, int rarityScore) {
        Shoe shoe = shoes.findById(shoeId)
                .orElseThrow(() -> new NotFoundException("Shoe not found: " + shoeId));
        User curator = users.findById(curatorUserId)
                .orElseThrow(() -> new NotFoundException("Curator not found: " + curatorUserId));
        if (curator.getRole() != UserRole.CURATOR && curator.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Only curators or admins can authenticate shoes");
        }
        if (shoe.isAuthenticated()) {
            throw new ConflictException("Shoe already authenticated");
        }
        shoe.setConditionGrade(grade);
        shoe.setRarityScore(rarityScore);
        shoe.setAuthenticatedByCurator(curator);
        shoe.setAuthenticatedAt(Instant.now());
        Shoe saved = shoes.save(shoe);
        provenance.append(shoeId, curatorUserId, ProvenanceEventType.AUTHENTICATED,
                "{\"grade\":\"" + grade + "\",\"rarity\":" + rarityScore + "}");
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Shoe findById(UUID shoeId) {
        return shoes.findById(shoeId)
                .orElseThrow(() -> new NotFoundException("Shoe not found: " + shoeId));
    }
}
