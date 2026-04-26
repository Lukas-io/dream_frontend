package com.thelineage.controller;

import com.thelineage.domain.Review;
import com.thelineage.dto.review.ReviewDto;
import com.thelineage.dto.review.SubmitReviewRequest;
import com.thelineage.mapper.DomainMappers;
import com.thelineage.security.LineageUserPrincipal;
import com.thelineage.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Reviews", description = "Post-transaction reviews of sellers")
public class ReviewController {

    private final ReviewService reviews;
    private final DomainMappers mappers;

    public ReviewController(ReviewService reviews, DomainMappers mappers) {
        this.reviews = reviews;
        this.mappers = mappers;
    }

    @GetMapping("/listings/{id}/reviews")
    @Operation(summary = "List reviews for a listing (public)")
    @SecurityRequirements
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Reviews for the listing.")})
    public List<ReviewDto> byListing(@PathVariable UUID id) {
        return reviews.findByListing(id).stream().map(mappers::toDto).toList();
    }

    @GetMapping("/sellers/{id}/reviews")
    @Operation(summary = "List reviews for a seller profile (public)")
    @SecurityRequirements
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Reviews for the seller.")})
    public List<ReviewDto> bySeller(@PathVariable UUID id) {
        return reviews.findBySeller(id).stream().map(mappers::toDto).toList();
    }

    @PostMapping("/orders/{id}/reviews")
    @Operation(summary = "Buyer submits a review for a COMPLETED order — one per order")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review created."),
            @ApiResponse(responseCode = "400", description = "Validation failed.", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not the buyer of this order.",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found.", content = @Content),
            @ApiResponse(responseCode = "409",
                    description = "Order is not COMPLETED, or a review already exists for it.",
                    content = @Content)
    })
    public ReviewDto submit(@AuthenticationPrincipal LineageUserPrincipal principal,
                            @PathVariable UUID id,
                            @Valid @RequestBody SubmitReviewRequest body) {
        Review r = reviews.submit(id, principal.id(), body.scores(), body.body());
        return mappers.toDto(r);
    }
}
