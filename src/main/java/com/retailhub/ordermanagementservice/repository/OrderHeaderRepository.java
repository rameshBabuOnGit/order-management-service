package com.retailhub.ordermanagementservice.repository;

import com.retailhub.ordermanagementservice.model.OrderHeader;
import com.retailhub.ordermanagementservice.util.OrderIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
public class OrderHeaderRepository {
    private static final String ORDER_STATUS_DRAFT = "DRAFT";
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final String INSERT_ORDER_HEADER = "INSERT INTO orders" +
            "(user_id, order_id, product_id, total_amount, order_status) " +
            "VALUES (:userId, :orderId, :productId, :totalAmount, :orderStatus)";
    private static final String RETRIEVE_ORDER_DETAILS = "SELECT order_id, user_id, product_id, total_amount, order_status " +
            "FROM orders WHERE user_id = :userId AND order_status = :orderStatus";

    private static final String DELETE_ORDER_FROM_CART = "UPDATE orders SET order_status = :orderStatus WHERE user_id = :userId " +
            "AND product_id = :productId";

    private final RowMapper<OrderHeader> orderHeaderRowMapper = orderHeaderRowMapper();

    public OrderHeaderRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<OrderHeader> retrieveOrderDetails(int userId, String orderStatus) {
        MapSqlParameterSource parameterSource = parameterSourceForUserIdAndOrderStatus(userId, 0, orderStatus);
        return jdbcTemplate.query(RETRIEVE_ORDER_DETAILS, parameterSource, orderHeaderRowMapper);
    }

    public void insertOrderHeader(OrderHeader orderHeader) {
        enrichOrderDetailsWithOrderId(orderHeader);
        int orderHeaderUpdated = jdbcTemplate.update(INSERT_ORDER_HEADER, parameterToInsertOrderHeader(orderHeader));
        if (orderHeaderUpdated == 1) {
            log.info("Inserted orders for user : {}", orderHeader);
        } else {
            throw new RuntimeException("Insert failed for user : " + orderHeader);
        }
    }

    public void deleteOrderFromCart(int userId, int productId, String orderStatus) {
        MapSqlParameterSource mapSqlParameterSource = parameterSourceForUserIdAndOrderStatus(userId, productId, orderStatus);
        int updatedRows = jdbcTemplate.update(DELETE_ORDER_FROM_CART, mapSqlParameterSource);
        log.info("No of rows updated for delete cart API : {}", updatedRows);
    }

    private static MapSqlParameterSource parameterSourceForUserIdAndOrderStatus(int userId, int productId, String orderStatus) {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("userId", userId);
        parameterSource.addValue("orderStatus", orderStatus);
        if (productId != 0) {
            parameterSource.addValue("productId", productId);
        }
        return parameterSource;
    }

    private static void enrichOrderDetailsWithOrderId(OrderHeader orderHeader) {
        int orderId = OrderIdGenerator.generateOrderId();
        orderHeader.setOrderId(orderId);
    }

    private MapSqlParameterSource parameterToInsertOrderHeader(OrderHeader orderHeader) {
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("userId", orderHeader.getUserId());
        mapSqlParameterSource.addValue("orderId", orderHeader.getOrderId());
        mapSqlParameterSource.addValue("productId", orderHeader.getProductId());
        mapSqlParameterSource.addValue("totalAmount", orderHeader.getTotalAmount());
        mapSqlParameterSource.addValue("orderStatus", ORDER_STATUS_DRAFT);
        return mapSqlParameterSource;
    }
    private RowMapper<OrderHeader> orderHeaderRowMapper() {
        return (rs, rowNum) -> OrderHeader.builder()
                .userId(rs.getInt("user_id"))
                .orderId(rs.getInt("order_id"))
                .productId(rs.getInt("product_id"))
                .totalAmount(rs.getInt("total_amount"))
                .orderStatus(rs.getString("order_status"))
                .build();
    }
}
