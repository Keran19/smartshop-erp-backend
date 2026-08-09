package com.smartshop.erp.service;

import com.smartshop.erp.entity.Vente;

import java.time.LocalDateTime;
import java.util.List;

public interface RapportVentesPdfService {
    /**
     * Genere un PDF listant toutes les ventes de la periode donnee (tableau recapitulatif
     * avec total, benefice) et retourne le chemin du fichier genere.
     */
    String genererPdfListeVentes(List<Vente> ventes, LocalDateTime debut, LocalDateTime fin, Long idBoutique);
}
