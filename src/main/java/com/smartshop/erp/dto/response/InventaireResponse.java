package com.smartshop.erp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventaireResponse {
    private Long idInventaire;
    private Long idBoutique;
    private String boutique;
    private String utilisateur;
    private LocalDateTime dateInventaire;
    private String observation;
    private List<LigneInventaireResponse> lignes;
}
