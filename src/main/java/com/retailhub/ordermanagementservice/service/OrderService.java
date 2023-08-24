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

@Slf4j
@Service
public class OrderService {
    private final OrderHeaderRepository orderHeaderRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private static final String ORDER_STATUS_DRAFT = "DRAFT";
    private static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    private static final String ORDER_STATUS_APPROVED = "APPROVED";

    public OrderService(OrderHeaderRepository orderHeaderRepository, OrderDetailsRepository orderDetailsRepository) {
        this.orderHeaderRepository = orderHeaderRepository;
        this.orderDetailsRepository = orderDetailsRepository;
    }

    @Transactional
    public void addProductsToCart(CartDetailsDTO cartDetailsDTO) {
        CartDetails cartDetails = transformCartDetailsDTOToCartDetails(cartDetailsDTO);
        List<OrderHeader> orderHeaders = orderHeaderRepository.getOrderHeadersForAUser(cartDetails.getOrderHeader());
        boolean isOrderIdMatched = isOrderIdMatched(cartDetails.getOrderHeader().getOrderId(), orderHeaders);
        if (isOrderIdMatched) {
            UpdateCartDetails(cartDetails.getOrderHeader(), orderHeaders, cartDetails.getOrderDetailsList());
        } else {
            enrichOrderHeaderAndOrderDetailsWithOrderId(cartDetails.getOrderHeader(), orderHeaders, cartDetails.getOrderDetailsList());
            insertOrderHeaderAndOrderDetails(cartDetails);
        }
    }

    private void insertOrderHeaderAndOrderDetails(CartDetails cartDetails) {
        orderHeaderRepository.insertOrderHeader(cartDetails.getOrderHeader());
        orderDetailsRepository.insertOrderDetails(cartDetails.getOrderDetailsList());
    }

    private void UpdateCartDetails(OrderHeader orderHeader, List<OrderHeader> orderHeaders, List<OrderDetails> orderDetailsList) {
        updateOrderDetails(orderHeader, orderDetailsList);
    }

    private static boolean isOrderIdMatched(int orderId, List<OrderHeader> orderHeaders) {
        return orderHeaders.stream().anyMatch(orderHeaderDetail -> orderHeaderDetail.getOrderId() == orderId
                && orderHeaderDetail.getOrderStatus().equals(ORDER_STATUS_DRAFT));
    }

    private void updateOrderDetails(OrderHeader orderHeader, List<OrderDetails> orderDetailsList) {
        OrderDetails orderDetails = orderDetailsList.get(0);
        int orderId = orderHeader.getOrderId();
        int productId = orderDetails.getProductId();
        List<OrderDetails> orderDetailList = orderDetailsRepository.retrieveOrderDetailsByOrderIdAndProductId(orderId, productId);
        if (orderDetailList.isEmpty()) {
            orderDetailsRepository.updateOrderDetailsByOrderId(orderHeader, orderDetails);
        } else {
            OrderDetails neworderDetails = orderDetailList.get(0);
            int updatedQuantityForAProduct = neworderDetails.getQuantity() + 1;
            orderDetailsRepository.updateProductQuantityByOrderIdAndProductId(orderId, productId, updatedQuantityForAProduct);
        }
    }

    private CartDetails transformCartDetailsDTOToCartDetails(CartDetailsDTO cartDetailsDTO) {
        OrderHeader orderHeader = buildOrderHeaderFromCartDetailsDTO(cartDetailsDTO);
        List<OrderDetails> orderDetails = transformCartLinesDTOToOrderDetails(cartDetailsDTO);
        return CartDetails.builder()
                .orderHeader(orderHeader)
                .orderDetailsList(orderDetails)
                .build();
    }

    private static OrderHeader buildOrderHeaderFromCartDetailsDTO(CartDetailsDTO cartDetailsDTO) {
        return OrderHeader.builder()
                .orderId(cartDetailsDTO.getOrderId())
                .userId(cartDetailsDTO.getUserId())
                .totalOrderValue(cartDetailsDTO.getTotalOrderValue())
                .orderStatus(cartDetailsDTO.getOrderStatus())
                .build();
    }

    private List<OrderDetails> transformCartLinesDTOToOrderDetails(CartDetailsDTO cartDetailsDTO) {
        return cartDetailsDTO.getCartLineDetailsDTOList().stream()
                .map(cartLineDetailsDTO -> {
                    OrderDetails orderDetails = new OrderDetails();
                    orderDetails.setProductId(cartLineDetailsDTO.getProductId());
                    orderDetails.setProductName(cartLineDetailsDTO.getProductName());
                    orderDetails.setProductPrice(cartLineDetailsDTO.getProductPrice());
                    orderDetails.setQuantity(cartLineDetailsDTO.getQuantity());
                    return orderDetails;
                })
                .toList();
    }

    private void enrichOrderHeaderAndOrderDetailsWithOrderId(OrderHeader orderHeader, List<OrderHeader> orderHeaders, List<OrderDetails> orderDetailsList) {
        int orderId = orderHeader.getOrderId() == 0 ? OrderIdGenerator.generateOrderId() : orderHeader.getOrderId();
        while (true) {
            boolean isOrderIdMatched = isOrderIdMatched(orderId, orderHeaders);
            if (isOrderIdMatched) {
                orderId = OrderIdGenerator.generateOrderId();
            } else {
                break;
            }
        }
        int finalOrderId = orderId;
        orderHeader.setOrderId(finalOrderId);
        orderDetailsList.forEach(orderDetail -> orderDetail.setOrderId(finalOrderId));
    }

    public List<CartDetailsDTO> retrieveCartDetailsByDraftStatus(int userId) {
        List<OrderHeader> orderHeaders = orderHeaderRepository.retrieveOrderHeaderDetailsByUserAndStatus(userId, ORDER_STATUS_DRAFT);
        return retrieveCartDetails(orderHeaders);
    }

    private List<CartDetailsDTO> retrieveCartDetails(List<OrderHeader> orderHeaders) {
        List<CartDetailsDTO> cartDetailsDTOList = new ArrayList<>();
        List<OrderDetails> orderDetails = orderDetailsRepository.retrieveOrderDetails();
        for (OrderHeader orderHeader : orderHeaders) {
            List<OrderDetails> orderDetailsList = orderDetails.stream().filter(orderDetail -> orderDetail.getOrderId() == orderHeader.getOrderId()).toList();
            cartDetailsDTOList.add(transformCartDetailsToDTO(orderHeader, orderDetailsList));
        }
        return cartDetailsDTOList;
    }

    public List<CartDetailsDTO> retrieveOrderDetails(int userId) {
        List<OrderHeader> orderHeaders = orderHeaderRepository.retrieveOrderHeaderDetailsByUser(userId, null);
        return retrieveCartDetails(orderHeaders);
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
                .toList();
    }

    @Transactional
    public void deleteOrderFromCart(int orderId, int productId) {
        orderDetailsRepository.updateProductQuantityByOrderIdAndProductId(orderId, productId, 0);
        int totalQuantity = getTotalQuantityForOrderId(orderId);
        if (totalQuantity == 0) {
            orderHeaderRepository.deleteOrderFromCart(orderId, ORDER_STATUS_CANCELLED);
        }
    }

    private int getTotalQuantityForOrderId(int orderId) {
        int totalQuantity = 0;
        List<OrderDetails> orderDetailList = orderDetailsRepository.retrieveOrderDetailsByOrderId(orderId);
        for (OrderDetails orderDetail : orderDetailList) {
            totalQuantity += orderDetail.getQuantity();
        }
        return totalQuantity;
    }

    @Transactional
    public void submitApprovedOrder(CartDetailsDTO cartDetailsDTO) {
        CartDetails cartDetails = transformCartDetailsDTOToCartDetails(cartDetailsDTO);
        List<OrderHeader> orderHeaders = orderHeaderRepository.getOrderHeadersForAUser(cartDetails.getOrderHeader());
        boolean OrderId = isOrderIdMatched(cartDetails.getOrderHeader().getOrderId(), orderHeaders);
        if (OrderId) {
            UpdateApprovedOrderDetails(cartDetails.getOrderHeader(), orderHeaders, cartDetails.getOrderDetailsList());
        }
    }

    private void UpdateApprovedOrderDetails(OrderHeader orderHeader, List<OrderHeader> orderHeaders, List<OrderDetails> orderDetailsList) {
        updateApprovedOrderDetails(orderHeader, orderDetailsList);
    }

    private void updateApprovedOrderDetails(OrderHeader orderHeader, List<OrderDetails> orderDetailsList) {
        int orderId = orderHeader.getOrderId();

        int updatedTotal = orderHeader.getTotalOrderValue();
        orderDetailsRepository.updateDetailsByOrderId(orderId, updatedTotal, "APPROVED");
        List<OrderDetails> orderDetailList = orderDetailsRepository.retrieveOrderDetailsByOrderId(orderId);

        for (int i = 0; i < orderDetailsList.size(); i++) {
            int productId = orderDetailList.get(i).getProductId();
            int updatedQuantityForAProduct = orderDetailsList.get(i).getQuantity();
            orderDetailsRepository.updateProductQuantityByOrderIdAndProductId(orderId, productId, updatedQuantityForAProduct);
        }
    }


}
