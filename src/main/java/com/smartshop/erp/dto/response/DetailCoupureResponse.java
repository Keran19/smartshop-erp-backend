package com.smartshop.erp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailCoupureResponse {
    private Integer billet10000;
    private Integer billet5000;
    private Integer billet2000;
    private Integer billet1000;
    private Integer billet500;
    private BigDecimal pieces;
    private BigDecimal total;
}
