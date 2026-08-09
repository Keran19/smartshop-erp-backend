package com.smartshop.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProduitRequest {

    @NotBlank(message = "Le code-barres est obligatoire")
    private String codeBarres;

    private String reference;

    @NotBlank(message = "Le nom du produit est obligatoire")
    private String nom;

    private String description;

    /** Prix d'achat (cout) - indispensable pour calculer le benefice a la vente */
    @NotNull(message = "Le prix d'achat est obligatoire")
    @PositiveOrZero(message = "Le prix d'achat ne peut pas etre negatif")
    private BigDecimal prixAchat;

    /** Prix unitaire de vente (catalogue) */
    @NotNull(message = "Le prix de vente est obligatoire")
    @Positive(message = "Le prix de vente doit etre positif")
    private BigDecimal prixVente;

    private Integer seuilAlerte;

    private String image;

    private BigDecimal poidsG;

    private BigDecimal volumeMl;

    @NotNull(message = "La categorie est obligatoire")
    private Long idCategorie;

    private Long idMarque;

    private Long idFournisseur;

    // --- Stock initial optionnel a la creation du produit ---
    private Long idBoutique;

    @PositiveOrZero
    private Integer quantiteInitiale;
}
