package com.smartshop.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MarqueRequest {
    @NotBlank
    private String nom;
    private String description;
}
