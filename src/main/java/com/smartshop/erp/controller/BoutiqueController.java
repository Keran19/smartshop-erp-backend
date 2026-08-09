package com.smartshop.erp.controller;

import com.smartshop.erp.dto.request.BoutiqueRequest;
import com.smartshop.erp.entity.Boutique;
import com.smartshop.erp.service.BoutiqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Gestion des points de vente (boutiques). */
@RestController
@RequestMapping("/api/boutiques")
@RequiredArgsConstructor
public class BoutiqueController {

    private final BoutiqueService boutiqueService;

    @GetMapping
    public ResponseEntity<List<Boutique>> lister() {
        return ResponseEntity.ok(boutiqueService.lister());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Boutique> obtenir(@PathVariable Long id) {
        return ResponseEntity.ok(boutiqueService.obtenir(id));
    }

    /** Ajouter un nouveau point de vente. */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boutique> creer(@Valid @RequestBody BoutiqueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boutiqueService.creer(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Boutique> modifier(@PathVariable Long id, @Valid @RequestBody BoutiqueRequest request) {
        return ResponseEntity.ok(boutiqueService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        boutiqueService.desactiver(id);
        return ResponseEntity.noContent().build();
    }
}
