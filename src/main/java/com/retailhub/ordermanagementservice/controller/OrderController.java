package com.retailhub.ordermanagementservice.controller;

import com.retailhub.ordermanagementservice.model.CartDetails;
import com.retailhub.ordermanagementservice.model.CartDetailsDTO;
import com.retailhub.ordermanagementservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Void> insertOrderDetails(@RequestBody CartDetails cartDetails) {
        orderDetailsService.insertOrderDetails(cartDetails);
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
    public ResponseEntity<Void> deleteOrderFromCart(@PathVariable int orderId) {
        orderDetailsService.deleteOrderFromCart(orderId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
