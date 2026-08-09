package com.smartshop.erp.controller;

import com.smartshop.erp.dto.request.ChangerMotDePasseRequest;
import com.smartshop.erp.dto.request.ReinitialiserMotDePasseRequest;
import com.smartshop.erp.dto.request.UtilisateurCreationRequest;
import com.smartshop.erp.dto.request.UtilisateurModificationRequest;
import com.smartshop.erp.dto.response.UtilisateurResponse;
import com.smartshop.erp.security.CustomUserDetails;
import com.smartshop.erp.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gestion des comptes utilisateurs. La creation/modification/desactivation de comptes
 * est strictement reservee aux ADMIN (principe du moindre privilege pour un ERP).
 * Chaque utilisateur peut neanmoins consulter son propre profil et changer son propre
 * mot de passe via les routes /me.
 */
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UtilisateurResponse>> lister() {
        return ResponseEntity.ok(utilisateurService.lister());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponse> obtenir(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.obtenir(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponse> creer(@Valid @RequestBody UtilisateurCreationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.creer(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponse> modifier(@PathVariable Long id, @Valid @RequestBody UtilisateurModificationRequest request) {
        return ResponseEntity.ok(utilisateurService.modifier(id, request));
    }

    @PatchMapping("/{id}/desactiver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        utilisateurService.desactiver(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activer(@PathVariable Long id) {
        utilisateurService.activer(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deverrouiller")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deverrouiller(@PathVariable Long id) {
        utilisateurService.deverrouiller(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reinitialiser-mot-de-passe")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> reinitialiserMotDePasse(@PathVariable Long id, @Valid @RequestBody ReinitialiserMotDePasseRequest request) {
        utilisateurService.reinitialiserMotDePasse(id, request);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------
    // Self-service : chaque utilisateur authentifie gere son propre profil
    // ---------------------------------------------------------------

    @GetMapping("/me")
    public ResponseEntity<UtilisateurResponse> monProfil(Authentication authentication) {
        Long id = idDepuis(authentication);
        return ResponseEntity.ok(utilisateurService.obtenir(id));
    }

    @PostMapping("/me/changer-mot-de-passe")
    public ResponseEntity<Void> changerMonMotDePasse(Authentication authentication, @Valid @RequestBody ChangerMotDePasseRequest request) {
        Long id = idDepuis(authentication);
        utilisateurService.changerMotDePasse(id, request);
        return ResponseEntity.noContent().build();
    }

    private Long idDepuis(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUtilisateur().getIdUtilisateur();
    }
}
