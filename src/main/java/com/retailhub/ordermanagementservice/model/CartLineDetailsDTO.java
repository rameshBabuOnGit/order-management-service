package com.retailhub.ordermanagementservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartLineDetailsDTO {
    private int productId;
    private String productName;
    private BigDecimal productPrice;
    private int quantity;
}
