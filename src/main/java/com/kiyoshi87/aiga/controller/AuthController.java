package com.kiyoshi87.aiga.controller;

import com.kiyoshi87.aiga.auth.AuthService;
import com.kiyoshi87.aiga.auth.model.AuthRequestDto;
import com.kiyoshi87.aiga.auth.model.AuthResponseDto;
import com.kiyoshi87.aiga.auth.model.CurrentUserResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> registerUser(@Valid @RequestBody AuthRequestDto request) {
        return ResponseEntity.status(201)
                .body(authService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> loginUser(@Valid @RequestBody AuthRequestDto request) {
        return ResponseEntity.ok(authService.loginUser(request));
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponseDto> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(authService.getCurrentUser(jwt.getSubject()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser() {
        return ResponseEntity.noContent().build();
    }
}
