package com.smartshop.erp.controller;

import com.smartshop.erp.dto.request.ClientRequest;
import com.smartshop.erp.entity.Client;
import com.smartshop.erp.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Tout role authentifie peut consulter/creer des clients (necessaire au flux de vente et d'acompte). */
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<List<Client>> lister() {
        return ResponseEntity.ok(clientService.lister());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> obtenir(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.obtenir(id));
    }

    /** Recherche exacte par telephone : utilisee pour verifier l'existence d'un client avant de creer un acompte. */
    @GetMapping("/telephone/{telephone}")
    public ResponseEntity<Client> obtenirParTelephone(@PathVariable String telephone) {
        return ResponseEntity.ok(clientService.obtenirParTelephone(telephone));
    }

    @GetMapping("/recherche")
    public ResponseEntity<List<Client>> rechercher(@RequestParam String q) {
        return ResponseEntity.ok(clientService.rechercher(q));
    }

    @PostMapping
    public ResponseEntity<Client> creer(@Valid @RequestBody ClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.creer(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> modifier(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(clientService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERANT')")
    public ResponseEntity<Void> desactiver(@PathVariable Long id) {
        clientService.desactiver(id);
        return ResponseEntity.noContent().build();
    }
}
