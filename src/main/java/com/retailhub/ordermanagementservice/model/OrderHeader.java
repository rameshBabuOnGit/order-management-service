package com.retailhub.ordermanagementservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderHeader {
    private int orderId;
    private int userId;
    private int productId;
    private int totalAmount;
    private String orderStatus;
}
