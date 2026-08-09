package com.smartshop.erp.controller;

import com.smartshop.erp.dto.request.AcompteRequest;
import com.smartshop.erp.dto.request.VersementAcompteRequest;
import com.smartshop.erp.dto.response.AcompteResponse;
import com.smartshop.erp.enums.StatutAcompte;
import com.smartshop.erp.security.CustomUserDetails;
import com.smartshop.erp.service.AcompteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gestion des acomptes clients. Avant de creer un acompte, le frontend doit verifier
 * l'existence du client via GET /api/clients/telephone/{telephone} : si absent (404,
 * code CLIENT_INCONNU), il doit rediriger vers la creation du client, puis reprendre
 * ce flux avec l'id_client obtenu.
 */
@RestController
@RequestMapping("/api/acomptes")
@RequiredArgsConstructor
public class AcompteController {

    private final AcompteService acompteService;

    @GetMapping
    public ResponseEntity<List<AcompteResponse>> lister(
            @RequestParam(required = false) StatutAcompte statut,
            @RequestParam(required = false) Long idBoutique) {
        return ResponseEntity.ok(acompteService.lister(statut, idBoutique));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AcompteResponse> obtenir(@PathVariable Long id) {
        return ResponseEntity.ok(acompteService.obtenir(id));
    }

    @GetMapping("/client/{idClient}")
    public ResponseEntity<List<AcompteResponse>> parClient(@PathVariable Long idClient) {
        return ResponseEntity.ok(acompteService.parClient(idClient));
    }

    @PostMapping
    public ResponseEntity<AcompteResponse> creer(@Valid @RequestBody AcompteRequest request, Authentication authentication) {
        Long idVendeur = idUtilisateurConnecte(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(acompteService.creer(request, idVendeur));
    }

    @PostMapping("/{id}/versements")
    public ResponseEntity<AcompteResponse> verser(@PathVariable Long id, @Valid @RequestBody VersementAcompteRequest request, Authentication authentication) {
        Long idUtilisateur = idUtilisateurConnecte(authentication);
        return ResponseEntity.ok(acompteService.enregistrerVersement(id, request, idUtilisateur));
    }

    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public ResponseEntity<Void> annuler(@PathVariable Long id) {
        acompteService.annuler(id);
        return ResponseEntity.noContent().build();
    }

    private Long idUtilisateurConnecte(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUtilisateur().getIdUtilisateur();
    }
}
