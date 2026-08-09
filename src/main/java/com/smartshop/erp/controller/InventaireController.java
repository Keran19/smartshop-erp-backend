package com.smartshop.erp.controller;

import com.smartshop.erp.dto.request.InventaireRequest;
import com.smartshop.erp.dto.response.InventaireResponse;
import com.smartshop.erp.security.CustomUserDetails;
import com.smartshop.erp.service.InventaireService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventaires")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','GERANT')")
public class InventaireController {

    private final InventaireService inventaireService;

    @GetMapping
    public ResponseEntity<List<InventaireResponse>> lister(@RequestParam(required = false) Long idBoutique) {
        return ResponseEntity.ok(inventaireService.lister(idBoutique));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventaireResponse> obtenir(@PathVariable Long id) {
        return ResponseEntity.ok(inventaireService.obtenir(id));
    }

    @PostMapping
    public ResponseEntity<InventaireResponse> creer(@Valid @RequestBody InventaireRequest request, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        InventaireResponse reponse = inventaireService.creer(request, userDetails.getUtilisateur().getIdUtilisateur());
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }
}
