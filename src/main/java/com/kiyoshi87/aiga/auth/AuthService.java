package com.kiyoshi87.aiga.auth;

import com.kiyoshi87.aiga.auth.model.AuthRequestDto;
import com.kiyoshi87.aiga.auth.model.AuthResponseDto;
import com.kiyoshi87.aiga.config.ConflictException;
import com.kiyoshi87.aiga.model.entity.User;
import com.kiyoshi87.aiga.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    @Transactional
    public AuthResponseDto registerUser(AuthRequestDto request, HttpServletRequest servletRequest,
                                        HttpServletResponse servletResponse) {
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

        authenticateAndPersistSession(email, request.password(), servletRequest, servletResponse);
        return toResponse(user);
    }

    public AuthResponseDto loginUser(AuthRequestDto request, HttpServletRequest servletRequest,
                                     HttpServletResponse servletResponse) {
        String email = normalizeEmail(request.email());
        Authentication authentication = authenticateAndPersistSession(email, request.password(), servletRequest, servletResponse);
        return getCurrentUser(authentication.getName());
    }

    @Transactional
    public AuthResponseDto getCurrentUser(String email) {
        return userRepository.findByEmail(normalizeEmail(email))
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
    }

    public void logoutUser(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();
    }

    private Authentication authenticateAndPersistSession(String email, String password, HttpServletRequest request,
                                                         HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, password));

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);

        return authentication;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private AuthResponseDto toResponse(User user) {
        return AuthResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }
}
