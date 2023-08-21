package com.retailhub.ordermanagementservice.repository;

import com.retailhub.ordermanagementservice.exception.NotFoundException;
import com.retailhub.ordermanagementservice.model.OrderHeader;
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
            "(user_id, order_id, total_order_value, order_status) " +
            "VALUES (:userId, :orderId, :totalOrderValue, :orderStatus)";
    private static final String RETRIEVE_ORDER_HEADER_DETAILS_BY_USER_AND_STATUS = "SELECT order_id, user_id, total_order_value, order_status " +
            "FROM orders WHERE user_id = :userId AND order_status = :orderStatus";

    private static final String RETRIEVE_ORDER_HEADER_DETAILS_BY_USER = "SELECT order_id, user_id, total_order_value, order_status " +
            "FROM orders WHERE user_id = :userId";

    private static final String DELETE_ORDER_FROM_CART = "UPDATE orders SET order_status = :orderStatus WHERE order_id = :orderId ";

    private static final String RETRIEVE_ORDER_HEADER_DETAILS_FOR_USER = "SELECT order_id, user_id, total_order_value, order_status " +
            "FROM orders WHERE user_id = :userId";

    private final RowMapper<OrderHeader> orderHeaderRowMapper = orderHeaderRowMapper();

    public OrderHeaderRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<OrderHeader> retrieveOrderHeaderDetailsByUserAndStatus(int userId, String orderStatus) {
        MapSqlParameterSource parameterSource = parameterSourceForUserIdAndOrderStatus(userId, orderStatus);
        return jdbcTemplate.query(RETRIEVE_ORDER_HEADER_DETAILS_BY_USER_AND_STATUS, parameterSource, orderHeaderRowMapper);
    }

    public List<OrderHeader> retrieveOrderHeaderDetailsByUser(int userId, String orderStatus) {
        MapSqlParameterSource parameterSource = parameterSourceForUserIdAndOrderStatus(userId, orderStatus);
        return jdbcTemplate.query(RETRIEVE_ORDER_HEADER_DETAILS_BY_USER, parameterSource, orderHeaderRowMapper);
    }


    public void insertOrderHeader(OrderHeader orderHeader) {
        int orderHeaderUpdated = jdbcTemplate.update(INSERT_ORDER_HEADER, parameterToInsertOrderHeader(orderHeader));
        if (orderHeaderUpdated == 1) {
            log.info("Inserted orders for user : {}", orderHeader);
        } else {
            throw new RuntimeException("Insert failed for user : " + orderHeader);
        }
    }

    public void deleteOrderFromCart(int orderId, String orderStatus) {
        MapSqlParameterSource mapSqlParameterSource = parameterSourceForDeletingOrder(orderId, orderStatus);
        int updatedRows = jdbcTemplate.update(DELETE_ORDER_FROM_CART, mapSqlParameterSource);
        log.info("No of rows updated for delete cart API : {}", updatedRows);
        if (updatedRows == 0) {
            throw new NotFoundException("Order Id not found : " + orderId);
        }
    }

    private static MapSqlParameterSource parameterSourceForUserIdAndOrderStatus(int userId, String orderStatus) {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("userId", userId);
        if (orderStatus != null) {
            parameterSource.addValue("orderStatus", orderStatus);
        }
        return parameterSource;
    }

    private static MapSqlParameterSource parameterSourceForDeletingOrder(int orderId, String orderStatus) {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("orderId", orderId);
        parameterSource.addValue("orderStatus", orderStatus);
        return parameterSource;
    }

    public List<OrderHeader> getOrderHeadersForAUser(OrderHeader orderHeader) {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("userId", orderHeader.getUserId());
        List<OrderHeader> orderHeaders = jdbcTemplate.query(RETRIEVE_ORDER_HEADER_DETAILS_FOR_USER, parameterSource, orderHeaderRowMapper);
        return orderHeaders;
    }

    private MapSqlParameterSource parameterToInsertOrderHeader(OrderHeader orderHeader) {
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("userId", orderHeader.getUserId());
        mapSqlParameterSource.addValue("orderId", orderHeader.getOrderId());
        mapSqlParameterSource.addValue("totalOrderValue", orderHeader.getTotalOrderValue());
        mapSqlParameterSource.addValue("orderStatus", ORDER_STATUS_DRAFT);
        return mapSqlParameterSource;
    }
    private RowMapper<OrderHeader> orderHeaderRowMapper() {
        return (rs, rowNum) -> OrderHeader.builder()
                .userId(rs.getInt("user_id"))
                .orderId(rs.getInt("order_id"))
                .totalOrderValue(rs.getInt("total_order_value"))
                .orderStatus(rs.getString("order_status"))
                .build();
    }
}
