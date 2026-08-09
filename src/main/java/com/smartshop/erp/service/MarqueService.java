package com.smartshop.erp.service;

import com.smartshop.erp.dto.request.MarqueRequest;
import com.smartshop.erp.entity.Marque;

import java.util.List;

public interface MarqueService {
    List<Marque> lister();
    Marque creer(MarqueRequest request);
    Marque modifier(Long id, MarqueRequest request);
    void supprimer(Long id);
}
