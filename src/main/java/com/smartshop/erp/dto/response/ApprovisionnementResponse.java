package com.smartshop.erp.dto.response;

import com.smartshop.erp.enums.StatutApprovisionnement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovisionnementResponse {
    private Long idApprovisionnement;
    private String numeroApprovisionnement;
    private LocalDateTime dateCreation;
    private String fournisseur;
    private Long idBoutique;
    private String boutique;
    private String gerant;
    private BigDecimal montantTotal;
    private StatutApprovisionnement statut;
    private String observation;
    private List<LigneApprovisionnementResponse> lignes;
}
