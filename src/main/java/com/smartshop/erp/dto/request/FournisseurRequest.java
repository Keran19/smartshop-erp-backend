package com.smartshop.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FournisseurRequest {
    @NotBlank(message = "Le nom du fournisseur est obligatoire")
    private String nom;
    private String telephone;
    private String email;
    private String adresse;
}
