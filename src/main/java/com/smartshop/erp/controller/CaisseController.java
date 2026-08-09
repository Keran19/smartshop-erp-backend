package com.smartshop.erp.controller;

import com.smartshop.erp.dto.request.FermetureCaisseRequest;
import com.smartshop.erp.dto.request.OuvertureCaisseRequest;
import com.smartshop.erp.dto.response.SessionCaisseResponse;
import com.smartshop.erp.security.CustomUserDetails;
import com.smartshop.erp.service.CaisseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/caisse")
@RequiredArgsConstructor
public class CaisseController {

    private final CaisseService caisseService;

    /** Ouverture de caisse : declaration du fond de caisse par coupures (meme mecanisme qu'a la fermeture). */
    @PostMapping("/ouvrir")
    public ResponseEntity<SessionCaisseResponse> ouvrir(@Valid @RequestBody OuvertureCaisseRequest request, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        SessionCaisseResponse reponse = caisseService.ouvrir(request, userDetails.getUtilisateur().getIdUtilisateur());
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    /** Fermeture de caisse : declaration du montant compte par coupures ; l'ecart est calcule automatiquement. */
    @PostMapping("/{idSession}/fermer")
    public ResponseEntity<SessionCaisseResponse> fermer(@PathVariable Long idSession, @Valid @RequestBody FermetureCaisseRequest request) {
        return ResponseEntity.ok(caisseService.fermer(idSession, request));
    }

    @GetMapping("/{idSession}")
    public ResponseEntity<SessionCaisseResponse> obtenir(@PathVariable Long idSession) {
        return ResponseEntity.ok(caisseService.obtenir(idSession));
    }

    /** La session actuellement ouverte pour une boutique (404 si aucune n'est ouverte). */
    @GetMapping("/ouverte")
    public ResponseEntity<SessionCaisseResponse> sessionOuverte(@RequestParam Long idBoutique) {
        return ResponseEntity.ok(caisseService.sessionOuverte(idBoutique));
    }

    @GetMapping("/historique")
    public ResponseEntity<List<SessionCaisseResponse>> historique(@RequestParam Long idBoutique) {
        return ResponseEntity.ok(caisseService.historique(idBoutique));
    }
}
