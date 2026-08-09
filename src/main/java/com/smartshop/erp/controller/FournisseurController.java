package com.smartshop.erp.controller;

import com.smartshop.erp.dto.request.FournisseurRequest;
import com.smartshop.erp.entity.Fournisseur;
import com.smartshop.erp.service.FournisseurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fournisseurs")
@RequiredArgsConstructor
public class FournisseurController {

    private final FournisseurService fournisseurService;

    @GetMapping
    public ResponseEntity<List<Fournisseur>> lister() {
        return ResponseEntity.ok(fournisseurService.lister());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fournisseur> obtenir(@PathVariable Long id) {
        return ResponseEntity.ok(fournisseurService.obtenir(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public ResponseEntity<Fournisseur> creer(@Valid @RequestBody FournisseurRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fournisseurService.creer(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public ResponseEntity<Fournisseur> modifier(@PathVariable Long id, @Valid @RequestBody FournisseurRequest request) {
        return ResponseEntity.ok(fournisseurService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        fournisseurService.desactiver(id);
        return ResponseEntity.noContent().build();
    }
}
