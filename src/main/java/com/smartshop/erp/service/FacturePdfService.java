package com.smartshop.erp.service;

import com.smartshop.erp.entity.Facture;
import com.smartshop.erp.entity.Vente;

public interface FacturePdfService {
    /** Genere le PDF de la facture/recu de caisse pour la vente donnee. Retourne le chemin du fichier genere. */
    String genererPdfVente(Vente vente, Facture facture);
}
