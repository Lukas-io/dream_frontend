package com.thelineage.controller;

import com.thelineage.domain.User;
import com.thelineage.dto.auth.LoginRequest;
import com.thelineage.dto.auth.RefreshRequest;
import com.thelineage.dto.auth.RegisterRequest;
import com.thelineage.dto.auth.TokenPair;
import com.thelineage.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Account creation and JWT issuance — start here")
@SecurityRequirements
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register a new buyer account",
            description = "Creates an account with role BUYER. To become a SELLER, register here, " +
                    "then submit a seller application via POST /sellers/applications. " +
                    "CURATOR and ADMIN roles are assigned by an existing admin via PUT /admin/users/{id}/role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created."),
            @ApiResponse(responseCode = "400", description = "Validation failed (bad email format, password < 8 chars, etc.).",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Email already registered.", content = @Content)
    })
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return ResponseEntity.status(201).body(Map.of(
                "id", user.getId().toString(),
                "email", user.getEmail(),
                "role", user.getRole().name()
        ));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Log in and receive a JWT token pair",
            description = "Returns an access token (short-lived) and a refresh token (longer-lived). " +
                    "Send the access token as `Authorization: Bearer <token>` on every protected request. " +
                    "In Scalar, click the lock icon and paste the access token to use Try-It."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful."),
            @ApiResponse(responseCode = "400", description = "Validation failed.", content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or inactive account.",
                    content = @Content)
    })
    public TokenPair login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Exchange a refresh token for a new access + refresh token pair",
            description = "Use when the access token has expired (default lifetime 15 minutes). " +
                    "The refresh token itself is also rotated."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New token pair issued."),
            @ApiResponse(responseCode = "400", description = "Validation failed.", content = @Content),
            @ApiResponse(responseCode = "401", description = "Refresh token is invalid, expired, or the account is inactive.",
                    content = @Content)
    })
    public TokenPair refresh(@Valid @RequestBody RefreshRequest request) {
        return userService.refresh(request.refreshToken());
    }
}
