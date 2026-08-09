package com.smartshop.erp.service;

import com.smartshop.erp.dto.request.CategorieRequest;
import com.smartshop.erp.entity.Categorie;

import java.util.List;

public interface CategorieService {
    List<Categorie> lister();
    Categorie creer(CategorieRequest request);
    Categorie modifier(Long id, CategorieRequest request);
    void supprimer(Long id);
}
