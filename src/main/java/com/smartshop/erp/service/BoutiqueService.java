package com.smartshop.erp.service;

import com.smartshop.erp.dto.request.BoutiqueRequest;
import com.smartshop.erp.entity.Boutique;

import java.util.List;

public interface BoutiqueService {
    List<Boutique> lister();
    Boutique obtenir(Long id);
    Boutique creer(BoutiqueRequest request);
    Boutique modifier(Long id, BoutiqueRequest request);
    void desactiver(Long id);
}
