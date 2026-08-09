package com.smartshop.erp.controller;

import com.smartshop.erp.dto.request.LoginRequest;
import com.smartshop.erp.dto.request.RefreshTokenRequest;
import com.smartshop.erp.dto.response.LoginResponse;
import com.smartshop.erp.security.CustomUserDetails;
import com.smartshop.erp.security.RateLimiter;
import com.smartshop.erp.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.smartshop.erp.exception.TropDeRequetesException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RateLimiter rateLimiter;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        verifierLimiteDebit(httpRequest);
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> rafraichir(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        verifierLimiteDebit(httpRequest);
        return ResponseEntity.ok(authService.rafraichir(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    /** Deconnecte toutes les sessions actives de l'utilisateur actuellement authentifie. */
    @PostMapping("/logout-toutes-sessions")
    public ResponseEntity<Void> logoutToutesLesSessions(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        authService.logoutToutesLesSessions(userDetails.getUtilisateur().getIdUtilisateur());
        return ResponseEntity.noContent().build();
    }

    private void verifierLimiteDebit(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        if (!rateLimiter.autoriser("auth:" + ip)) {
            throw new TropDeRequetesException("Trop de tentatives depuis cette adresse. Reessayez dans une minute.");
        }
    }
}
