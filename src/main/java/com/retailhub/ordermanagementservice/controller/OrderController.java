package com.retailhub.ordermanagementservice.controller;

import com.retailhub.ordermanagementservice.model.CartDetailsDTO;
import com.retailhub.ordermanagementservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderDetailsService;

    public OrderController(OrderService orderDetailsService) {
        this.orderDetailsService = orderDetailsService;
    }

    @Operation(summary = "Adds products to cart")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "add to cart")
    })
    @PostMapping(value = "/add-to-cart")
    public ResponseEntity<Void> insertOrderDetails(@RequestBody CartDetailsDTO cartDetailsDTO) {
        orderDetailsService.addProductsToCart(cartDetailsDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(summary = "Retrieves cart details for a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "retrieve cart details")
    })
    @GetMapping(value = "/cart-details")
    public ResponseEntity<List<CartDetailsDTO>> retrieveCartDetails(@RequestParam int userId) {
        return new ResponseEntity<>(orderDetailsService.retrieveCartDetailsByDraftStatus(userId), HttpStatus.OK);
    }

    @Operation(summary = "Retrieves order details for a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "retrieve order details")
    })
    @GetMapping(value = "/details")
    public ResponseEntity<List<CartDetailsDTO>> retrieveOrderDetails(@RequestParam int userId) {
        return new ResponseEntity<>(orderDetailsService.retrieveOrderDetails(userId), HttpStatus.OK);
    }

    @Operation(summary = "Deletes products from cart")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "delete products from cart")
    })
    @DeleteMapping(value = "/delete-order/{orderId}")
    public ResponseEntity<Void> deleteOrderFromCart(@PathVariable int orderId, @RequestParam int productId) {
        orderDetailsService.deleteOrderFromCart(orderId, productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @PostMapping(value = "/submit-order")
    public ResponseEntity<Void> submitOrderFromCart(@RequestBody CartDetailsDTO cartDetailsDTO) {
        orderDetailsService.submitApprovedOrder(cartDetailsDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
