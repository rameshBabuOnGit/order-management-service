package com.retailhub.ordermanagementservice.service;

import com.retailhub.ordermanagementservice.model.CartDetails;
import com.retailhub.ordermanagementservice.model.CartDetailsDTO;
import com.retailhub.ordermanagementservice.model.CartLineDetailsDTO;
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

    public List<CartDetailsDTO> retrieveCartDetails(int userId) {
        List<CartDetailsDTO> cartDetailsDTOList = new ArrayList<>();
        List<OrderHeader> orderHeaders = orderHeaderRepository.retrieveOrderHeaderDetails(userId, ORDER_STATUS_DRAFT);
        List<OrderDetails> orderDetails = orderDetailsRepository.retrieveOrderDetails();
        for(OrderHeader orderHeader : orderHeaders) {
            List<OrderDetails> orderDetailsList = orderDetails.stream().filter(orderDetail -> orderDetail.getOrderId() == orderHeader.getOrderId()).collect(Collectors.toList());
            cartDetailsDTOList.add(transformCartDetailsToDTO(orderHeader, orderDetailsList));
        }
        return cartDetailsDTOList;
    }

    private CartDetailsDTO transformCartDetailsToDTO(OrderHeader orderHeader, List<OrderDetails> orderDetailsList) {
        CartDetailsDTO cartDetailsDTO = new CartDetailsDTO();
        cartDetailsDTO.setOrderId(orderHeader.getOrderId());
        cartDetailsDTO.setUserId(orderHeader.getUserId());
        cartDetailsDTO.setTotalOrderValue(orderHeader.getTotalOrderValue());
        cartDetailsDTO.setOrderStatus(orderHeader.getOrderStatus());
        cartDetailsDTO.setCartLineDetailsDTOList(transformOrderDetailsToCartDetailsDTO(orderDetailsList));
        return cartDetailsDTO;
    }

    private List<CartLineDetailsDTO> transformOrderDetailsToCartDetailsDTO(List<OrderDetails> orderDetailsList) {
        return orderDetailsList.stream()
                .map(orderDetails -> {
                    CartLineDetailsDTO cartLine = new CartLineDetailsDTO();
                    cartLine.setProductId(orderDetails.getProductId());
                    cartLine.setProductName(orderDetails.getProductName());
                    cartLine.setProductPrice(orderDetails.getProductPrice());
                    cartLine.setQuantity(orderDetails.getQuantity());
                    return cartLine;
                })
                .collect(Collectors.toList());
    }

    public void deleteOrderFromCart(int userId, int productId) {
        orderHeaderRepository.deleteOrderFromCart(userId, productId, ORDER_STATUS_CANCELLED);
    }

}
