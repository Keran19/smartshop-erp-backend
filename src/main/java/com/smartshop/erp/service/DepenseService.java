package com.smartshop.erp.service;

import com.smartshop.erp.dto.request.DepenseRequest;
import com.smartshop.erp.dto.response.DepenseResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface DepenseService {
    List<DepenseResponse> lister(LocalDateTime debut, LocalDateTime fin, Long idBoutique);
    DepenseResponse creer(DepenseRequest request, Long idUtilisateurConnecte);
}
