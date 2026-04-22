package com.thelineage.service;

import com.thelineage.domain.User;
import com.thelineage.domain.UserRole;
import com.thelineage.dto.auth.LoginRequest;
import com.thelineage.dto.auth.RegisterRequest;
import com.thelineage.dto.auth.TokenPair;
import com.thelineage.exception.ConflictException;
import com.thelineage.exception.NotFoundException;
import com.thelineage.repository.UserRepository;
import com.thelineage.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwt;

    public UserServiceImpl(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwt) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwt = jwt;
    }

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        if (users.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered");
        }
        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .displayName(request.displayName())
                .role(UserRole.BUYER)
                .active(true)
                .build();
        return users.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public TokenPair login(LoginRequest request) {
        User user = users.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!user.isActive() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return new TokenPair(jwt.issueAccess(user), jwt.issueRefresh(user));
    }

    @Override
    @Transactional(readOnly = true)
    public User getProfile(UUID userId) {
        return users.findById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    @Override
    @Transactional
    public User updateRole(UUID userId, UserRole newRole) {
        User user = getProfile(userId);
        user.setRole(newRole);
        return users.save(user);
    }
}
