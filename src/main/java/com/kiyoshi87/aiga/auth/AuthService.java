package com.kiyoshi87.aiga.auth;

import com.kiyoshi87.aiga.auth.model.AuthRequestDto;
import com.kiyoshi87.aiga.auth.model.AuthResponseDto;
import com.kiyoshi87.aiga.auth.model.CurrentUserResponseDto;
import com.kiyoshi87.aiga.config.ConflictException;
import com.kiyoshi87.aiga.config.JwtProperties;
import com.kiyoshi87.aiga.model.entity.User;
import com.kiyoshi87.aiga.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    @Transactional
    public AuthResponseDto registerUser(AuthRequestDto request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists");
        }

        User user = userRepository.save(
                User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(request.password()))
                        .build()
        );

        return toAuthResponse(user);
    }

    public AuthResponseDto loginUser(AuthRequestDto request) {
        String email = normalizeEmail(request.email());
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, request.password()));
        return toAuthResponse(findUserByEmail(authentication.getName()));
    }

    @Transactional
    public CurrentUserResponseDto getCurrentUser(String email) {
        User user = findUserByEmail(email);
        return new CurrentUserResponseDto(user.getId(), user.getEmail());
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private AuthResponseDto toAuthResponse(User user) {
        return AuthResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .accessToken(jwtService.createAccessToken(user))
                .tokenType("Bearer")
                .expiresIn(jwtProperties.accessTokenTtl().toSeconds())
                .build();
    }
}
