package com.thelineage.service;

import com.thelineage.domain.User;
import com.thelineage.domain.UserRole;
import com.thelineage.dto.auth.LoginRequest;
import com.thelineage.dto.auth.RegisterRequest;
import com.thelineage.dto.auth.TokenPair;

import java.util.UUID;

public interface UserService {
    User register(RegisterRequest request);
    TokenPair login(LoginRequest request);
    TokenPair refresh(String refreshToken);
    User getProfile(UUID userId);
    User updateRole(UUID userId, UserRole newRole);
}
