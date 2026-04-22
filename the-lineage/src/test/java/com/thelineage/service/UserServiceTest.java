package com.thelineage.service;

import com.thelineage.domain.User;
import com.thelineage.domain.UserRole;
import com.thelineage.dto.auth.LoginRequest;
import com.thelineage.dto.auth.RegisterRequest;
import com.thelineage.dto.auth.TokenPair;
import com.thelineage.exception.ConflictException;
import com.thelineage.repository.UserRepository;
import com.thelineage.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository users;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwt;
    @InjectMocks private UserServiceImpl service;

    @Test
    void register_whenEmailIsNew_createsBuyer() {
        when(users.existsByEmail("new@x.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(users.save(any())).thenAnswer(inv -> inv.getArgument(0));
        User u = service.register(new RegisterRequest("new@x.com", "password123", "New User"));
        assertThat(u.getEmail()).isEqualTo("new@x.com");
        assertThat(u.getRole()).isEqualTo(UserRole.BUYER);
        assertThat(u.getPasswordHash()).isEqualTo("hashed");
        assertThat(u.isActive()).isTrue();
    }

    @Test
    void register_whenEmailExists_throwsConflict() {
        when(users.existsByEmail("dup@x.com")).thenReturn(true);
        assertThatThrownBy(() -> service.register(new RegisterRequest("dup@x.com", "password123", "Dup")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void login_withValidCredentials_returnsTokens() {
        User user = User.builder().id(UUID.randomUUID()).email("x@x.com")
                .passwordHash("hash").role(UserRole.BUYER).active(true).build();
        when(users.findByEmail("x@x.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pw12345678", "hash")).thenReturn(true);
        when(jwt.issueAccess(user)).thenReturn("access");
        when(jwt.issueRefresh(user)).thenReturn("refresh");
        TokenPair tp = service.login(new LoginRequest("x@x.com", "pw12345678"));
        assertThat(tp.accessToken()).isEqualTo("access");
        assertThat(tp.refreshToken()).isEqualTo("refresh");
    }

    @Test
    void login_withBadPassword_throwsBadCredentials() {
        User user = User.builder().id(UUID.randomUUID()).email("x@x.com").passwordHash("hash").role(UserRole.BUYER).active(true).build();
        when(users.findByEmail("x@x.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);
        assertThatThrownBy(() -> service.login(new LoginRequest("x@x.com", "bad")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_whenUserNotFound_throwsBadCredentials() {
        when(users.findByEmail("ghost@x.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.login(new LoginRequest("ghost@x.com", "pw")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void updateRole_changesRoleAndSaves() {
        UUID id = UUID.randomUUID();
        User user = User.builder().id(id).role(UserRole.BUYER).build();
        when(users.findById(id)).thenReturn(Optional.of(user));
        when(users.save(any())).thenAnswer(inv -> inv.getArgument(0));
        User result = service.updateRole(id, UserRole.SELLER);
        assertThat(result.getRole()).isEqualTo(UserRole.SELLER);
    }
}
