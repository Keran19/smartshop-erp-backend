package com.smartshop.erp.controller;

import com.smartshop.erp.dto.request.PaiementCreditRequest;
import com.smartshop.erp.dto.response.CreditResponse;
import com.smartshop.erp.enums.StatutCredit;
import com.smartshop.erp.security.CustomUserDetails;
import com.smartshop.erp.service.CreditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credits")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;

    @GetMapping
    public ResponseEntity<List<CreditResponse>> lister(@RequestParam(required = false) StatutCredit statut) {
        return ResponseEntity.ok(creditService.lister(statut));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditResponse> obtenir(@PathVariable Long id) {
        return ResponseEntity.ok(creditService.obtenir(id));
    }

    @GetMapping("/client/{idClient}")
    public ResponseEntity<List<CreditResponse>> parClient(@PathVariable Long idClient) {
        return ResponseEntity.ok(creditService.parClient(idClient));
    }

    @PostMapping("/{id}/paiements")
    public ResponseEntity<CreditResponse> payer(@PathVariable Long id, @Valid @RequestBody PaiementCreditRequest request, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(creditService.enregistrerPaiement(id, request, userDetails.getUtilisateur().getIdUtilisateur()));
    }
}
