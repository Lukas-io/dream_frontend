package com.thelineage.controller;

import com.thelineage.domain.User;
import com.thelineage.security.LineageUserPrincipal;
import com.thelineage.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Authenticated user's own profile")
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    public record ProfileDto(UUID id, String email, String displayName, String role, Instant createdAt) {}

    @GetMapping("/me")
    @Operation(summary = "Fetch the authenticated user's profile")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile returned."),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content)
    })
    public ProfileDto me(@AuthenticationPrincipal LineageUserPrincipal principal) {
        User user = users.getProfile(principal.id());
        return new ProfileDto(user.getId(), user.getEmail(), user.getDisplayName(),
                user.getRole().name(), user.getCreatedAt());
    }
}
