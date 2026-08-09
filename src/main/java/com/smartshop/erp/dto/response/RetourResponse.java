package com.smartshop.erp.dto.response;

import com.smartshop.erp.enums.StatutRetour;
import com.smartshop.erp.enums.TypeRetour;
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
public class RetourResponse {
    private Long idRetour;
    private String numeroRetour;
    private Long idVente;
    private String numeroVenteOrigine;
    private Long idBoutique;
    private String boutique;
    private Long idUtilisateur;
    private String utilisateur;
    private TypeRetour typeRetour;
    private StatutRetour statut;
    private LocalDateTime dateRetour;
    private String observation;

    private BigDecimal montantRetourne;
    private BigDecimal montantEchange;
    /** Somme rendue au client (remboursement, ou difference en sa faveur pour un echange). */
    private BigDecimal montantRembourse;
    /** Somme que le client doit regler en plus (echange vers produits de plus grande valeur). */
    private BigDecimal montantComplement;

    private List<LigneRetourResponse> lignesRetour;
    private List<LigneEchangeResponse> lignesEchange;
}
