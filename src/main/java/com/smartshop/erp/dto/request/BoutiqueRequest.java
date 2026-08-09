package com.smartshop.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BoutiqueRequest {
    @NotBlank(message = "Le nom de la boutique est obligatoire")
    private String nom;
    private String adresse;
    private String telephone;
    private Boolean principale;
}
