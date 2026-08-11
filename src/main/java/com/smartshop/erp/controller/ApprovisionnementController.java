package com.smartshop.erp.controller;

import com.smartshop.erp.dto.request.ApprovisionnementRequest;
import com.smartshop.erp.dto.response.ApprovisionnementResponse;
import com.smartshop.erp.dto.response.LotActuelResponse;
import com.smartshop.erp.dto.response.LotStockResponse;
import com.smartshop.erp.security.CustomUserDetails;
import com.smartshop.erp.service.ApprovisionnementService;
import com.smartshop.erp.service.CoutMarchandiseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvisionnements")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','GERANT')")
public class ApprovisionnementController {

    private final ApprovisionnementService approvisionnementService;
    private final CoutMarchandiseService coutMarchandiseService;

    @GetMapping
    public ResponseEntity<List<ApprovisionnementResponse>> lister(@RequestParam(required = false) Long idBoutique) {
        return ResponseEntity.ok(approvisionnementService.lister(idBoutique));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApprovisionnementResponse> obtenir(@PathVariable Long id) {
        return ResponseEntity.ok(approvisionnementService.obtenir(id));
    }

    @PostMapping
    public ResponseEntity<ApprovisionnementResponse> creer(@Valid @RequestBody ApprovisionnementRequest request, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        ApprovisionnementResponse reponse = approvisionnementService.creer(request, userDetails.getUtilisateur().getIdUtilisateur());
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    /**
     * Le lot en train d'etre ecoule pour ce produit dans cette boutique (methode FIFO), avec
     * une alerte si le prix de vente actuel ne couvre plus ce cout d'achat. Ouvert aux vendeurs
     * (pas seulement admin/gerant) car c'est eux qui ajustent le prix au point de vente.
     */
    @GetMapping("/lot-actuel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LotActuelResponse> lotActuel(@RequestParam Long idProduit, @RequestParam Long idBoutique) {
        return ResponseEntity.ok(coutMarchandiseService.lotActuel(idProduit, idBoutique));
    }

    /** Historique complet des lots (epuises ou non) d'un produit dans une boutique. */
    @GetMapping("/lots")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LotStockResponse>> lots(@RequestParam Long idProduit, @RequestParam Long idBoutique) {
        return ResponseEntity.ok(coutMarchandiseService.lots(idProduit, idBoutique));
    }
}
