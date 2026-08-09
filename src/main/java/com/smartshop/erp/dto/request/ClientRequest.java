package com.smartshop.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClientRequest {
    @NotBlank(message = "Le nom du client est obligatoire")
    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private String adresse;
}
