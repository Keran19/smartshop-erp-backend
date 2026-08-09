package com.smartshop.erp.controller;

import com.smartshop.erp.dto.request.VenteRequest;
import com.smartshop.erp.dto.response.VenteResponse;
import com.smartshop.erp.security.CustomUserDetails;
import com.smartshop.erp.service.VenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/ventes")
@RequiredArgsConstructor
public class VenteController {

    private final VenteService venteService;

    /**
     * Etape 1 du flux de caisse : calcule l'apercu de la vente (totaux, monnaie a rendre)
     * SANS RIEN ENREGISTRER. C'est l'ecran de "confirmation avant impression" demande :
     * le caissier scanne son panier, saisit le montant recu, voit le recapitulatif complet,
     * puis clique sur "confirmer" -> appel a POST /api/ventes pour valider definitivement.
     */
    @PostMapping("/apercu")
    public ResponseEntity<VenteResponse> apercu(@Valid @RequestBody VenteRequest request, Authentication authentication) {
        Long idVendeur = idVendeurConnecte(authentication);
        return ResponseEntity.ok(venteService.apercu(request, idVendeur));
    }

    /** Etape 2 : validation definitive de la vente (deduction du stock, creation facture/credit). */
    @PostMapping
    public ResponseEntity<VenteResponse> valider(@Valid @RequestBody VenteRequest request, Authentication authentication) {
        Long idVendeur = idVendeurConnecte(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(venteService.valider(request, idVendeur));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenteResponse> obtenir(@PathVariable Long id) {
        return ResponseEntity.ok(venteService.obtenir(id));
    }

    /**
     * Historique des ventes sur une periode choisie par l'utilisateur.
     * Parametres au format ISO (yyyy-MM-dd). Si non fournis, prend le jour courant.
     * Chaque vente renvoyee inclut son benefice (voir VenteResponse.benefice et lignes[].benefice).
     */
    @GetMapping("/historique")
    public ResponseEntity<List<VenteResponse>> historique(
            @RequestParam(required = false) LocalDate dateDebut,
            @RequestParam(required = false) LocalDate dateFin,
            @RequestParam(required = false) Long idBoutique) {

        LocalDateTime debut = (dateDebut != null ? dateDebut : LocalDate.now()).atStartOfDay();
        LocalDateTime fin = (dateFin != null ? dateFin : LocalDate.now()).atTime(LocalTime.MAX);

        return ResponseEntity.ok(venteService.historique(debut, fin, idBoutique));
    }

    /**
     * Genere et telecharge un PDF listant toutes les ventes de la periode choisie
     * (bouton "Exporter en PDF" de la liste des ventes).
     */
    @GetMapping("/historique/pdf")
    public ResponseEntity<Resource> historiquePdf(
            @RequestParam(required = false) LocalDate dateDebut,
            @RequestParam(required = false) LocalDate dateFin,
            @RequestParam(required = false) Long idBoutique) {

        LocalDateTime debut = (dateDebut != null ? dateDebut : LocalDate.now()).atStartOfDay();
        LocalDateTime fin = (dateFin != null ? dateFin : LocalDate.now()).atTime(LocalTime.MAX);

        String chemin = venteService.genererRapportPdf(debut, fin, idBoutique);
        File fichier = new File(chemin);
        Resource resource = new FileSystemResource(fichier);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fichier.getName() + "\"")
                .body(resource);
    }

    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public ResponseEntity<Void> annuler(@PathVariable Long id) {
        venteService.annuler(id);
        return ResponseEntity.noContent().build();
    }

    /** Bouton "Imprimer en PDF" dans la liste des ventes : genere/telecharge la facture au format PDF. */
    @GetMapping("/{id}/imprimer")
    public ResponseEntity<Resource> imprimer(@PathVariable Long id) {
        String chemin = venteService.imprimer(id);
        File fichier = new File(chemin);
        Resource resource = new FileSystemResource(fichier);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fichier.getName() + "\"")
                .body(resource);
    }

    private Long idVendeurConnecte(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUtilisateur().getIdUtilisateur();
    }
}
