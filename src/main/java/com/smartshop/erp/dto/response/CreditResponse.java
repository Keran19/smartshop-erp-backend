package com.smartshop.erp.dto.response;

import com.smartshop.erp.enums.StatutCredit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditResponse {
    private Long idCredit;
    private Long idVente;
    private String numeroVente;
    private Long idClient;
    private String client;
    private String telephoneClient;
    private BigDecimal montantInitial;
    private BigDecimal montantPaye;
    private BigDecimal resteAPayer;
    private LocalDateTime dateCreation;
    private LocalDate dateLimite;
    private StatutCredit statut;
    private String observation;
    private List<PaiementCreditResponse> paiements;
}
