package com.smartshop.erp.controller;

import com.smartshop.erp.dto.request.RetourRequest;
import com.smartshop.erp.dto.response.RetourResponse;
import com.smartshop.erp.security.CustomUserDetails;
import com.smartshop.erp.service.RetourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Gestion des retours clients rattaches a une vente : remboursement pur, echange contre
 * des produits de meme valeur, ou echange contre des produits de valeur differente
 * (complement a payer par le client, ou difference remboursee).
 */
@RestController
@RequestMapping("/api/retours")
@RequiredArgsConstructor
public class RetourController {

    private final RetourService retourService;

    @PostMapping
    public ResponseEntity<RetourResponse> creer(@Valid @RequestBody RetourRequest request, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        RetourResponse reponse = retourService.creer(request, userDetails.getUtilisateur().getIdUtilisateur());
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RetourResponse> obtenir(@PathVariable Long id) {
        return ResponseEntity.ok(retourService.obtenir(id));
    }

    /** Tous les retours deja effectues sur une vente donnee (utile pour afficher ce qui est encore retournable). */
    @GetMapping("/vente/{idVente}")
    public ResponseEntity<List<RetourResponse>> parVente(@PathVariable Long idVente) {
        return ResponseEntity.ok(retourService.parVente(idVente));
    }

    @GetMapping("/historique")
    public ResponseEntity<List<RetourResponse>> historique(
            @RequestParam(required = false) LocalDate dateDebut,
            @RequestParam(required = false) LocalDate dateFin,
            @RequestParam(required = false) Long idBoutique) {

        LocalDateTime debut = (dateDebut != null ? dateDebut : LocalDate.now()).atStartOfDay();
        LocalDateTime fin = (dateFin != null ? dateFin : LocalDate.now()).atTime(LocalTime.MAX);
        return ResponseEntity.ok(retourService.historique(debut, fin, idBoutique));
    }

    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public ResponseEntity<Void> annuler(@PathVariable Long id) {
        retourService.annuler(id);
        return ResponseEntity.noContent().build();
    }
}
