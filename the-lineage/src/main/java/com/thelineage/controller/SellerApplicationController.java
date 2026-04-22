package com.thelineage.controller;

import com.thelineage.domain.SellerApplication;
import com.thelineage.dto.seller.ApplicationData;
import com.thelineage.security.LineageUserPrincipal;
import com.thelineage.service.SellerApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/sellers/applications")
public class SellerApplicationController {

    private final SellerApplicationService service;

    public SellerApplicationController(SellerApplicationService service) {
        this.service = service;
    }

    public record ApplicationDto(UUID id, UUID applicantId, String status, Instant submittedAt) {}

    @PostMapping
    public ResponseEntity<ApplicationDto> submit(
            @AuthenticationPrincipal LineageUserPrincipal principal,
            @Valid @RequestBody ApplicationData body
    ) {
        SellerApplication app = service.submit(principal.id(), body);
        return ResponseEntity.status(201).body(new ApplicationDto(
                app.getId(), app.getApplicant().getId(), app.getStatus().name(), app.getSubmittedAt()));
    }

    @GetMapping("/pending")
    public List<ApplicationDto> listPending() {
        return service.listPending().stream()
                .map(a -> new ApplicationDto(a.getId(), a.getApplicant().getId(), a.getStatus().name(), a.getSubmittedAt()))
                .toList();
    }
}
