package com.smartshop.erp.controller;

import com.smartshop.erp.dto.request.ProduitRequest;
import com.smartshop.erp.dto.response.HistoriqueVenteProduitResponse;
import com.smartshop.erp.dto.response.ProduitResponse;
import com.smartshop.erp.service.ProduitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/produits")
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitService produitService;

    /** Liste des produits avec leur disponibilite en stock. ?idBoutique= pour filtrer sur une boutique. */
    @GetMapping
    public ResponseEntity<List<ProduitResponse>> lister(@RequestParam(required = false) Long idBoutique) {
        return ResponseEntity.ok(produitService.listerTous(idBoutique));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProduitResponse> obtenir(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.obtenirParId(id));
    }

    /**
     * Recherche par code-barres : utilisee lors du SCAN pour ajouter un produit au panier de vente.
     * Si le produit n'existe pas, renvoie 404 avec code "PRODUIT_INCONNU" et redirection="AJOUTER_PRODUIT"
     * (voir GlobalExceptionHandler) afin que le frontend redirige automatiquement vers le formulaire
     * de creation de produit, avec le code-barres scanne pre-rempli.
     */
    @GetMapping("/scan/{codeBarres}")
    public ResponseEntity<ProduitResponse> scanner(@PathVariable String codeBarres) {
        return ResponseEntity.ok(produitService.obtenirParCodeBarres(codeBarres));
    }

    @GetMapping("/recherche")
    public ResponseEntity<List<ProduitResponse>> rechercher(@RequestParam String q) {
        return ResponseEntity.ok(produitService.rechercher(q));
    }

    @GetMapping("/alertes")
    public ResponseEntity<List<ProduitResponse>> produitsEnAlerte(@RequestParam(required = false) Long idBoutique) {
        return ResponseEntity.ok(produitService.produitsEnAlerte(idBoutique));
    }

    /**
     * Historique de vente d'un produit precis, identifie par son code-barres, sur une periode
     * choisie librement par l'utilisateur.
     */
    @GetMapping("/scan/{codeBarres}/historique-ventes")
    public ResponseEntity<HistoriqueVenteProduitResponse> historiqueVentes(
            @PathVariable String codeBarres,
            @RequestParam(required = false) LocalDate dateDebut,
            @RequestParam(required = false) LocalDate dateFin) {

        LocalDateTime debut = (dateDebut != null ? dateDebut : LocalDate.now().minusMonths(1)).atStartOfDay();
        LocalDateTime fin = (dateFin != null ? dateFin : LocalDate.now()).atTime(LocalTime.MAX);
        return ResponseEntity.ok(produitService.historiqueVentes(codeBarres, debut, fin));
    }

    /** Creation d'un nouveau produit : prix d'achat + prix de vente obligatoires pour calculer le benefice. */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public ResponseEntity<ProduitResponse> creer(@Valid @RequestBody ProduitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produitService.creer(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public ResponseEntity<ProduitResponse> modifier(@PathVariable Long id, @Valid @RequestBody ProduitRequest request) {
        return ResponseEntity.ok(produitService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        produitService.desactiver(id);
        return ResponseEntity.noContent().build();
    }
}
