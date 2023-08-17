package com.retailhub.ordermanagementservice.repository;

import com.retailhub.ordermanagementservice.model.OrderDetails;
import com.retailhub.ordermanagementservice.model.OrderHeader;
import com.retailhub.ordermanagementservice.util.OrderIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
@Slf4j
public class OrderDetailsRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String INSERT_ORDER_DETAILS = "INSERT INTO order_details(order_id, product_id, quantity)" +
            " VALUES (:orderId, :productId, :quantity)";
    private static final String RETRIEVE_ORDER_ID = "SELECT order_id from orders WHERE user_id = :userId " +
            "AND product_id = :productId";

    private final RowMapper<OrderDetails> orderDetailsRowMapper = orderDetailsRowMapper();

    public OrderDetailsRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private RowMapper<OrderDetails> orderDetailsRowMapper() {
        return (rs, rowNum) -> OrderDetails.builder()
                .orderId(rs.getInt("order_id"))
                .productId(rs.getInt("product_id"))
                .quantity(rs.getInt("quantity"))
                .build();
    }

    public void insertOrderDetails(OrderDetails orderDetails) {
        enrichOrderDetailsWithOrderId(orderDetails);
        int orderDetailsUpdated = jdbcTemplate.update(INSERT_ORDER_DETAILS, parameterToInsertOrderDetails(orderDetails));
        if (orderDetailsUpdated == 1) {
            log.info("Inserted orders for user : {}", orderDetails);
        } else {
            throw new RuntimeException("Insert failed for user : " + orderDetails);
        }
    }

    private static void enrichOrderDetailsWithOrderId(OrderDetails orderDetails) {
        int orderId = OrderIdGenerator.generateOrderId();
        orderDetails.setOrderId(orderId);
    }

    private MapSqlParameterSource parameterToInsertOrderDetails(OrderDetails orderDetails) {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("productId", orderDetails.getProductId());
        parameterSource.addValue("orderId", orderDetails.getOrderId());
        parameterSource.addValue("quantity", orderDetails.getQuantity());
        return parameterSource;
    }
}

