package com.retailhub.ordermanagementservice.controller;

import com.retailhub.ordermanagementservice.model.CartDetails;
import com.retailhub.ordermanagementservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
