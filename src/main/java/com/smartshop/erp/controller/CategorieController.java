package com.smartshop.erp.controller;

import com.smartshop.erp.dto.request.CategorieRequest;
import com.smartshop.erp.entity.Categorie;
import com.smartshop.erp.service.CategorieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategorieController {

    private final CategorieService categorieService;

    @GetMapping
    public ResponseEntity<List<Categorie>> lister() {
        return ResponseEntity.ok(categorieService.lister());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public ResponseEntity<Categorie> creer(@Valid @RequestBody CategorieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categorieService.creer(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public ResponseEntity<Categorie> modifier(@PathVariable Long id, @Valid @RequestBody CategorieRequest request) {
        return ResponseEntity.ok(categorieService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        categorieService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
