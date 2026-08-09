package com.smartshop.erp.service;

import com.smartshop.erp.dto.request.FournisseurRequest;
import com.smartshop.erp.entity.Fournisseur;

import java.util.List;

public interface FournisseurService {
    List<Fournisseur> lister();
    Fournisseur obtenir(Long id);
    Fournisseur creer(FournisseurRequest request);
    Fournisseur modifier(Long id, FournisseurRequest request);
    void desactiver(Long id);
}
