package com.smartshop.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategorieRequest {
    @NotBlank
    private String nom;
    private String description;
}
