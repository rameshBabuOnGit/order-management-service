package com.retailhub.ordermanagementservice.service;

import com.retailhub.ordermanagementservice.model.CartDetails;
import com.retailhub.ordermanagementservice.model.OrderDetails;
import com.retailhub.ordermanagementservice.model.OrderHeader;
import com.retailhub.ordermanagementservice.repository.OrderDetailsRepository;
import com.retailhub.ordermanagementservice.repository.OrderHeaderRepository;
import com.retailhub.ordermanagementservice.util.OrderIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        enrichOrderHeaderAndOrderDetailsWithOrderId(cartDetails.getOrderHeader(), cartDetails.getOrderDetailsList());
        orderHeaderRepository.insertOrderHeader(cartDetails.getOrderHeader());
        orderDetailsRepository.insertOrderDetails(cartDetails.getOrderDetailsList());
    }

    private void enrichOrderHeaderAndOrderDetailsWithOrderId(OrderHeader orderHeader, List<OrderDetails> orderDetailsList) {
        int orderId = orderHeader.getOrderId() == 0 ? OrderIdGenerator.generateOrderId() : orderHeader.getOrderId();
        List<OrderHeader> orderHeaders = orderHeaderRepository.getOrderHeadersForAUser(orderHeader);
        boolean isOrderIdPresent = orderHeaders.stream().anyMatch(orderHeaderData -> orderHeaderData.getOrderId() == orderId);
        int newOrderId = isOrderIdPresent ? OrderIdGenerator.generateOrderId() : orderId;
        orderHeader.setOrderId(newOrderId);
        orderDetailsList.stream().forEach(orderDetail -> orderDetail.setOrderId(newOrderId));
    }

    public List<CartDetails> retrieveCartDetails(int userId) {
        List<CartDetails> cartDetailsList = new ArrayList<>();
        CartDetails cartDetails = new CartDetails();
        List<OrderHeader> orderHeaders = orderHeaderRepository.retrieveOrderHeaderDetails(userId, ORDER_STATUS_DRAFT);
        List<OrderDetails> orderDetails = orderDetailsRepository.retrieveOrderDetails();
        for(OrderHeader orderHeader : orderHeaders) {
            List<OrderDetails> orderDetailsList = orderDetails.stream().filter(orderDetail -> orderDetail.getOrderId() == orderHeader.getOrderId()).collect(Collectors.toList());
            cartDetails.setOrderHeader(orderHeader);
            cartDetails.setOrderDetailsList(orderDetailsList);
            cartDetailsList.add(cartDetails);
        }
        return cartDetailsList;
    }

    public void deleteOrderFromCart(int userId, int productId) {
        orderHeaderRepository.deleteOrderFromCart(userId, productId, ORDER_STATUS_CANCELLED);
    }

}
