package com.retailhub.ordermanagementservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDetails {
    private OrderHeader orderHeader;
    private List<OrderDetails> orderDetailsList;
}
