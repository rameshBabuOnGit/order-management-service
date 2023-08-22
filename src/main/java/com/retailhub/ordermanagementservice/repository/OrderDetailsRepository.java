package com.retailhub.ordermanagementservice.repository;

import com.retailhub.ordermanagementservice.exception.NotFoundException;
import com.retailhub.ordermanagementservice.model.OrderDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@Slf4j
public class OrderDetailsRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String INSERT_ORDER_DETAILS = "INSERT INTO order_details(order_id, product_id, quantity, product_name, product_price)" +
            " VALUES (:orderId, :productId, :quantity, :productName, :productPrice)";
    private static final String RETRIEVE_ORDER_ID = "SELECT order_id from orders WHERE user_id = :userId " +
            "AND product_id = :productId";

    private static final String RETRIEVE_ORDER_DETAILS = "SELECT order_id, product_id, product_name, product_price, quantity FROM order_details";

    private static final String RETRIEVE_ORDER_DETAILS_BY_ORDER_ID = "SELECT order_id, product_id, product_name, product_price, quantity FROM order_details " +
            "WHERE order_id = :orderId";

    private static final String DELETE_PRODUCT_FROM_CART = "UPDATE order_details SET quantity = :quantity WHERE order_id = :orderId " +
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
                .productName(rs.getString("product_name"))
                .productPrice(rs.getBigDecimal("product_price"))
                .build();
    }

    public void insertOrderDetails(List<OrderDetails> orderDetailsList) {
        SqlParameterSource[] batchArgs = parametersToInsertOrderDetails(orderDetailsList);
        int[] orderDetailsUpdated = jdbcTemplate.batchUpdate(INSERT_ORDER_DETAILS, batchArgs);
        int totalCount = 0;
        for (int updatedCount : orderDetailsUpdated) {
            totalCount += updatedCount;
        }
        if (totalCount == orderDetailsList.size()) {
            log.info("Inserted orders for user : {}", orderDetailsList);
        } else {
            throw new RuntimeException("Insert failed for user : " + orderDetailsList);
        }
    }

    public List<OrderDetails> retrieveOrderDetails() {
        return jdbcTemplate.query(RETRIEVE_ORDER_DETAILS, orderDetailsRowMapper);
    }

    public List<OrderDetails> retrieveOrderDetailsByOrderIdAndProductId(int orderId) {
        MapSqlParameterSource mapSqlParameterSource = parameterSourceForRetrievingOrderDetails(orderId);
        return jdbcTemplate.query(RETRIEVE_ORDER_DETAILS_BY_ORDER_ID, mapSqlParameterSource,orderDetailsRowMapper);
    }

    private SqlParameterSource[] parametersToInsertOrderDetails(List<OrderDetails> orderDetailsList) {
        SqlParameterSource[] batchArgs = new SqlParameterSource[orderDetailsList.size()];
        for (int i = 0; i < orderDetailsList.size(); i++) {
            OrderDetails orderDetails = orderDetailsList.get(i);
            MapSqlParameterSource parameterSource = new MapSqlParameterSource();
            parameterSource.addValue("productId", orderDetails.getProductId());
            parameterSource.addValue("orderId", orderDetails.getOrderId());
            parameterSource.addValue("productName", orderDetails.getProductName());
            parameterSource.addValue("productPrice", orderDetails.getProductPrice());
            parameterSource.addValue("quantity", orderDetails.getQuantity());
            batchArgs[i] = parameterSource;
        }
        return batchArgs;
    }

    public void deleteProductFromCart(int orderId, int productId, int quantity) {
        MapSqlParameterSource mapSqlParameterSource = parameterSourceForDeletingOrder(orderId, productId, quantity);
        int updatedRows = jdbcTemplate.update(DELETE_PRODUCT_FROM_CART, mapSqlParameterSource);
        if (updatedRows == 0) {
            throw new NotFoundException("Order Id not found : " + orderId);
        }
    }

    private static MapSqlParameterSource parameterSourceForDeletingOrder(int orderId, int productId, int quantity) {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("orderId", orderId);
        parameterSource.addValue("productId", productId);
        parameterSource.addValue("quantity", quantity);
        return parameterSource;
    }

    private static MapSqlParameterSource parameterSourceForRetrievingOrderDetails(int orderId) {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        parameterSource.addValue("orderId", orderId);
        return parameterSource;
    }
}

