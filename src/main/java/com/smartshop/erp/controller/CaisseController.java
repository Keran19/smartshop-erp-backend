package com.smartshop.erp.controller;

import com.smartshop.erp.dto.request.FermetureCaisseRequest;
import com.smartshop.erp.dto.request.OuvertureCaisseRequest;
import com.smartshop.erp.dto.request.ValidationEcartRequest;
import com.smartshop.erp.dto.response.MouvementCaisseLigne;
import com.smartshop.erp.dto.response.MouvementsCaisseResponse;
import com.smartshop.erp.dto.response.SessionCaisseAdminResponse;
import com.smartshop.erp.dto.response.SessionCaisseResponse;
import com.smartshop.erp.security.CustomUserDetails;
import com.smartshop.erp.service.CaisseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/caisse")
@RequiredArgsConstructor
public class CaisseController {

    private final CaisseService caisseService;

    /** Ouverture de caisse : declaration du fond de caisse par coupures, obligatoire, y compris a l'ouverture. */
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

    /** La session actuellement ouverte pour le vendeur connecte (404 si aucune n'est ouverte). Chaque vendeur a sa propre caisse. */
    @GetMapping("/ouverte")
    public ResponseEntity<SessionCaisseResponse> sessionOuverte(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(caisseService.sessionOuverteParVendeur(userDetails.getUtilisateur().getIdUtilisateur()));
    }

    /** Photo en temps reel des mouvements de la session (ventes, credits, retours, acomptes, depenses). */
    @GetMapping("/{idSession}/mouvements")
    public ResponseEntity<MouvementsCaisseResponse> mouvements(@PathVariable Long idSession) {
        return ResponseEntity.ok(caisseService.mouvements(idSession));
    }

    /** Journal detaille de la session : une ligne par operation (retour, remboursement, acompte, depense, vente). */
    @GetMapping("/{idSession}/journal")
    public ResponseEntity<List<MouvementCaisseLigne>> journal(@PathVariable Long idSession) {
        return ResponseEntity.ok(caisseService.journal(idSession));
    }

    /** Historique des sessions d'une boutique (toutes caisses confondues) - utile pour la vue admin. */
    @GetMapping("/historique")
    public ResponseEntity<List<SessionCaisseResponse>> historique(@RequestParam Long idBoutique) {
        return ResponseEntity.ok(caisseService.historiqueParBoutique(idBoutique));
    }

    /** Historique des sessions du vendeur connecte uniquement. */
    @GetMapping("/mon-historique")
    public ResponseEntity<List<SessionCaisseResponse>> monHistorique(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(caisseService.historiqueParVendeur(userDetails.getUtilisateur().getIdUtilisateur()));
    }

    // --- Tableau de bord admin "Gestion de caisse" ---

    /** Toutes les caisses (toutes boutiques, tous vendeurs) d'une journee donnee (aujourd'hui par defaut). */
    @GetMapping("/admin/sessions")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public ResponseEntity<List<SessionCaisseAdminResponse>> sessionsAdmin(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(caisseService.sessionsAdmin(date));
    }

    /** Valide l'ecart d'une caisse fermee, ou l'impute sur le salaire du vendeur. */
    @PostMapping("/{idSession}/valider-ecart")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public ResponseEntity<SessionCaisseAdminResponse> validerEcart(
            @PathVariable Long idSession, @Valid @RequestBody ValidationEcartRequest request, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(caisseService.validerEcart(idSession, request, userDetails.getUtilisateur().getIdUtilisateur()));
    }
}
