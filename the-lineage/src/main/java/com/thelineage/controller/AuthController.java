package com.thelineage.controller;

import com.thelineage.domain.User;
import com.thelineage.dto.auth.LoginRequest;
import com.thelineage.dto.auth.RegisterRequest;
import com.thelineage.dto.auth.TokenPair;
import com.thelineage.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return ResponseEntity.status(201).body(Map.of(
                "id", user.getId().toString(),
                "email", user.getEmail(),
                "role", user.getRole().name()
        ));
    }

    @PostMapping("/login")
    public TokenPair login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }
}
