package com.smartshop.erp.controller;

import com.smartshop.erp.dto.request.MarqueRequest;
import com.smartshop.erp.entity.Marque;
import com.smartshop.erp.service.MarqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marques")
@RequiredArgsConstructor
public class MarqueController {

    private final MarqueService marqueService;

    @GetMapping
    public ResponseEntity<List<Marque>> lister() {
        return ResponseEntity.ok(marqueService.lister());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public ResponseEntity<Marque> creer(@Valid @RequestBody MarqueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(marqueService.creer(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public ResponseEntity<Marque> modifier(@PathVariable Long id, @Valid @RequestBody MarqueRequest request) {
        return ResponseEntity.ok(marqueService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        marqueService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
