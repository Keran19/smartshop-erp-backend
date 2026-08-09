package com.smartshop.erp.controller;

import com.smartshop.erp.dto.request.DepenseRequest;
import com.smartshop.erp.dto.response.DepenseResponse;
import com.smartshop.erp.security.CustomUserDetails;
import com.smartshop.erp.service.DepenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/depenses")
@RequiredArgsConstructor
public class DepenseController {

    private final DepenseService depenseService;

    @GetMapping
    public ResponseEntity<List<DepenseResponse>> lister(
            @RequestParam(required = false) LocalDate dateDebut,
            @RequestParam(required = false) LocalDate dateFin,
            @RequestParam(required = false) Long idBoutique) {

        LocalDateTime debut = (dateDebut != null ? dateDebut : LocalDate.now()).atStartOfDay();
        LocalDateTime fin = (dateFin != null ? dateFin : LocalDate.now()).atTime(LocalTime.MAX);
        return ResponseEntity.ok(depenseService.lister(debut, fin, idBoutique));
    }

    @PostMapping
    public ResponseEntity<DepenseResponse> creer(@Valid @RequestBody DepenseRequest request, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        DepenseResponse reponse = depenseService.creer(request, userDetails.getUtilisateur().getIdUtilisateur());
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }
}
