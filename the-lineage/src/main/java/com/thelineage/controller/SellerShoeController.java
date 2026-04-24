package com.thelineage.controller;

import com.thelineage.domain.Shoe;
import com.thelineage.dto.shoe.ShoeDto;
import com.thelineage.dto.shoe.SubmitShoeRequest;
import com.thelineage.mapper.DomainMappers;
import com.thelineage.security.LineageUserPrincipal;
import com.thelineage.service.ShoeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/sellers/me/shoes")
@Tag(name = "Seller — Shoes", description = "Seller-side shoe submission")
public class SellerShoeController {

    private final ShoeService shoes;
    private final DomainMappers mappers;

    public SellerShoeController(ShoeService shoes, DomainMappers mappers) {
        this.shoes = shoes;
        this.mappers = mappers;
    }

    @PostMapping
    @Operation(summary = "Submit a shoe for authentication (creates a SUBMITTED provenance record)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Shoe submitted; awaits curator authentication."),
            @ApiResponse(responseCode = "400", description = "Validation failed.", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content),
            @ApiResponse(responseCode = "403",
                    description = "Caller is not a SELLER or seller application is not APPROVED.",
                    content = @Content)
    })
    public ResponseEntity<ShoeDto> submit(@AuthenticationPrincipal LineageUserPrincipal principal,
                                          @Valid @RequestBody SubmitShoeRequest body) {
        Shoe shoe = shoes.submit(principal.id(), body);
        return ResponseEntity.created(URI.create("/sellers/me/shoes/" + shoe.getId()))
                .body(mappers.toDto(shoe));
    }
}
