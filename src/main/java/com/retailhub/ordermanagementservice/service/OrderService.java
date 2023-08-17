package com.retailhub.ordermanagementservice.service;

import com.retailhub.ordermanagementservice.model.CartDetails;
import com.retailhub.ordermanagementservice.model.OrderDetails;
import com.retailhub.ordermanagementservice.model.OrderHeader;
import com.retailhub.ordermanagementservice.repository.OrderDetailsRepository;
import com.retailhub.ordermanagementservice.repository.OrderHeaderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class OrderService {
    private final OrderHeaderRepository orderHeaderRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private static final String ORDER_STATUS_DRAFT = "DRAFT";
    private static final String ORDER_STATUS_CANCELLED = "CANCELLED";

    public OrderService(OrderHeaderRepository orderHeaderRepository, OrderDetailsRepository orderDetailsRepository) {
        this.orderHeaderRepository = orderHeaderRepository;
        this.orderDetailsRepository = orderDetailsRepository;
    }

    @Transactional
    public void insertOrderDetails(CartDetails cartDetails) {
        OrderHeader orderHeader = buildOrderHeader(cartDetails);
        OrderDetails orderDetails = buildOrderDetails(cartDetails);
        orderHeaderRepository.insertOrderHeader(orderHeader);
        orderDetailsRepository.insertOrderDetails(orderDetails);
    }

    private static OrderDetails buildOrderDetails(CartDetails cartDetails) {
        OrderDetails orderDetails = OrderDetails.builder()
                .productId(cartDetails.getProductId())
                .quantity(cartDetails.getQuantity())
                .build();
        return orderDetails;
    }

    private static OrderHeader buildOrderHeader(CartDetails cartDetails) {
        OrderHeader orderHeader = OrderHeader.builder()
                .userId(cartDetails.getUserId())
                .productId(cartDetails.getProductId())
                .totalAmount(cartDetails.getTotalAmount())
                .build();
        return orderHeader;
    }

    public List<OrderHeader> retrieveCartDetails(int userId) {
        return orderHeaderRepository.retrieveOrderDetails(userId, ORDER_STATUS_DRAFT);
    }

    public void deleteOrderFromCart(int userId, int productId) {
        orderHeaderRepository.deleteOrderFromCart(userId, productId, ORDER_STATUS_CANCELLED);
    }

}
