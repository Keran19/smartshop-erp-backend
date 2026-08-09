package com.smartshop.erp.service;

import com.smartshop.erp.dto.response.StatistiqueResponse;

import java.time.LocalDateTime;

public interface StatistiqueService {

    /**
     * Calcule toutes les statistiques (CA, nb ventes, benefice, nouveaux clients, produit le plus
     * vendu) sur une periode choisie par l'utilisateur, avec filtre boutique optionnel.
     */
    StatistiqueResponse calculer(LocalDateTime debut, LocalDateTime fin, Long idBoutique);
}
