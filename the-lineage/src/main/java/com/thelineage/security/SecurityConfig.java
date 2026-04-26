package com.thelineage.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AccessDeniedHandler lineageAccessDeniedHandler() {
        return (request, response, ex) -> response.sendError(HttpServletResponse.SC_FORBIDDEN, ex.getMessage());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter,
                                           AccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // Spring Boot dispatches errors to /error; let the filter chain pass them through
                        // so the original status (404, 500, etc.) reaches the client instead of being
                        // overwritten by the catch-all .authenticated() rule.
                        .requestMatchers("/error").permitAll()
                        // public
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/", "/docs", "/scalar", "/scalar.html",
                                "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**",
                                "/webjars/**", "/favicon.ico").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/listings", "/listings/*",
                                "/listings/*/comments", "/listings/*/reviews",
                                "/sellers/*", "/sellers/*/reviews").permitAll()
                        // role-gated
                        .requestMatchers("/curator/**").hasAnyRole("CURATOR", "ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/listings").hasRole("SELLER")
                        .requestMatchers(HttpMethod.PUT, "/listings/*").hasRole("SELLER")
                        .requestMatchers(HttpMethod.DELETE, "/listings/*").hasRole("SELLER")
                        .requestMatchers("/sellers/me/**").hasRole("SELLER")
                        .requestMatchers("/cart/**", "/checkout").hasRole("BUYER")
                        .requestMatchers(HttpMethod.POST, "/disputes").hasRole("BUYER")
                        .requestMatchers(HttpMethod.POST, "/orders/*/reviews").hasRole("BUYER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
