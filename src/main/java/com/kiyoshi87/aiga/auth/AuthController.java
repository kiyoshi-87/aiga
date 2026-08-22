package com.kiyoshi87.aiga.auth;

import com.kiyoshi87.aiga.auth.model.AuthRequestDto;
import com.kiyoshi87.aiga.auth.model.AuthResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> registerUser(@Valid @RequestBody AuthRequestDto request,
                                                         HttpServletRequest servletRequest,
                                                         HttpServletResponse servletResponse) {
        return ResponseEntity.status(201)
                .body(authService.registerUser(request, servletRequest, servletResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> loginUser(@Valid @RequestBody AuthRequestDto request,
                                                      HttpServletRequest servletRequest,
                                                      HttpServletResponse servletResponse) {
        return ResponseEntity.ok(authService.loginUser(request, servletRequest, servletResponse));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponseDto> getCurrentUser(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(authService.getCurrentUser(user.getUsername()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logoutUser(HttpServletRequest request) {
        authService.logoutUser(request);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }
}
