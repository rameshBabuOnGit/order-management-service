package com.retailhub.ordermanagementservice.repository;

import com.retailhub.ordermanagementservice.model.OrderHeader;
import com.retailhub.ordermanagementservice.util.OrderIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class OrderHeaderRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final String INSERT_ORDER_HEADER = "INSERT INTO orders" +
            "(user_id, order_id, product_id, total_amount, order_status) " +
            "VALUES (:userId, :orderId, :productId, :totalAmount, :orderStatus)";
    private final RowMapper<OrderHeader> orderHeaderRowMapper = orderHeaderRowMapper();

    public OrderHeaderRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
        mapSqlParameterSource.addValue("orderStatus", "DRAFT");
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
