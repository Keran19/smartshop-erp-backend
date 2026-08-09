package com.smartshop.erp.controller;

import com.smartshop.erp.dto.response.StatistiqueResponse;
import com.smartshop.erp.service.StatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;

/**
 * Statistiques de vente : chiffre d'affaires, nombre de ventes, benefice, nouveaux clients
 * et produit le plus vendu, sur une periode choisie librement par l'utilisateur, ou sur
 * un mois precis via le raccourci /mensuel.
 */
@RestController
@RequestMapping("/api/statistiques")
@RequiredArgsConstructor
public class StatistiqueController {

    private final StatistiqueService statistiqueService;

    /** Statistiques sur une periode libre choisie par l'utilisateur (dates au format ISO yyyy-MM-dd). */
    @GetMapping("/periode")
    public ResponseEntity<StatistiqueResponse> parPeriode(
            @RequestParam LocalDate dateDebut,
            @RequestParam LocalDate dateFin,
            @RequestParam(required = false) Long idBoutique) {

        LocalDateTime debut = dateDebut.atStartOfDay();
        LocalDateTime fin = dateFin.atTime(LocalTime.MAX);
        return ResponseEntity.ok(statistiqueService.calculer(debut, fin, idBoutique));
    }

    /** Raccourci : statistiques du mois choisi (annee + mois). Si non fournis, prend le mois courant. */
    @GetMapping("/mensuel")
    public ResponseEntity<StatistiqueResponse> mensuel(
            @RequestParam(required = false) Integer annee,
            @RequestParam(required = false) Integer mois,
            @RequestParam(required = false) Long idBoutique) {

        YearMonth ym = (annee != null && mois != null) ? YearMonth.of(annee, mois) : YearMonth.now();
        LocalDateTime debut = ym.atDay(1).atStartOfDay();
        LocalDateTime fin = ym.atEndOfMonth().atTime(LocalTime.MAX);
        return ResponseEntity.ok(statistiqueService.calculer(debut, fin, idBoutique));
    }

    /** Raccourci : statistiques du jour courant. */
    @GetMapping("/aujourdhui")
    public ResponseEntity<StatistiqueResponse> aujourdhui(@RequestParam(required = false) Long idBoutique) {
        LocalDateTime debut = LocalDate.now().atStartOfDay();
        LocalDateTime fin = LocalDate.now().atTime(LocalTime.MAX);
        return ResponseEntity.ok(statistiqueService.calculer(debut, fin, idBoutique));
    }
}
